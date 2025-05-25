package ru.itmo.icompiler.semantic.visitor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.SemanticContext.Scope;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.VarType.Tag;
import ru.itmo.icompiler.semantic.exception.EntityRedefinitionSemanticException;
import ru.itmo.icompiler.semantic.exception.LoopStatementOutsideLoopSemanticException;
import ru.itmo.icompiler.semantic.exception.NonAssignableLeftPartSemanticException;
import ru.itmo.icompiler.semantic.exception.NonIterableInForEachSemanticException;
import ru.itmo.icompiler.semantic.exception.RoutineDeclarationMismatchSemanticException;
import ru.itmo.icompiler.semantic.exception.RoutineNotDefinedSemanticException;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.exception.VariableIsSizelessArraySemanticException;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.BreakStatementASTNode;
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
import ru.itmo.icompiler.syntax.ast.expression.ImplicitCastExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.exception.VariableDeclWithoutTypeSyntaxException;

public class SimpleASTVisitor extends AbstractASTVisitor {
	private FunctionType currentRoutineType;
	private Map<String, FunctionType> routines;
	private boolean loopProcessing;
	
	public SimpleASTVisitor(AbstractExpressionASTVisitor expressionisitor) {
		super(expressionisitor);
	}
	
	private void tryAddVariableToScope(VariableDeclarationASTNode node, SemanticContext ctx, boolean reportRedefintion) {
		String varName = node.getVarName();
		VarType varType = node.getVarType();
		Token tk = node.getToken();
		
		if (ctx.getScope().lookup(varName) != null) {
			if (reportRedefintion)
				ctx.addCompilerError(new EntityRedefinitionSemanticException(varName, tk.lineNumber, tk.lineOffset, lookupDefinitionInfo(varName)));
		}
		else {
			addDefinitionInfo(varName, new int[] { tk.lineNumber });
			ctx.getScope().addEntity(varName, varType);
		}
	}
	
	private SemanticContext visitRoutineArgDecl(VariableDeclarationASTNode node, SemanticContext ctx) {
		if (ctx.getScope().lookup(node.getVarName()) == null)
			tryAddVariableToScope(node, ctx, false);
		
		return ctx;
	}
	
	@Override
	public SemanticContext visit(ProgramASTNode node, SemanticContext ctx) {
		routines = new HashMap<>();

		for (ASTNode child: node.getChildren())
			child.accept(this, ctx);
		
		for(Map.Entry<String, FunctionType> entry : routines.entrySet()) {
			if (entry.getValue() == null) {
				String routineName = entry.getKey();
				int[] routineDecl = lookupDefinitionInfo(routineName);
				ctx.addCompilerError(new RoutineNotDefinedSemanticException(routineName, routineDecl[0], 1));
			}
		}

		return ctx;
	}

	@Override
	public SemanticContext visit(VariableDeclarationASTNode node, SemanticContext ctx) {
		String varName = node.getVarName();
		VarType varType = node.getVarType();
		
		VariableAssignmentASTNode assignNode;
		
		if (varType == VarType.AUTO_TYPE) { /* check for type inference need */
			assignNode = (VariableAssignmentASTNode) node.getChild(0);
			
			try {
				ExpressionASTNode assignValue = assignNode.getValueNode();
				
				assignValue.validate(ctx);
				varType = assignNode.getValueNode().inferType(ctx);
				
				ctx.getScope().addEntity(varName, varType);
				assignNode.getLeftSide().inferType(ctx); // force to cache variable type
			} catch (CompilerException e) {
				varType = null;
				
				ctx.addCompilerError(e);
				
				Token tok = node.getToken();
				
				ctx.addCompilerError(new VariableDeclWithoutTypeSyntaxException(null, tok.lineNumber, tok.lineOffset));
			}
		} else {
			if (varType != null && varType.getClass() == ArrayType.class)
				ctx.addCompilerError(new VariableIsSizelessArraySemanticException(node.getToken().lineNumber, node.getToken().lineOffset));
			
			assignNode = node.getChildren().isEmpty() ? null : (VariableAssignmentASTNode) node.getChild(0);
			
			if (assignNode != null) {
				if (varType == null) {
					try {
						assignNode.getValueNode().validate(ctx);
					} catch (CompilerException e) {
						ctx.addCompilerError(e);
					}
				} else {
					visitAssigmentLite(assignNode, new SemanticContext(
								ctx.getCompilerErrors(),
								new Scope(ctx.getScope(), Map.of(varName, varType))
							), ctx);
					
					tryAddVariableToScope(node, ctx, true);
				}
			} else if (varType != null)
				tryAddVariableToScope(node, ctx, true);
		}
		
		if (assignNode != null)
			assignNode.getValueNode().accept(this, ctx);
		
		node.setVarType(varType);
		
		return ctx;
	}

	private static void checkPrimaryAssignable(ExpressionASTNode expr) throws SemanticException {
		SemanticException e = new NonAssignableLeftPartSemanticException(expr.getStartToken().lineNumber, expr.getStartToken().lineOffset);
		
		switch (expr.getExpressionNodeType()) {
			case PROPERTY_ACCESS_EXPR_NODE:
				PropertyAccessExpressionNode propAccExpr = (PropertyAccessExpressionNode) expr;
				
				ExpressionASTNode holder = propAccExpr.getPropertyHolder();
				String propName = propAccExpr.getPropertyName();
				
				if (holder.getExpressionType().getTag() == Tag.ARRAY && "length".equals(propName))
					throw e;
			case VARIABLE_EXPR_NODE:
			case ARRAY_ACCESS_EXPR_NODE:
				break;
			default:
				throw e;
		}
	}
	
	private void visitAssigmentLite(VariableAssignmentASTNode node, SemanticContext lhsCtx, SemanticContext ctx) {
		ExpressionASTNode lhs = node.getLeftSide();
		ExpressionASTNode rhs = node.getValueNode();
		
		try {
			rhs.validate(ctx);
			
			lhs.validate(lhsCtx);
			checkPrimaryAssignable(lhs);
			
			VarType leftType = lhs.inferType(lhsCtx);

			rhs.checkType(ctx, leftType);
			rhs.accept(this, ctx);
			
			if (!rhs.getExpressionType().equals(leftType))
				node.setValueNode(new ImplicitCastExpressionNode(null, leftType, rhs));
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
	}
	
	@Override
	public SemanticContext visit(VariableAssignmentASTNode node, SemanticContext ctx) {
		visitAssigmentLite(node, ctx, ctx);
		
		return ctx;
	}

	@Override
	public SemanticContext visit(TypeDeclarationASTNode node, SemanticContext ctx) {
		Token tk = node.getToken();
		String typename = node.getTypename();
		VarType newType = node.getType();
		
		Scope scope = ctx.getScope();
		VarType entity = scope.lookup(typename);
		if (entity != null) {
			ctx.addCompilerError(new EntityRedefinitionSemanticException(typename, tk.lineNumber, tk.lineOffset, lookupDefinitionInfo(typename)));
			return ctx;
		}
		scope.addTypealias(typename, newType);
		addDefinitionInfo(typename, new int[] { tk.lineNumber });
		
		return ctx;
	}

	@Override
	public SemanticContext visit(RoutineDeclarationASTNode node, SemanticContext ctx) {
		try {
			node.validate(ctx);
			
			openScope();
			node.getArgumentsDeclarations().forEach(arg -> visitRoutineArgDecl(arg, ctx));
			closeScope();
			
			Token tk = node.getToken();
			
			FunctionType funcType = parseRoutineTypeFromDecl(node, ctx);
			
			String routineName = node.getRoutineName();
			
			VarType definitionType = routines.get(routineName);
			VarType entityType = ctx.getScope().lookup(routineName);

			boolean isDeclared = entityType != null;
			boolean isDefined = definitionType != null;
			
			if (isDeclared && isDefined)
				throw new EntityRedefinitionSemanticException(routineName, tk.lineNumber, tk.lineOffset, lookupDefinitionInfo(routineName));
			
			if (isDeclared) {
				if (funcType.equals(entityType))
					addDefinitionInfo(routineName, new int[] { tk.lineNumber });
				else
					throw new RoutineDeclarationMismatchSemanticException(routineName, tk.lineNumber, tk.lineOffset, lookupDefinitionInfo(routineName));
			} else {
				addDefinitionInfo(routineName, new int[] { tk.lineNumber });
				ctx.getScope().addEntity(routineName, funcType);
				routines.put(routineName, null);
			}
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(RoutineDefinitionASTNode node, SemanticContext ctx) {
		visit(node.getRoutineDeclaration(), ctx);
		
		FunctionType funcType = parseRoutineTypeFromDecl(node.getRoutineDeclaration(), ctx);
		routines.put(node.getRoutineDeclaration().getRoutineName(), funcType);
		
		Scope subscope = new Scope(ctx.getScope(), funcType.getArgumentsTypes(), new HashMap<>());
		
		currentRoutineType = funcType;
		
		openScope();
		node.getRoutineDeclaration().getArgumentsDeclarations().forEach(arg -> addDefinitionInfo(arg.getVarName(), new int[]{ arg.getToken().lineNumber }));
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), subscope));
		closeScope();
		
		currentRoutineType = null;
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ReturnStatementASTNode node, SemanticContext ctx) {
		try {
			VarType expectedReturnType = currentRoutineType.getReturnType();
			
			ExpressionASTNode resultNode = node.getResultNode(); 
			
			resultNode.checkType(ctx, expectedReturnType);
			resultNode.accept(this, ctx);
			
			if (!resultNode.getExpressionType().equals(expectedReturnType))
				node.setResultValue(new ImplicitCastExpressionNode(null, expectedReturnType, resultNode));
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(IfThenElseStatementASTNode node, SemanticContext ctx) {
		try {
			node.validate(ctx);
			
			ExpressionASTNode conditionExpr = node.getConditionExpression();
			
			conditionExpr.accept(this, ctx);
			if (!conditionExpr.getExpressionType().equals(VarType.BOOLEAN_PRIMITIVE_TYPE))
				node.setConditionExpression(new ImplicitCastExpressionNode(null, VarType.BOOLEAN_PRIMITIVE_TYPE, conditionExpr));
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
		
		openScope();
		node.getTrueBranch().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope())));
		closeScope();

		if (node.getElseBranch() != null) {
			openScope();
			node.getElseBranch().accept(this, new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope())));
			closeScope();
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ForInRangeStatementASTNode node, SemanticContext ctx) {
		ExpressionASTNode fromExpr = node.getFromExpression();
		ExpressionASTNode toExpr = node.getToExpression();
		
		try {
			fromExpr.validate(ctx);
			fromExpr.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE);
			
			fromExpr.accept(this, ctx);
			if (!fromExpr.getExpressionType().equals(VarType.INTEGER_PRIMITIVE_TYPE))
				node.setFromExpression(new ImplicitCastExpressionNode(null, VarType.INTEGER_PRIMITIVE_TYPE, fromExpr));
		} catch (CompilerException e) {
			e.printStackTrace();
		}
		
		try {
			toExpr.validate(ctx);
			toExpr.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE);
			
			toExpr.accept(this, ctx);
			if (!toExpr.getExpressionType().equals(VarType.INTEGER_PRIMITIVE_TYPE))
				node.setToExpression(new ImplicitCastExpressionNode(null, VarType.INTEGER_PRIMITIVE_TYPE, toExpr));
		} catch (CompilerException e) {
			e.printStackTrace();
		}
		
		openScope();
		
		addDefinitionInfo(node.getIterVariable(), new int[] { node.getToken().lineNumber });
		
		Scope subscope = new Scope(ctx.getScope(), Map.of(node.getIterVariable(), VarType.INTEGER_PRIMITIVE_TYPE));
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), subscope));
		closeScope();
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ForEachStatementASTNode node, SemanticContext ctx) {
		ExpressionASTNode arrayExpr = node.getArrayExpression();
		
		SemanticContext bodyCtx; 
		
		try {
			arrayExpr.validate(ctx);
			VarType iterEntityType = arrayExpr.inferType(ctx);
			
			if (iterEntityType.getTag() == VarType.Tag.ARRAY) {
				ArrayType iterArray = (ArrayType) iterEntityType;
				
				Scope subscope = new Scope(ctx.getScope(), Map.of(
							node.getIterVariable(), iterArray.getElementType()
						));
				
				bodyCtx = new SemanticContext(ctx.getCompilerErrors(), subscope);
			} else
				throw new NonIterableInForEachSemanticException(iterEntityType, arrayExpr.getStartToken().lineNumber, arrayExpr.getStartToken().lineOffset);
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
			
			bodyCtx = new SemanticContext(ctx.getCompilerErrors(), new Scope(ctx.getScope()));
		}
		
		openScope();
		
		addDefinitionInfo(node.getIterVariable(), new int[] { node.getToken().lineNumber });
		
		node.getBody().accept(this, bodyCtx);
		closeScope();
		
		return ctx;
	}

	@Override
	public SemanticContext visit(WhileStatementASTNode node, SemanticContext ctx) {
		try {
			node.validate(ctx);
			
			ExpressionASTNode conditionExpr = node.getConditionExpression();
			
			conditionExpr.accept(this, ctx);
			if (!conditionExpr.getExpressionType().equals(VarType.BOOLEAN_PRIMITIVE_TYPE))
				node.setConditionExpression(new ImplicitCastExpressionNode(null, VarType.BOOLEAN_PRIMITIVE_TYPE, conditionExpr));
		} catch (CompilerException e) {
			ctx.addCompilerError(e);
		}
		
		loopProcessing = true;
		
		Scope subscope = new Scope(ctx.getScope());
		
		openScope();
		node.getBody().accept(this, new SemanticContext(ctx.getCompilerErrors(), subscope));
		closeScope();
		
		loopProcessing = false;
		
		return ctx;
	}

	@Override
	public SemanticContext visit(BreakStatementASTNode node, SemanticContext ctx) {
		if (!loopProcessing)
			ctx.addCompilerError(new LoopStatementOutsideLoopSemanticException(node.getToken().lineNumber, node.getToken().lineOffset));
		
		return ctx;
	}

	@Override
	public SemanticContext visit(PrintStatementASTNode node, SemanticContext ctx) {
		for (ASTNode child: node.getChildren()) {
			try {
				ExpressionASTNode expr = (ExpressionASTNode) child;
				
				expr.validate(ctx);
				
				expr.checkType(
							ctx,
							VarType.BOOLEAN_PRIMITIVE_TYPE,
							VarType.INTEGER_PRIMITIVE_TYPE,
							VarType.REAL_PRIMITIVE_TYPE
						);
				expr.accept(this, ctx);
			} catch (CompilerException e) {
				ctx.addCompilerError(e);
			}
		}
		
		return ctx;
	}
	
	private static FunctionType parseRoutineTypeFromDecl(RoutineDeclarationASTNode node, SemanticContext ctx) {
		LinkedHashMap<String, VarType> argsTypes = new LinkedHashMap<>();
		
		for (VariableDeclarationASTNode argDecl: node.getArgumentsDeclarations()) {
			if (!argsTypes.containsKey(argDecl.getVarName())) {
				argsTypes.put(
						argDecl.getVarName(),
						argDecl.getVarType()
				);
			}
		}
		
		VarType retType = node.getResultType();
		
		return new FunctionType(
					argsTypes, 
					retType
				);
	}
}
