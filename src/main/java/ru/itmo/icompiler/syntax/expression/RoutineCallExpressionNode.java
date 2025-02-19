package ru.itmo.icompiler.syntax.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.itmo.icompiler.syntax.ASTNode;

public class RoutineCallExpressionNode extends ExpressionASTNode {
	private String routineName;
	private List<ExpressionASTNode> arguments;
	
	public RoutineCallExpressionNode(ASTNode parentNode, String routineName, ExpressionASTNode... arguments) {
		super(parentNode, ExpressionNodeType.FUN_CALL_EXPR_NODE);
		
		this.routineName = routineName;
		this.arguments = new ArrayList<>(Arrays.asList(arguments));
	}
	
	public void addArguments(ExpressionASTNode... arguments) {
		this.arguments.addAll(Arrays.asList(arguments));
	}
	
	public String getRoutineName() {
		return routineName;
	}
	
	public List<ExpressionASTNode> getArguments() {
		return arguments;
	}
	
	public String toString() {
		return toString(0);
	}
	
	public String toString(int tabs) {
		List<String> argsStringified = arguments
											.stream()
											.map(arg -> "\n" + arg.toString(tabs + 1))
											.toList();
		
		return String.format("%s::%s{routine = %s, args = [%s]}",
					getNodeType(), getExpressionNodeType(),
					routineName, argsStringified
				);
	}
}
