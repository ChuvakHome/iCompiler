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
		addChild(lvalue);
		
		this.valueNode = valueNode;
		valueNode.setParentNode(this);
	}
	
	public VariableAssignmentASTNode(ASTNode parentNode, Token token, ExpressionASTNode valueNode) {
		this(parentNode, new VariableExpressionNode(null, token), valueNode);
	}
	
	public void setLeftSide(ExpressionASTNode leftValue) {
		lvalue = leftValue;
	}
	
	public ExpressionASTNode getLeftSide() {
		return lvalue;
	}
	
	public void setValueNode(ExpressionASTNode valueNode) {
		this.valueNode = valueNode;
	}
	
	public ExpressionASTNode getValueNode() {
		return this.valueNode;
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

	@Override
    public Token getToken() {
        return lvalue.getToken();
    }
}
