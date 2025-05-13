package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class RealValueExpressionNode extends ExpressionASTNode {
	private float value;
	
	public RealValueExpressionNode(ASTNode parentNode, Token startToken, float value) {
		super(parentNode, startToken, ExpressionNodeType.REAL_VALUE_EXPR_NODE);
		
		this.value = value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	@Override
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg); 
	}
	
	public String toString() {
		return String.format(Locale.ENGLISH, "%s::%s{value = %f}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
	
	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		return VarType.REAL_PRIMITIVE_TYPE;
	}
}
