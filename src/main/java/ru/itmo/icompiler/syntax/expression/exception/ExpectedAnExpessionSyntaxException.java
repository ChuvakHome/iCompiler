package ru.itmo.icompiler.syntax.expression.exception;

import ru.itmo.icompiler.syntax.exception.SyntaxException;

public class ExpectedAnExpessionSyntaxException extends ExpressionSyntaxException {
	public ExpectedAnExpessionSyntaxException(int[] enclosingLines, int errorLine, int errorOffset) {
		super("Expected an expression", enclosingLines, errorLine, errorOffset);
	}
	
	public ExpectedAnExpessionSyntaxException(int errorLine, int errorOffset) {
		super("Expected an expression", errorLine, errorOffset);
	}
}
