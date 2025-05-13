package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Arrays;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.exception.UnexpectedTypeSemanticException;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public abstract class ExpressionASTNode extends ASTNode {
	private Token startToken;
	private ExpressionNodeType exprNodeType;
	private VarType exprType;
	
	public ExpressionASTNode(ASTNode parentNode, Token startToken, ExpressionNodeType exprNodeType) {
		super(parentNode, ASTNodeType.EXPRESSION_NODE);
		this.startToken = startToken;
		this.exprNodeType = exprNodeType;
	}
	
	public Token getStartToken() {
		return startToken;
	}
	
	public ExpressionNodeType getExpressionNodeType() {		
		return exprNodeType;
	}
	
	protected void updateParentNode(ASTNode node) {
		super.setParentNode(node);
	}
	
	protected void setExpressionType(VarType exprType) {
		this.exprType = exprType;
	}
	
	public VarType getExpressionType() {
		return this.exprType;
	}
	
	public String toString() {
		return String.format("%s[exprType = %s]",
					getNodeType(),
					exprNodeType
				);
	}
	
	public void checkType(SemanticContext ctx, VarType... expectedTypes) throws SemanticException {
		VarType actualType = inferType(ctx); 
		
		if (Arrays.stream(expectedTypes).noneMatch(actualType::isConvertibleTo))
			throw new UnexpectedTypeSemanticException(
					Arrays.asList(expectedTypes), 
					actualType, 
					getStartToken().lineNumber,
					getStartToken().lineOffset
				);
	}
	
	protected abstract VarType doTypeInference(SemanticContext ctx) throws SemanticException;
	
	public VarType inferType(SemanticContext ctx) throws SemanticException {
		if (exprType == null)
			exprType = doTypeInference(ctx);
		
		return exprType;
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
	
	public abstract<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg);
	
	public static enum ExpressionNodeType {
		IMPLICIT_CAST_EXPR_NODE,
		
		BOOLEAN_VALUE_EXPR_NODE,
		INTEGER_VALUE_EXPR_NODE,
		REAL_VALUE_EXPR_NODE,
		VARIABLE_EXPR_NODE,
		UNOP_EXPR_NODE,
		BINOP_EXPR_NODE,
		ARRAY_ACCESS_EXPR_NODE,
		FUN_CALL_EXPR_NODE,
		PROPERTY_ACCESS_EXPR_NODE,
		RECORD_PROPERTY_NAME_EXPR_NODE,
		
		EMPTY_EXPR_NODE
	}
}
