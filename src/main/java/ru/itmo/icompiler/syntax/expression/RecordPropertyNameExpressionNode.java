package ru.itmo.icompiler.syntax.expression;

import java.util.Locale;

import ru.itmo.icompiler.syntax.ASTNode;

public class RecordPropertyNameExpressionNode extends ExpressionASTNode {
	private String propertyName;
	
	public RecordPropertyNameExpressionNode(ASTNode parentNode, String propertyName) {
		super(parentNode, ExpressionNodeType.RECORD_PROPERTY_NAME_EXPR_NODE);
		
		this.propertyName = propertyName;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public String toString(int tabs) {
		return String.format(Locale.ENGLISH, "%s%s::%s[\n%sprop = \"%s\"]",
					" ".repeat(4 * tabs),
					getNodeType(), getExpressionNodeType(), 
					" ".repeat(4 * tabs), propertyName
				);
	}
}
