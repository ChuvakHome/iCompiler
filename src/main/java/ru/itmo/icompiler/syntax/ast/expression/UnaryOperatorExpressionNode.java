package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.syntax.ast.ASTNode;

public class UnaryOperatorExpressionNode extends ExpressionASTNode {
	private UnaryOperatorType unopType;
	private ExpressionASTNode value;
	
	public UnaryOperatorExpressionNode(ASTNode parentNode, UnaryOperatorType operatorType, ExpressionASTNode value) {
		super(parentNode, ExpressionNodeType.UNOP_EXPR_NODE);
		
		this.unopType = operatorType;
		setValue(value);
	}
	
	public void setValue(ExpressionASTNode value) {
		this.value = value;
		
		if (value != null)
			addChild(value);
	}
	
	public UnaryOperatorType getUnaryOperatorType() {
		return unopType;
	}
	
	public ExpressionASTNode getValue() {
		return value;
	}
	
	@Override
	public void addChild(ASTNode child) {
		ExpressionASTNode exprNode = (ExpressionASTNode) child;
		
		if (children.isEmpty())
			children.add(exprNode);
		else
			children.set(0, exprNode);
		
		if (value != exprNode)
			value = exprNode;
		
		exprNode.updateParentNode(this);
	}
	
	public String toString() {
		return toString(0);
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(Locale.ENGLISH, "%s::%s[%stype = %s,%svalue = %s]",
					getNodeType(), getExpressionNodeType(),
					sep, unopType,
					sep, value.toString(tabs + 1)
				);
	}

	public static enum UnaryOperatorType {
		PLUS_BINOP,
		MINUS_BINOP,
		NOT_BINOP,
	}
}
