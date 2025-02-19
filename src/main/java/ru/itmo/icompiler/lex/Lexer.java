package ru.itmo.icompiler.lex;

import java.util.function.Predicate;

public interface Lexer {
	Token lookupToken(Predicate<Token> p);
	
	Token lookupToken();
	
	void skipToken();

	Token nextToken(Predicate<Token> p);
	
	Token nextToken();
	
	boolean isEndReached();
}
