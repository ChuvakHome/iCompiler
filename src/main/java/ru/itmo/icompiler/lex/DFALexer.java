package ru.itmo.icompiler.lex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Predicate;

import ru.itmo.compiler.reader.TextReader;
import ru.itmo.icompiler.lex.Token.TokenType;

public class DFALexer implements Lexer {
	private static enum DFALexerState {
		INIT_STATE,
		STRING_OR_IDENTIFIER_STATE,
		NUMERIC_LITERAL_STATE,
		REAL_NUMERIC_LITERAL_STATE,
		OPERATOR_STATE,
		TERMINAL_STATE,
	}
	
	private TextReader reader;
	private DFALexerState state;
	private int lineNumber = 1, lineOffset;
	private Token currentToken;
	
	public DFALexer(InputStream in) {
		reader = new TextReader(in);
	}
	
	public DFALexer(File file) throws FileNotFoundException {
		reader = new TextReader(file);
	}
	
	public DFALexer(String text) {
		reader = new TextReader(text);
	}
	
	@Override
	public Token lookupToken(Predicate<Token> p) {
		while (currentToken == null || !p.test(currentToken))
			currentToken = doLookupToken();
		
		return currentToken;
	}
	
	@Override
	public Token lookupToken() {
		return lookupToken(LexUtils::truePredicate);
	}
	
	private Token doLookupToken() {
		StringBuilder tokenTextSB = new StringBuilder();
		TokenType tokType = TokenType.INVALID_TOKEN;
		
		int startLineNumber = 0, startLineOffset = 0;
		
		state = DFALexerState.INIT_STATE;
		
		while (!reader.isEndReached() && state != DFALexerState.TERMINAL_STATE) {
			switch (state) {
				case INIT_STATE: {
					char ch = reader.nextChar();
					++lineOffset;
					
					DFALexerState oldState = state;
					
					if (Character.isAlphabetic(ch) || ch == '_')
						state = DFALexerState.STRING_OR_IDENTIFIER_STATE;
					else if (Character.isDigit(ch))
						state = DFALexerState.NUMERIC_LITERAL_STATE;
					else if (ch == '\n') {
						startLineNumber = lineNumber++;
						startLineOffset = lineOffset;
						
						lineOffset = 0;
						
						return new Token(
										startLineNumber,
										startLineOffset,
										TokenType.LINE_FEED_DELIMITER,
										"\n"
									);
					} else if (Character.isWhitespace(ch)) {
						return new Token(
										lineNumber,
										lineOffset,
										TokenType.WHITESPACE,
										String.valueOf(ch)
									);
					} else {
						boolean jumpOpState = true;
						
						if (ch == '/') {
							char ch2 = reader.lookupChar();
							
							if (ch2 == '/') {
								jumpOpState = false;
								
								while (reader.lookupChar() != '\n') {
									reader.toNextChar();
									++lineOffset;
								}
							}
						} else if (ch == '.') {
							char ch2 = reader.lookupChar();
							
							if (Character.isDigit(ch2)) {
//								tokenTextSB.append('0');
								state = DFALexerState.REAL_NUMERIC_LITERAL_STATE;
								
								jumpOpState = false;
							}
						}
						
						if (jumpOpState)
							state = DFALexerState.OPERATOR_STATE;
					}
					
					if (oldState != state) {
						startLineNumber = lineNumber;
						startLineOffset = lineOffset;
						
						tokenTextSB.append(ch);
					}
					
					break;
				} case STRING_OR_IDENTIFIER_STATE: {
					char ch = reader.lookupChar();
					
					if (Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_') {
						tokenTextSB.append(ch);
						reader.toNextChar();
						++lineOffset;
					} else {
						state = DFALexerState.TERMINAL_STATE;
						tokType = Token.TEXT_TOKENS.getOrDefault(tokenTextSB.toString(), TokenType.IDENTIFIER);
					}
					
					break;
				} case NUMERIC_LITERAL_STATE: {
					char ch = reader.lookupChar();
					
					if (Character.isDigit(ch)) {
						tokenTextSB.append(ch);
						reader.toNextChar();
						++lineOffset;
					} else if (ch == '.') {
						reader.flush();
						reader.toNextChar();
						
						char ch2 = reader.lookupChar();
						
						if (ch2 == '.') {
							reader.backtrack();
							
							state = DFALexerState.TERMINAL_STATE;
							tokType = TokenType.INTEGER_NUMERIC_LITERAL;
						} else {
							tokenTextSB.append(ch);
							++lineOffset;

							state = DFALexerState.REAL_NUMERIC_LITERAL_STATE;
							tokType = TokenType.REAL_NUMERIC_LITERAL;
						}
					} else {
						state = DFALexerState.TERMINAL_STATE;
						tokType = TokenType.INTEGER_NUMERIC_LITERAL;
					}
					
					break;
				} case REAL_NUMERIC_LITERAL_STATE: {
					char ch = reader.lookupChar();
					
					if (Character.isDigit(ch)) {
						tokenTextSB.append(ch);
						reader.toNextChar();
						++lineOffset;
					} else {
						state = DFALexerState.TERMINAL_STATE;
						tokType = TokenType.REAL_NUMERIC_LITERAL;
					}
					
					break;
				} case OPERATOR_STATE: {
					char ch = reader.lookupChar();
					
					tokenTextSB.append(ch);
					
					TokenType t = Token.TEXT_TOKENS.getOrDefault(tokenTextSB.toString(), TokenType.INVALID_TOKEN);
					
					if (t == TokenType.INVALID_TOKEN) {
						tokenTextSB.deleteCharAt(tokenTextSB.length() - 1);
						
						state = DFALexerState.TERMINAL_STATE;
						tokType = Token.TEXT_TOKENS.getOrDefault(tokenTextSB.toString(), TokenType.INVALID_TOKEN);
					} else {
						tokType = t;
						reader.toNextChar();
						++lineOffset;
					}
					
					break;
				} default: {
					break;
				}
			}
		}
		
		if (state == DFALexerState.TERMINAL_STATE) {
			return new Token(
							startLineNumber, 
							startLineOffset, 
							tokType, 
							tokenTextSB.toString()
						);
		}
	
		switch (state) {
			case STRING_OR_IDENTIFIER_STATE:
				return new Token(
								startLineNumber, 
								startLineOffset, 
								Token.TEXT_TOKENS.getOrDefault(tokenTextSB.toString(), TokenType.IDENTIFIER), 
								tokenTextSB.toString()
							);
			case NUMERIC_LITERAL_STATE:
				return new Token(
								startLineNumber, 
								startLineOffset,
								TokenType.INTEGER_NUMERIC_LITERAL,
								tokenTextSB.toString()
							);
			case REAL_NUMERIC_LITERAL_STATE:
				return new Token(
								startLineNumber, 
								startLineOffset,
								TokenType.REAL_NUMERIC_LITERAL,
								tokenTextSB.toString()
							);
			default:
				return new Token(
								lineNumber,
								lineOffset,
								TokenType.END_OF_TEXT,
								""
							);
		}
	}
	
	@Override
	public void skipToken() {
		currentToken = null; // force lexer to search for next token on next step
	}
	
	@Override
	public Token nextToken(Predicate<Token> p) {
		Token tok = lookupToken(p);
		
		skipToken();
		
		return tok;
	}
	
	@Override
	public Token nextToken() {
		return nextToken(LexUtils::truePredicate);
	}
	
	public boolean isEndReached() {
		return reader.isEndReached();
	}
}
