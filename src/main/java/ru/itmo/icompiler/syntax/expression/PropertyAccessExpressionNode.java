package ru.itmo.icompiler.syntax.expression;

import java.util.Locale;

import ru.itmo.icompiler.syntax.ASTNode;

public class PropertyAccessExpressionNode extends BinaryOperatorExpressionNode {
	private ExpressionASTNode propertyHolder;
	private String propertyName;
	
	public PropertyAccessExpressionNode(ASTNode parentNode, ExpressionASTNode propertyHolder, String propertyName) {
		super(parentNode, ExpressionNodeType.PROPERTY_ACCESS_EXPR_NODE, BinaryOperatorType.PROP_ACC_BINOP,
					propertyHolder,
					new RecordPropertyNameExpressionNode(null, propertyName)
				);
		
		this.propertyHolder = propertyHolder;
		this.propertyName = propertyName;
	}
	
	public ExpressionASTNode getPropertyHolder() {
		return propertyHolder;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
//	public String toString() {
//		return String.format("%s::%s[holder = %s, prop = %s]",
//					getNodeType(), getExpressionNodeType(),
//					propertyHolder, propertyName
//				);
//	}
	
	public String toString(int tabs) {
		return String.format(Locale.ENGLISH, "%s%s::%s[\n%sholder = %s,\n%sprop = %s]",
					" ".repeat(4 * tabs),
					getNodeType(), getExpressionNodeType(),
					" ".repeat(4 * tabs), propertyHolder.toString(tabs + 1), 
					" ".repeat(4 * tabs), propertyName
				);
	}
}
