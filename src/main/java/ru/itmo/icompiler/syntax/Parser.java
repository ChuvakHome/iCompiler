package ru.itmo.icompiler.syntax;

import java.util.List;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public interface Parser {
	List<CompilerException> getParseErrors();
	
	ASTNode parse();
	
	boolean isEndReached();
}
