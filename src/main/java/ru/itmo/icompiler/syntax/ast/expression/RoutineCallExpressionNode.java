package ru.itmo.icompiler.syntax.ast.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.SemUtils;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.AttemptToCallNonCallableSemanticException;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.exception.TooFewArgumentsForRoutineCallSemanticException;
import ru.itmo.icompiler.semantic.exception.TooManyArgumentsForRoutineCallSemanticException;
import ru.itmo.icompiler.semantic.exception.UndefinedRoutineSemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class RoutineCallExpressionNode extends ExpressionASTNode {
	private String routineName;
	private FunctionType routineType;
	private List<ExpressionASTNode> arguments;
	
	public RoutineCallExpressionNode(ASTNode parentNode, Token startToken, String routineName, FunctionType routineType, List<ExpressionASTNode> arguments) {
		super(parentNode, startToken, ExpressionNodeType.FUN_CALL_EXPR_NODE);
		
		this.routineType = routineType;
		this.routineName = routineName;
		this.arguments = new ArrayList<>(arguments);
	}
	
	public RoutineCallExpressionNode(ASTNode parentNode, Token startToken, String routineName, List<ExpressionASTNode> arguments) {
		this(parentNode, startToken, routineName, null, arguments);
	}
	
	public RoutineCallExpressionNode(ASTNode parentNode, Token startToken, FunctionType routineType, ExpressionASTNode... arguments) {
		this(parentNode, startToken, startToken.text, routineType, new ArrayList<>(Arrays.asList(arguments)));
	}
	
	public RoutineCallExpressionNode(ASTNode parentNode, Token startToken, ExpressionASTNode... arguments) {
		this(parentNode, startToken, null, arguments);
	}
	
	public void addArguments(ExpressionASTNode... arguments) {
		this.arguments.addAll(Arrays.asList(arguments));
	}
	
	public void setRoutineType(FunctionType routineType) {
		this.routineType = routineType;
	}
	
	public FunctionType getRoutineType() {
		return routineType;
	}
	
	public String getRoutineName() {
		return routineName;
	}
	
	public List<ExpressionASTNode> getArguments() {
		return arguments;
	}
	
	@Override
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg); 
	}
	
	public String toString() {
		return toString(0);
	}
	
	public String toString(int tabs) {
		List<String> argsStringified = arguments
											.stream()
											.map(arg -> "\n" + " ".repeat((tabs + 1) * 4) + arg.toString(tabs + 1))
											.toList();
		
		return String.format("%s::%s{routine = %s, args = [%s]}",
					getNodeType(), getExpressionNodeType(),
					routineName, argsStringified
				);
	}
	
	private void validatePresence(SemanticContext ctx) throws SemanticException {
		Token tk = getStartToken();
		
		VarType type = SemUtils.checkRoutine(routineName, ctx, true, new UndefinedRoutineSemanticException(routineName, tk.lineNumber, tk.lineOffset));
		
		if (type.getTag() != VarType.Tag.FUNCTION)
			throw new AttemptToCallNonCallableSemanticException(type, tk.lineNumber, tk.lineOffset);
	}
	
	public void validate(SemanticContext ctx) throws CompilerException {
		validatePresence(ctx);
		
		FunctionType funcType = (FunctionType) ctx.getScope().deepLookupRoutine(routineName);
				
		Iterator<Entry<String, VarType>> argsIter = funcType.getArgumentsTypes().entrySet().iterator();
		Iterator<ExpressionASTNode> iter = arguments.iterator();
		
		while (argsIter.hasNext() && iter.hasNext()) {
			Entry<String, VarType> argEntry = argsIter.next();
			
			ExpressionASTNode argExpr = iter.next();
			argExpr.validate(ctx);
			
			try {
				argExpr.checkType(ctx, argEntry.getValue());
			} catch (CompilerException e) {
				ctx.addCompilerError(e);
			}
		}
		
		if (argsIter.hasNext()) {
			ctx.addCompilerError(new TooFewArgumentsForRoutineCallSemanticException(
						getStartToken().lineNumber,
						getStartToken().lineOffset
					));
		} else if (iter.hasNext()) {
			Token tk = iter.next().getStartToken();
			
			ctx.addCompilerError(new TooManyArgumentsForRoutineCallSemanticException(
					tk.lineNumber, tk.lineOffset
				));
		}
	}

	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		FunctionType varType = ctx.getScope().deepLookupRoutine(routineName);
		
		return varType.getReturnType();
	}

	@Override
    public Token getToken() {
        return getStartToken();
    }
}
