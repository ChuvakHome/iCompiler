package ru.itmo.icompiler.syntax.ast.expression;

import java.util.Locale;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class ImplicitCastExpressionNode extends ExpressionASTNode {
	private VarType targetType;
	private ExpressionASTNode argument;
	
	public ImplicitCastExpressionNode(ASTNode parentNode, VarType targetType, ExpressionASTNode argument) {
		super(parentNode, argument.getStartToken(), ExpressionNodeType.IMPLICIT_CAST_EXPR_NODE);
		
		this.argument = argument;
		this.targetType = targetType;
		addChild(argument);
		
		this.setExpressionType(targetType);
	}

	public void validate(SemanticContext ctx) throws CompilerException {
		VarType argType = getArgument().inferType(ctx);
		argType.isConvertibleTo(targetType);
	}
	
	public VarType getTargetType() {
		return targetType;
	}
	
	public ExpressionASTNode getArgument() {
		return argument;
	}
	
	public String toString() {
		return toString(0);
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return String.format(Locale.ENGLISH, "%s::%s[%starget = %s,%sexpr = %s]",
					getNodeType(), getExpressionNodeType(),
					sep, targetType,
					sep, argument.toString(tabs + 1)
				);
	}
	
	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		return targetType;
	}

	@Override
	public <R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	@Override
    public Token getToken() {
        return getStartToken();
    }
}
