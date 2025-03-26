package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.syntax.ast.ASTNode;

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
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(Locale.ENGLISH, "%s::%s[%sholder = %s,%sprop = %s]",
					getNodeType(), getExpressionNodeType(),
					sep, propertyHolder.toString(tabs + 1), 
					sep, propertyName
				);
	}
}
