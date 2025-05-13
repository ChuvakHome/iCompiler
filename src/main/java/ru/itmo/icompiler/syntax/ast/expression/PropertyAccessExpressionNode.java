package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.RecordType;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.VarType.Tag;
import ru.itmo.icompiler.semantic.exception.IllegalPropertyAccessSemanticException;
import ru.itmo.icompiler.semantic.exception.NoSuchPropertySemanticException;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class PropertyAccessExpressionNode extends ExpressionASTNode {
	private ExpressionASTNode propertyHolder;
	private String propertyName;
	
	public PropertyAccessExpressionNode(ASTNode parentNode, Token opToken, ExpressionASTNode propertyHolder, Token propertyNameToken) {
		super(parentNode, opToken, ExpressionNodeType.PROPERTY_ACCESS_EXPR_NODE);
		
		this.propertyHolder = propertyHolder;
		this.propertyName = propertyNameToken.text;
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
	
	@Override
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg); 
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(Locale.ENGLISH, "%s::%s[%sholder = %s,%sprop = %s]",
					getNodeType(), getExpressionNodeType(),
					sep, propertyHolder.toString(tabs + 1), 
					sep, propertyName
				);
	}
	
	@Override
	public void validate(SemanticContext ctx) throws CompilerException {
		propertyHolder.validate(ctx);
		
		VarType type = propertyHolder.doTypeInference(ctx);
		
		Token tk = getStartToken();
		
		SemanticException e = new IllegalPropertyAccessSemanticException(type, tk.lineNumber, tk.lineOffset);
		
		if (type.getTag() == VarType.Tag.RECORD) {
			RecordType recordType = (RecordType) type;
			
			if (!recordType.hasProperty(propertyName))
				throw new NoSuchPropertySemanticException(recordType, propertyName, tk.lineNumber, tk.lineOffset);
		} else if (type.getTag() != VarType.Tag.ARRAY || !"length".equals(propertyName))
			throw e;
	}

	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		VarType rawType = propertyHolder.doTypeInference(ctx);
		
		if (rawType.getTag() == Tag.ARRAY && "length".equals(propertyName))
			return VarType.INTEGER_PRIMITIVE_TYPE;
		
		RecordType recordType = (RecordType) rawType;
		
		return recordType.getPropertyType(propertyName);
	}
}
