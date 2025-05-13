package ru.itmo.icompiler.syntax.ast.expression;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class BooleanValueExpressionNode extends ExpressionASTNode {
	private boolean value;
	
	public BooleanValueExpressionNode(ASTNode parentNode, Token startToken, boolean value) {
		super(parentNode, startToken, ExpressionNodeType.BOOLEAN_VALUE_EXPR_NODE);
		
		this.value = value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
	
	public String toString() {
		return String.format("%s::%s{value = %s}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
	
	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		return VarType.BOOLEAN_PRIMITIVE_TYPE;
	}
}
