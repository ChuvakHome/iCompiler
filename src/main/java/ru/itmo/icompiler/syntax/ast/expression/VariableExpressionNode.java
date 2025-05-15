package ru.itmo.icompiler.syntax.ast.expression;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemUtils;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.SemanticException;
import ru.itmo.icompiler.semantic.exception.UndefinedVariableSemanticException;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class VariableExpressionNode extends ExpressionASTNode {
	private String variable;
	
	public VariableExpressionNode(ASTNode parentNode, Token token) {
		super(parentNode, token, ExpressionNodeType.VARIABLE_EXPR_NODE);
		
		this.variable = token.text;
	}
	
	public String getVariable() {
		return variable;
	}
	
	@Override
	public<R, A> R accept(ExpressionNodeVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg); 
	}
	
	public String toString() {
		return String.format("%s::%s{var = %s}",
					getNodeType(), getExpressionNodeType(),
					variable
				);
	}
	
	public void validate(SemanticContext ctx) throws SemanticException {
		SemUtils.checkEntity(variable, ctx, true, new UndefinedVariableSemanticException(variable, getStartToken().lineNumber, getStartToken().lineOffset));
	}
	
	@Override
	protected VarType doTypeInference(SemanticContext ctx) throws SemanticException {
		VarType varType = ctx.getScope().deepLookupEntity(variable);
			
		return varType;
	}

	@Override
    public Token getToken() {
        return getStartToken();
    }
}
