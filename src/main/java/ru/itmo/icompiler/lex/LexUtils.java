package ru.itmo.icompiler.lex;

import java.util.function.Predicate;

import ru.itmo.icompiler.lex.Token.TokenType;

public class LexUtils {
	public static boolean truePredicate(Token tk) {
		return true;
	}
	
	public static boolean emptyPredicate(Token tk) {
		return false;
	}
	
	public static boolean isWhitespace(Token tk) {
		return tk.type == TokenType.WHITESPACE;
	}
	
	public static boolean isDelimeter(Token tk) {
		return tk.type.anyOf(TokenType.LINE_FEED_DELIMITER, TokenType.SEMICOLON_DELIMITER);
	}
	
	public static Predicate<Token> isTypeAnyOf(TokenType... types) {
		return tk -> tk.type.anyOf(types);
	}
	
	public static Predicate<Token> isTypeNoneOf(TokenType... types) {
		return tk -> tk.type.noneOf(types);
	}
	
	public static String tabToSpaces(int tabWidth) {
		return " ".repeat(Math.max(tabWidth, 1));
	}
}
