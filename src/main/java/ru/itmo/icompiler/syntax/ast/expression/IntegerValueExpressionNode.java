package ru.itmo.icompiler.syntax.ast.expression;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class IntegerValueExpressionNode extends ExpressionASTNode {
	private int value;
	
	public IntegerValueExpressionNode(ASTNode parentNode, Token startToken, int value) {
		super(parentNode, startToken, ExpressionNodeType.INTEGER_VALUE_EXPR_NODE);
		
		this.value = value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public String toString() {
		return String.format("%s::%s{value = %d}",
					getNodeType(), getExpressionNodeType(),
					value
				);
	}
	
	public VarType inferType(SemanticContext ctx) throws SemanticException {
		return VarType.INTEGER_PRIMITIVE_TYPE;
	}
}
