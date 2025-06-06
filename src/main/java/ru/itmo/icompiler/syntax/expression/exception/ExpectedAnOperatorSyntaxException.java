package ru.itmo.icompiler.syntax.expression.exception;

import ru.itmo.icompiler.lex.Token;

public class ExpectedAnOperatorSyntaxException extends ExpressionSyntaxException {
	private Token token;
	
	public ExpectedAnOperatorSyntaxException(int[] enclosingLines, Token token) {
		super(
			String.format(
				"expected an operator, got \"%s\"",
				token.text
			), 
			enclosingLines,
			token.lineNumber,
			token.lineOffset
		);
		
		this.token = token;
	}
	
	public ExpectedAnOperatorSyntaxException(Token token) {
		this(new int[] { token.lineNumber }, token);
	}
	
	public Token getToken() {
		return token;
	}
}
