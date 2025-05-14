package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.IllegalArrayAccessSemanticException;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class ArrayAcessExpressionNode extends ExpressionASTNode {
	private ExpressionASTNode holder;
	private ExpressionASTNode index;
	
	public ArrayAcessExpressionNode(ASTNode parent, Token tok, ExpressionASTNode holder, ExpressionASTNode index) {
		super(parent, tok, ExpressionNodeType.ARRAY_ACCESS_EXPR_NODE);
		
		this.holder = holder;
		this.index = index;
	}
	
	public ExpressionASTNode getHolder() {
		return holder;
	}
	
	public void setIndex(ExpressionASTNode index) {
		this.index = index;
	}
	
	public ExpressionASTNode getIndex() {
		return index;
	}
	
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return null;
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(Locale.ENGLISH, "%s::%s[%sholder = %s,%sindex = %s]",
					getNodeType(), getExpressionNodeType(),
					sep, holder.toString(tabs + 1), 
					sep, index.toString(tabs + 1)
				);
	}
	
	@Override
	public void validate(SemanticContext ctx) throws CompilerException {
		holder.validate(ctx);
		
		VarType holderType = holder.doTypeInference(ctx);
		if (holderType.getTag() != VarType.Tag.ARRAY)
			throw new IllegalArrayAccessSemanticException(holderType, getStartToken().lineNumber, getStartToken().lineOffset);	
		
		index.validate(ctx);
		index.checkType(ctx, VarType.INTEGER_PRIMITIVE_TYPE);
	}

	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		return ((ArrayType) holder.doTypeInference(ctx)).getElementType();
	}

	@Override
    public Token getToken() {
        return getStartToken();
    }
}
