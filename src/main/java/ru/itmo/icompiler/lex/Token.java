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
	
	public String toString() {
		return String.format("[type = %s, %d:%d, \"%s\"]", 
					type, 
					lineNumber, lineOffset,
					text
				);
	}
}
