package ru.itmo.icompiler.semantic.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.ArrayType.SizedArrayType;
import ru.itmo.icompiler.semantic.RecordType;
import ru.itmo.icompiler.semantic.RecordType.RecordProperty;
import ru.itmo.icompiler.semantic.SemUtils;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.SemanticContext.Scope;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.UndefinedTypeSemanticException;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.BreakStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ContinueStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForEachStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForInRangeStatementASTNode;
import ru.itmo.icompiler.syntax.ast.IfThenElseStatementASTNode;
import ru.itmo.icompiler.syntax.ast.PrintStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ProgramASTNode;
import ru.itmo.icompiler.syntax.ast.ReturnStatementASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDefinitionASTNode;
import ru.itmo.icompiler.syntax.ast.TypeDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.VariableAssignmentASTNode;
import ru.itmo.icompiler.syntax.ast.VariableDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.WhileStatementASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class TypealiasResolverASTVisitor extends AbstractASTVisitor {
	private List<ASTNode> detachingCandidates;
	
	public TypealiasResolverASTVisitor(AbstractExpressionASTVisitor expressionVisitor) {
		super(expressionVisitor);
		
		detachingCandidates = new ArrayList<>();
	}
	
	private VarType processType(VarType originalType, SemanticContext ctx) {
		VarType realType = SemUtils.getRealType(originalType, ctx);
		
		if (realType == null)
			return null;
		else if (realType == VarType.AUTO_TYPE)
			return realType; 
		
		switch (realType.getTag()) {
			case RECORD:
				List<RecordProperty> props = new ArrayList<>();
				RecordType recordType = (RecordType) realType;
				
				List<CompilerException> propertyResolvingErrors = new ArrayList<>();
				
				for (RecordProperty prop: recordType.getProperties()) {
					VarType propRealType = processType(prop.type, ctx);
					
					if (propRealType != null)
						props.add(new RecordProperty(propRealType, prop.name, prop.line, prop.offset));
					else
						propertyResolvingErrors.add(new UndefinedTypeSemanticException(prop.type.getTypename(), prop.line, prop.offset));
				}
				
				if (propertyResolvingErrors.isEmpty())
					return new RecordType(props);
			
				propertyResolvingErrors.forEach(ctx::addCompilerError);
				
				return null;
			case ARRAY:
				ArrayType arrayType = (ArrayType) realType;
				
				VarType processedElemType = processType(arrayType.getElementType(), ctx);
				
				if (processedElemType == null)
					return null;
				
				if (arrayType instanceof SizedArrayType sizedArrayType)
					return new SizedArrayType(processedElemType, sizedArrayType.getSize());
				
				return new ArrayType(processedElemType);
			default:
				return realType;
		}
	}
	
	private VarType findRealTypeWithFailureReport(VarType type, Token tk, SemanticContext ctx) {
		VarType realVarType = processType(type, ctx);
		
		if (realVarType == null)
			ctx.addCompilerError(new UndefinedTypeSemanticException(type.toString(), tk.lineNumber, tk.lineOffset));
		
		return realVarType;
	}
	
	@Override
	public SemanticContext visit(ProgramASTNode node, SemanticContext ctx) {
		SemanticContext res = super.visit(node, ctx);
		
		detachingCandidates.forEach(ASTNode::detach);
		
		return res;
	}

	@Override
	public SemanticContext visit(VariableDeclarationASTNode node, SemanticContext ctx) {
		VarType realVarType = findRealTypeWithFailureReport(node.getVarType(), node.getToken(), ctx);
		
		if (realVarType != null) {
			node.setVarType(realVarType);
			
			ctx.getScope().addEntity(node.getVarName(), realVarType);
		} else
			detachingCandidates.add(node);
		
		return ctx;
	}

	@Override
	public SemanticContext visit(VariableAssignmentASTNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(TypeDeclarationASTNode node, SemanticContext ctx) {
		String typename = node.getTypename();
		
		VarType replaceType = findRealTypeWithFailureReport(node.getType(), node.getToken(), ctx);
		
		if (replaceType == null)
			detachingCandidates.add(node);
		else {
			VarType oldEntityType = ctx.getScope().lookup(typename);
			
			if (oldEntityType == null)
				ctx.getScope().addTypealias(typename, replaceType);
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(RoutineDeclarationASTNode node, SemanticContext ctx) {
		for (VariableDeclarationASTNode argDecl: node.getArgumentsDeclarations())
			argDecl.accept(this, ctx);
		
		VarType realRetType = findRealTypeWithFailureReport(node.getResultType(), node.getToken(), ctx);
		
		node.setResultType(Optional.ofNullable(realRetType).orElse(VarType.VOID_TYPE));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(RoutineDefinitionASTNode node, SemanticContext ctx) {
		node.getRoutineDeclaration().accept(this, ctx);
		
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope())));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ReturnStatementASTNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(IfThenElseStatementASTNode node, SemanticContext ctx) {
		node.getTrueBranch().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope())));
		
		if (node.getElseBranch() != null)
			node.getElseBranch().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope())));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ForInRangeStatementASTNode node, SemanticContext ctx) {
		ctx.getScope().addEntity(node.getIterVariable(), VarType.INTEGER_PRIMITIVE_TYPE);
		addDefinitionInfo(node.getIterVariable(), new int[] { node.getToken().lineNumber });
		
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(
					ctx.getScope(),
					Map.of(),
					Map.of(node.getIterVariable(), VarType.INTEGER_PRIMITIVE_TYPE)
				)));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ForEachStatementASTNode node, SemanticContext ctx) {
		ctx.getScope().addEntity(node.getIterVariable(), VarType.AUTO_TYPE);
		
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(
				ctx.getScope(),
				Map.of(),
				Map.of(node.getIterVariable(), VarType.AUTO_TYPE)
			)));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(WhileStatementASTNode node, SemanticContext ctx) {
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope())));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(BreakStatementASTNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(ContinueStatementASTNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(PrintStatementASTNode node, SemanticContext ctx) {
		return ctx;
	}
	
	@Override
	public SemanticContext visit(ExpressionASTNode node, SemanticContext ctx) {
		return ctx;
	}
}
