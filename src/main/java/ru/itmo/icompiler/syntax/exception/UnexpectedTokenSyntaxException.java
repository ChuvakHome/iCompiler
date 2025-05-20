package ru.itmo.icompiler.syntax.exception;

import java.util.Arrays;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.lex.Token.TokenType;

public class UnexpectedTokenSyntaxException extends SyntaxException {
	private Token tok;
	
	public UnexpectedTokenSyntaxException(String message, int[] lines, Token tok) {
		super(message, lines, tok.lineNumber, tok.lineOffset);
		this.tok = tok;
	}
	
	public UnexpectedTokenSyntaxException(String message, Token tok) {
		this(message, new int[] { tok.lineNumber }, tok);
	}
	
	public UnexpectedTokenSyntaxException(int[] lines, Token tok, TokenType... expected) {
		this(
			expected != null && expected.length > 0
			? String.format(
					"unexpected token: expected %s, got \"%s\"", 
					String.join(
							", ", 
							Arrays.stream(expected).map(Token.TOKENS_TEXT::get).toList()), 
					tok.text
				)
			: String.format(
					"unexpected token: \"%s\"", tok.text
				),
			lines,
			tok
		);
	}
	
	public UnexpectedTokenSyntaxException(Token tok, TokenType... expected) {
		this(new int[] {tok.lineNumber}, tok, expected);
	}
	
	public Token getToken() {
		return tok;
	}
	
	public static class UnexpectedEndOfTextSyntaxException extends UnexpectedTokenSyntaxException {
		public UnexpectedEndOfTextSyntaxException(int line, int offset) {
			super("unexpected end of text", new int[]{ line }, new Token(line, offset, TokenType.END_OF_TEXT, ""));
		}
	}
}
