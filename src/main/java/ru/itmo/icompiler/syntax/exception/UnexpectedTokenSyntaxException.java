package ru.itmo.icompiler.syntax.exception;

import java.util.Arrays;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.lex.Token.TokenType;

public class UnexpectedTokenSyntaxException extends SyntaxException {
	private Token tok;
	
	public UnexpectedTokenSyntaxException(String message, int[] lines, Token tok, TokenType... expected) {
		super(message, lines, tok.lineNumber, tok.lineOffset);
		this.tok = tok;
	}
	
	public UnexpectedTokenSyntaxException(int[] lines, Token tok, TokenType... expected) {
		this(
			String.format(
					"Unexpected token: expected %s, got \"%s\"", 
					String.join(
							", ", 
							Arrays.stream(expected).map(Token.TOKENS_TEXT::get).toList()), 
					tok.text
				),
			lines,
			tok,
			expected
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
			super("Unexpected end of text", new int[]{ line }, new Token(line, offset, TokenType.END_OF_TEXT, ""));
		}
	}
}
