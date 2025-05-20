package ru.itmo.icompiler.syntax.exception;

public class VariableDeclWithoutTypeSyntaxException extends SyntaxException {
	public VariableDeclWithoutTypeSyntaxException(int[] enclosingLines, int errorLine, int errorOffset) {
		super("variable declaration with no assignment should have type annotation", enclosingLines, errorLine, errorOffset);
	}
	
	public VariableDeclWithoutTypeSyntaxException(int errorLine, int errorOffset) {
		this(new int[] { errorLine }, errorLine, errorOffset);
	}
}
