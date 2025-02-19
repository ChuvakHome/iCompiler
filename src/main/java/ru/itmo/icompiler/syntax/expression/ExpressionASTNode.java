package ru.itmo.icompiler.syntax.expression;

import ru.itmo.icompiler.syntax.ASTNode;

public class ExpressionASTNode extends ASTNode {
	private ExpressionNodeType exprNodeType;
	
	public ExpressionASTNode(ASTNode parentNode, ExpressionNodeType exprNodeType) {
		super(parentNode, ASTNodeType.EXPRESSION_NODE);
		this.exprNodeType = exprNodeType;
	}
	
	public ExpressionNodeType getExpressionNodeType() {		
		return exprNodeType;
	}
	
	protected void updateParentNode(ASTNode node) {
		super.setParentNode(node);
	}
	
	public String toString() {
		return String.format("%s[exprType = %s]",
					getNodeType(),
					exprNodeType
				);
	}
	
	public static enum ExpressionNodeType {
		BOOLEAN_VALUE_EXPR_NODE,
		INTEGER_VALUE_EXPR_NODE,
		REAL_VALUE_EXPR_NODE,
		VARIABLE_EXPR_NODE,
		UNOP_EXPR_NODE,
		BINOP_EXPR_NODE,
		ARRAY_ACCESS_EXPR_NODE,
		FUN_CALL_EXPR_NODE,
		PROPERTY_ACCESS_EXPR_NODE,
		RECORD_PROPERTY_NAME_EXPR_NODE,
	}
}
