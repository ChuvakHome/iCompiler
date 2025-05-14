package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class UnaryOperatorExpressionNode extends ExpressionASTNode {
	private UnaryOperatorType unopType;
	private ExpressionASTNode value;
	
	public UnaryOperatorExpressionNode(ASTNode parentNode, Token startToken, UnaryOperatorType operatorType, ExpressionASTNode value) {
		super(parentNode, startToken, ExpressionNodeType.UNOP_EXPR_NODE);
		
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
	
	@Override
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg); 
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
	
	@Override
	public void validate(SemanticContext ctx) throws CompilerException {
		value.validate(ctx);
		
		switch (unopType) {
			case NOT_BINOP:
				value.checkType(ctx, VarType.BOOLEAN_PRIMITIVE_TYPE);
				break;
			default:
				value.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE, VarType.REAL_PRIMITIVE_TYPE);
				break;
		}
	}
	
	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		switch (unopType) {
			case NOT_BINOP:
				return VarType.BOOLEAN_PRIMITIVE_TYPE;
			default:
				return value.inferType(ctx);
		}
	}

	public static enum UnaryOperatorType {
		PLUS_BINOP,
		MINUS_BINOP,
		NOT_BINOP,
	}
}
