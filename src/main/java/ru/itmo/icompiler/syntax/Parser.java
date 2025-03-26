package ru.itmo.icompiler.syntax;

import java.util.List;

import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.exception.SyntaxException;

public interface Parser {
	List<SyntaxException> getSyntaxErrors();
	
	void printErrors();
	
	ASTNode parse();
	
	boolean isEndReached();
}
