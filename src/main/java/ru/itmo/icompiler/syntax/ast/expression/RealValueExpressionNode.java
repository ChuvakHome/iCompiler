package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
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
	
	public String toString() {
		return String.format(Locale.ENGLISH, "%s::%s{value = %f}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
	
	public VarType inferType(SemanticContext ctx) throws SemanticException {
		return VarType.REAL_PRIMITIVE_TYPE;
	}
}
