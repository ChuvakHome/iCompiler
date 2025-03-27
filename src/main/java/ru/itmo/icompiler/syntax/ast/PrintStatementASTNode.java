package ru.itmo.icompiler.syntax.ast;

import java.util.Arrays;
import java.util.List;

import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class PrintStatementASTNode extends ASTNode {
	public PrintStatementASTNode(ASTNode parentNode, List<ExpressionASTNode> expressions) {
		super(parentNode, ASTNodeType.PRINT_STMT_NODE);
		
		addChildren(expressions);
	}
	
	public PrintStatementASTNode(ASTNode parentNode, ExpressionASTNode... expressions) {
		this(parentNode, Arrays.asList(expressions));
	}
}
