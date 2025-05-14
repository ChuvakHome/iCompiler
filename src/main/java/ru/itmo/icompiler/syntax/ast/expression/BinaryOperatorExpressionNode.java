package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class BinaryOperatorExpressionNode extends ExpressionASTNode {
	private BinaryOperatorType binopType;
	private ExpressionASTNode leftChild, rightChild;
	
	public BinaryOperatorExpressionNode(ASTNode parentNode, Token opToken, BinaryOperatorType operatorType, ExpressionASTNode leftChild, ExpressionASTNode rightChild) {
		super(parentNode, opToken, ExpressionNodeType.BINOP_EXPR_NODE);
		
		this.binopType = operatorType;
		
		setLeftChild(leftChild);
		setRightChild(rightChild);
	}
	
	BinaryOperatorExpressionNode(ASTNode parentNode, Token opToken, ExpressionNodeType expressionNodeType, BinaryOperatorType operatorType, ExpressionASTNode leftChild, ExpressionASTNode rightChild) {
		super(parentNode, opToken, expressionNodeType);
		
		this.binopType = operatorType;
		
		setLeftChild(leftChild);
		setRightChild(rightChild);
	}
	
	public BinaryOperatorExpressionNode(ASTNode parentNode, Token opToken, BinaryOperatorType operatorType) {
		this(parentNode, opToken, operatorType, null, null);
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
	
	@Override
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg); 
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(Locale.ENGLISH, "%s::%s[%stype = %s,%sleft = %s,%sright = %s]",
					getNodeType(), getExpressionNodeType(),
					sep, binopType,
					sep, leftChild.toString(tabs + 1), 
					sep, rightChild.toString(tabs + 1)
				);
	}
	
	private void validateLogicalBinop(SemanticContext ctx) throws CompilerException {
		leftChild.checkType(ctx, VarType.BOOLEAN_PRIMITIVE_TYPE);
		rightChild.checkType(ctx, VarType.BOOLEAN_PRIMITIVE_TYPE);
	}
	
	private void validateComparisonBinop(SemanticContext ctx) throws CompilerException {
		leftChild.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE, VarType.REAL_PRIMITIVE_TYPE);
		rightChild.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE, VarType.REAL_PRIMITIVE_TYPE);
	}
	
	private void validateArithmeticBinop(SemanticContext ctx) throws CompilerException {
		leftChild.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE, VarType.REAL_PRIMITIVE_TYPE);
		rightChild.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE, VarType.REAL_PRIMITIVE_TYPE);
	}
	
	@Override
	public void validate(SemanticContext ctx) throws CompilerException {
		leftChild.validate(ctx);
		rightChild.validate(ctx);
		
		switch (binopType) {
			case XOR_BINOP:
			case OR_BINOP:
			case AND_BINOP:
				validateLogicalBinop(ctx);	
				break;
			case LT_BINOP:
			case LE_BINOP:
			case EQ_BINOP:
			case NE_BINOP:
			case GT_BINOP:
			case GE_BINOP:
				validateComparisonBinop(ctx);
				break;
			default:
				validateArithmeticBinop(ctx);
				break;
		}
	}
	
	private VarType inferTypeForLogicalBinop(SemanticContext ctx) throws SemanticException {
		return VarType.BOOLEAN_PRIMITIVE_TYPE;
	}
	
	private VarType inferTypeForComparisonBinop(SemanticContext ctx) throws SemanticException {
		return VarType.BOOLEAN_PRIMITIVE_TYPE;
	}
	
	private VarType inferTypeForArithmeticBinop(SemanticContext ctx) throws SemanticException {
		VarType leftType = leftChild.doTypeInference(ctx);
		VarType rightType = rightChild.doTypeInference(ctx);
		
		if (leftType.equals(VarType.REAL_PRIMITIVE_TYPE) || rightType.equals(VarType.REAL_PRIMITIVE_TYPE))
			return VarType.REAL_PRIMITIVE_TYPE;
		else
			return VarType.INTEGER_PRIMITIVE_TYPE;
	}

	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		switch (binopType) {
			case XOR_BINOP:
			case OR_BINOP:
			case AND_BINOP:
				return inferTypeForLogicalBinop(ctx);
			case LT_BINOP:
			case LE_BINOP:
			case EQ_BINOP:
			case NE_BINOP:
			case GT_BINOP:
			case GE_BINOP:
				return inferTypeForComparisonBinop(ctx);
			default:
				return inferTypeForArithmeticBinop(ctx);
		}
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
		;
		
		public final int priority;
		
		BinaryOperatorType(int priority) {
			this.priority = priority;
		}
	}

	@Override
    public Token getToken() {
        return getStartToken();
    }
}
