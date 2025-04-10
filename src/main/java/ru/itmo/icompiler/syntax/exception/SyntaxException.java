package ru.itmo.icompiler.syntax.exception;

import ru.itmo.icompiler.exception.CompilerException;

public class SyntaxException extends CompilerException {
	public SyntaxException(String message, int[] enclosingLines, int errorLine, int errorOffset) {
		super("Syntax error", message, enclosingLines, errorLine, errorOffset);
	}
	
	public SyntaxException(String message, int errorLine, int errorOffset) {
		super(message, new int[] { errorLine }, errorLine, errorOffset);
	}
}
