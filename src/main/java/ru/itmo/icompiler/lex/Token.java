package ru.itmo.icompiler.lex;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class Token {
	public final int lineNumber;
	public final int lineOffset;
	public final TokenType type; 
	public final String text;
	
	public Token(int lineNumber, int lineOffset, TokenType type, String text) {
		this.lineNumber = lineNumber;
		this.lineOffset = lineOffset;
		this.type = type;
		this.text = text;
	}
	
	public static enum TokenType {
		// Keywords
		
		VAR_KEYWORD,
		TYPE_KEYWORD,
		
		ROUTINE_KEYWORD,
		RETURN_KEYWORD,
		
		IF_KEYWORD,
		THEN_KEYWORD,
		ELSE_KEYWORD,
		
		WHILE_KEYWORD,
		
		FOR_KEYWORD,
		REVERSE_KEYWORD,
		IN_KEYWORD,
		
		BREAK_KEYWORD,
		
		LOOP_KEYWORD,
		
		IS_KEYWORD,
		END_KEYWORD,
		
		// Types
		
		BOOLEAN_KEYWORD,
		INTEGER_KEYWORD,
		REAL_KEYWORD,
		
		ARRAY_KEYWORD,
		RECORD_KEYWORD,
		
		// Brackets
		
		LEFT_BRACKET,
		RIGHT_BRACKET,
		
		LEFT_PARENTHESIS,
		RIGHT_PARENTHESIS,
		
		WHITESPACE,
		
		// Literals
		
		TRUE_BOOLEAN_LITERAL,
		FALSE_BOOLEAN_LITERAL,
		INTEGER_NUMERIC_LITERAL,
		REAL_NUMERIC_LITERAL,
		
		STRING_LITERAL,
		
		// Ident
		
		IDENTIFIER,
		
		// Operators
		
		PLUS_OPERATOR,
		MINUS_OPERATOR,
		MULTIPLY_OPERATOR,
		DIVIDE_OPERATOR,
		MODULO_OPERATOR,
		
		NOT_OPERATOR,
		
		AND_OPERATOR,
		OR_OPERATOR,
		XOR_OPERATOR,
		
		RANGE_OPERATOR,
		
		ASSIGN_OPERATOR,
		
		PRINT_OPERATOR,
		
		LT_OPERATOR,
		LE_OPERATOR,
		EQ_OPERATOR,
		NE_OPERATOR,
		GT_OPERATOR,
		GE_OPERATOR,
		
		// Special operators
		
		COLON_OPERATOR,
		COMMA_OPERATOR,
		DOT_OPERATOR,
		ROUTINE_EXPRESSION_OPERATOR,
		
		// Delimiters
		
		SEMICOLON_DELIMITER,
		LINE_FEED_DELIMITER,
		
		END_OF_TEXT,
		
		INVALID_TOKEN,
		;
		
		public boolean anyOf(TokenType... expected) {
			return Arrays.stream(expected)
					.anyMatch(this::equals);
		}
		
		public boolean noneOf(TokenType... expected) {
			return Arrays.stream(expected)
						.noneMatch(this::equals);
		}
	}
	
	public static final Map<String, TokenType> TEXT_TOKENS = Collections.unmodifiableMap(Map.ofEntries(
			Map.entry("var", TokenType.VAR_KEYWORD),
			Map.entry("type", TokenType.TYPE_KEYWORD),
			
			Map.entry("routine", TokenType.ROUTINE_KEYWORD),
			Map.entry("return", TokenType.RETURN_KEYWORD),
			
			Map.entry("if", TokenType.IF_KEYWORD),
			Map.entry("then", TokenType.THEN_KEYWORD),
			Map.entry("else", TokenType.ELSE_KEYWORD),
			
			Map.entry("while", TokenType.WHILE_KEYWORD),
			
			Map.entry("for", TokenType.FOR_KEYWORD),
			Map.entry("reverse", TokenType.REVERSE_KEYWORD),
			Map.entry("in", TokenType.IN_KEYWORD),
			
			Map.entry("break", TokenType.BREAK_KEYWORD),
			
			Map.entry("loop", TokenType.LOOP_KEYWORD),
			
			Map.entry("is", TokenType.IS_KEYWORD),
			Map.entry("end", TokenType.END_KEYWORD),
			
			// Types
			
			Map.entry("boolean", TokenType.BOOLEAN_KEYWORD),
			Map.entry("integer", TokenType.INTEGER_KEYWORD),
			Map.entry("real", TokenType.REAL_KEYWORD),
			
			Map.entry("array", TokenType.ARRAY_KEYWORD),
			Map.entry("record", TokenType.RECORD_KEYWORD),
			
			// Brackets
			
			Map.entry("[", TokenType.LEFT_BRACKET),
			Map.entry("]", TokenType.RIGHT_BRACKET),
			
			Map.entry("(", TokenType.LEFT_PARENTHESIS),
			Map.entry(")", TokenType.RIGHT_PARENTHESIS),
			
			// Literals
			
			Map.entry("true", TokenType.TRUE_BOOLEAN_LITERAL),
			Map.entry("false", TokenType.FALSE_BOOLEAN_LITERAL),
			
			// Operators
			
			Map.entry("+", TokenType.PLUS_OPERATOR),
			Map.entry("-", TokenType.MINUS_OPERATOR),
			Map.entry("*", TokenType.MULTIPLY_OPERATOR),
			Map.entry("/", TokenType.DIVIDE_OPERATOR),
			Map.entry("%", TokenType.MODULO_OPERATOR),
			
			Map.entry("not", TokenType.NOT_OPERATOR),
			
			Map.entry("and", TokenType.AND_OPERATOR),
			Map.entry("or", TokenType.OR_OPERATOR),
			Map.entry("xor", TokenType.XOR_OPERATOR),
			
			Map.entry("..", TokenType.RANGE_OPERATOR),
			
			Map.entry(":=", TokenType.ASSIGN_OPERATOR),
			
			Map.entry("print", TokenType.PRINT_OPERATOR),
			
			Map.entry("<", TokenType.LT_OPERATOR),
			Map.entry("<=", TokenType.LE_OPERATOR),
			Map.entry("=", TokenType.EQ_OPERATOR),
			Map.entry("/=", TokenType.NE_OPERATOR),
			Map.entry(">", TokenType.GT_OPERATOR),
			Map.entry(">=", TokenType.GE_OPERATOR),
			
			// Special operators
			
			Map.entry(":", TokenType.COLON_OPERATOR),
			Map.entry(",", TokenType.COMMA_OPERATOR),
			Map.entry(".", TokenType.DOT_OPERATOR),
			Map.entry("=>", TokenType.ROUTINE_EXPRESSION_OPERATOR),
			
			// Terminals ??
			
			Map.entry(";", TokenType.SEMICOLON_DELIMITER),
			Map.entry("\n", TokenType.LINE_FEED_DELIMITER)
	));
	
	public static final Map<TokenType, String> TOKENS_TEXT = Collections.unmodifiableMap(Map.ofEntries(
			Map.entry(TokenType.VAR_KEYWORD, quoted("var")),
			Map.entry(TokenType.TYPE_KEYWORD, quoted("type")),
			
			Map.entry(TokenType.ROUTINE_KEYWORD, quoted("routine")),
			Map.entry(TokenType.RETURN_KEYWORD, quoted("return")),
			
			Map.entry(TokenType.IF_KEYWORD, quoted("if")),
			Map.entry(TokenType.THEN_KEYWORD, quoted("then")),
			Map.entry(TokenType.ELSE_KEYWORD, quoted("else")),
			
			Map.entry(TokenType.WHILE_KEYWORD, quoted("while")),
			
			Map.entry(TokenType.FOR_KEYWORD, quoted("for")),
			Map.entry(TokenType.REVERSE_KEYWORD, quoted("reverse")),
			Map.entry(TokenType.IN_KEYWORD, quoted("in")),
			
			Map.entry(TokenType.BREAK_KEYWORD, quoted("break")),
			
			Map.entry(TokenType.LOOP_KEYWORD, quoted("loop")),
			
			Map.entry(TokenType.IS_KEYWORD, quoted("is")),
			Map.entry(TokenType.END_KEYWORD, quoted("end")),
			
			Map.entry(TokenType.BOOLEAN_KEYWORD, quoted("boolean")),
			Map.entry(TokenType.INTEGER_KEYWORD, quoted("integer")),
			Map.entry(TokenType.REAL_KEYWORD, quoted("real")),
			
			Map.entry(TokenType.ARRAY_KEYWORD, quoted("array")),
			Map.entry(TokenType.RECORD_KEYWORD, quoted("record")),
			
			Map.entry(TokenType.LEFT_BRACKET, quoted("[")),
			Map.entry(TokenType.RIGHT_BRACKET, quoted("]")),
			
			Map.entry(TokenType.LEFT_PARENTHESIS, quoted("(")),
			Map.entry(TokenType.RIGHT_PARENTHESIS, quoted(")")),
			
			Map.entry(TokenType.IDENTIFIER, "identifier"),
			
			Map.entry(TokenType.TRUE_BOOLEAN_LITERAL, quoted("true")),
			Map.entry(TokenType.FALSE_BOOLEAN_LITERAL, quoted("false")),
			
			Map.entry(TokenType.PLUS_OPERATOR, quoted("+")),
			Map.entry(TokenType.MINUS_OPERATOR, quoted("-")),
			Map.entry(TokenType.MULTIPLY_OPERATOR, quoted("*")),
			Map.entry(TokenType.DIVIDE_OPERATOR, quoted("/")),
			Map.entry(TokenType.MODULO_OPERATOR, quoted("%")),
			
			Map.entry(TokenType.NOT_OPERATOR, quoted("not")),
			
			Map.entry(TokenType.AND_OPERATOR, quoted("and")),
			Map.entry(TokenType.OR_OPERATOR, quoted("or")),
			Map.entry(TokenType.XOR_OPERATOR, quoted("xor")),
			
			Map.entry(TokenType.RANGE_OPERATOR, quoted("..")),
			
			Map.entry(TokenType.ASSIGN_OPERATOR, quoted(":=")),
			
			Map.entry(TokenType.PRINT_OPERATOR, quoted("print")),
			
			Map.entry(TokenType.LT_OPERATOR, quoted("<")),
			Map.entry(TokenType.LE_OPERATOR, quoted("<=")),
			Map.entry(TokenType.EQ_OPERATOR, quoted("=")),
			Map.entry(TokenType.NE_OPERATOR, quoted("/=")),
			Map.entry(TokenType.GT_OPERATOR, quoted(">")),
			Map.entry(TokenType.GE_OPERATOR, quoted(">=")),
			
			Map.entry(TokenType.COLON_OPERATOR, quoted(":")),
			Map.entry(TokenType.COMMA_OPERATOR, quoted(",")),
			Map.entry(TokenType.DOT_OPERATOR, quoted(".")),
			Map.entry(TokenType.ROUTINE_EXPRESSION_OPERATOR, quoted("=>")),
			
			Map.entry(TokenType.SEMICOLON_DELIMITER, quoted(";")),
			Map.entry(TokenType.LINE_FEED_DELIMITER, "LF")
	));
	
	public static String quoted(String s) {
		return '"' + s + '"';
	}
	
	public boolean equals(Object o) {
		if (o instanceof Token) {
			Token t = (Token) o;
			
			return lineNumber == t.lineNumber && lineOffset == t.lineOffset && type == t.type && text.equals(t.text);
		}
		
		return false;
	}
	
	public String toString() {
		return String.format("[type = %s, %d:%d, \"%s\"]", 
					type, 
					lineNumber, lineOffset,
					text
				);
	}
}
