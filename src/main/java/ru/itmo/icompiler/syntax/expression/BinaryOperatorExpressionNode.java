package ru.itmo.icompiler.syntax.expression;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ru.itmo.icompiler.syntax.ASTNode;

public class BinaryOperatorExpressionNode extends ExpressionASTNode {
	private BinaryOperatorType binopType;
	private ExpressionASTNode leftChild, rightChild;
	
	public BinaryOperatorExpressionNode(ASTNode parentNode, BinaryOperatorType operatorType, ExpressionASTNode leftChild, ExpressionASTNode rightChild) {
		super(parentNode, ExpressionNodeType.BINOP_EXPR_NODE);
		
		this.binopType = operatorType;
		
		setLeftChild(leftChild);
		setRightChild(rightChild);
	}
	
	BinaryOperatorExpressionNode(ASTNode parentNode, ExpressionNodeType expressionNodeType, BinaryOperatorType operatorType, ExpressionASTNode leftChild, ExpressionASTNode rightChild) {
		super(parentNode, expressionNodeType);
		
		this.binopType = operatorType;
		
		setLeftChild(leftChild);
		setRightChild(rightChild);
	}
	
	public BinaryOperatorExpressionNode(ASTNode parentNode, BinaryOperatorType operatorType) {
		this(parentNode, operatorType, null, null);
	}
	
	public BinaryOperatorType getBinaryOperatorType() {
		return binopType;
	}
	
	public void setLeftChild(ExpressionASTNode leftChild) {
		this.leftChild = leftChild;
		
		if (leftChild != null)
			leftChild.updateParentNode(this);
	}
	
	public ExpressionASTNode getLeftChild() {
		return this.leftChild;
	}
	
	public void setRightChild(ExpressionASTNode rightChild) {
		this.rightChild = rightChild;
		
		if (rightChild != null)
			rightChild.updateParentNode(this);
	}
	
	public ExpressionASTNode getRightChild() {
		return this.rightChild;
	}
	
	@Override
	public void addChild(ASTNode child) {
		ExpressionASTNode exprNode = (ExpressionASTNode) child;
		
		if (leftChild == null)
			setLeftChild(exprNode);
		else
			setRightChild(exprNode);
	}
	
	@Override
	public List<ASTNode> getChildren() {
		return Arrays.asList(leftChild, rightChild);
	}
	
	public String toString() {
		return toString(0);
	}
	
	public String toString(int tabs) {
		return String.format(Locale.ENGLISH, "%s%s::%s[%s,\n%sleft = %s,\n%sright = %s]",
					" ".repeat(4 * tabs),
					getNodeType(), getExpressionNodeType(),
					binopType,
					" ".repeat(4 * tabs), leftChild.toString(tabs + 1), 
					" ".repeat(4 * tabs), rightChild.toString(tabs + 1)
				);
	}
	
	public static enum BinaryOperatorType {
		ADD_BINOP(4),
		SUB_BINOP(4),
		MUL_BINOP(5),
		DIV_BINOP(5),
		MOD_BINOP(5),
		
		AND_BINOP(2),
		OR_BINOP(1),
		XOR_BINOP(1),
		
		LT_BINOP(3),
		LE_BINOP(3),
		EQ_BINOP(3),
		NE_BINOP(3),
		GT_BINOP(3),
		GE_BINOP(3),
		
		PROP_ACC_BINOP(Integer.MAX_VALUE),
		ARR_ACC_BINOP(Integer.MAX_VALUE),
		;
		
		public final int priority;
		
		BinaryOperatorType(int priority) {
			this.priority = priority;
		}
	}
}
