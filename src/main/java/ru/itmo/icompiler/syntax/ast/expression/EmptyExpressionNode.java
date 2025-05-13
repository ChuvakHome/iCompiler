package ru.itmo.icompiler.syntax.ast.expression;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class EmptyExpressionNode extends ExpressionASTNode {
	public EmptyExpressionNode(ASTNode parentNode, Token startToken) {
		super(parentNode, startToken, ExpressionNodeType.EMPTY_EXPR_NODE);
	}

	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		return VarType.VOID_TYPE;
	}

	@Override
	public <R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
