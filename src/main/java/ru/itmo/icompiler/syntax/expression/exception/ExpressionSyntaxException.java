package ru.itmo.icompiler.syntax.expression.exception;

import ru.itmo.icompiler.syntax.exception.SyntaxException;

public class ExpressionSyntaxException extends SyntaxException {
	public ExpressionSyntaxException(String message, int[] enclosingLines, int errorLine, int errorOffset) {
		super(message, enclosingLines, errorLine, errorOffset);
	}

	public ExpressionSyntaxException(String message, int errorLine, int errorOffset) {
		super(message, errorLine, errorOffset);
	}
}
