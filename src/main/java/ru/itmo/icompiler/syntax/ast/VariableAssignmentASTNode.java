package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

public class VariableAssignmentASTNode extends ASTNode {
	private ExpressionASTNode lvalue;
	private ExpressionASTNode valueNode;
	
	public VariableAssignmentASTNode(ASTNode parentNode, ExpressionASTNode lvalue, ExpressionASTNode valueNode) {
		super(parentNode, ASTNodeType.VAR_ASSIGN_NODE);
		
		this.lvalue = lvalue;
		this.valueNode = valueNode;
	}
	
	public VariableAssignmentASTNode(ASTNode parentNode, Token token, ExpressionASTNode valueNode) {
		this(parentNode, new VariableExpressionNode(null, token), valueNode);
	}
	
	public ExpressionASTNode getValueNode() {
		return this.valueNode;
	}
	
	public ExpressionASTNode getLeftSide() {
		return lvalue;
	}
	
	public String toString(int tabs) {
		return String.format("%s[lvalue = %s, expr = %s]", 
					getNodeType(),
					lvalue.toString(tabs + 1),
					valueNode.toString(tabs + 1)
				);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
