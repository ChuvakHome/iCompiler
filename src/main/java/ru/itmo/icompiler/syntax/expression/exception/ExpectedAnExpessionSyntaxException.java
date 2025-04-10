package ru.itmo.icompiler.syntax.expression.exception;

public class ExpectedAnExpessionSyntaxException extends ExpressionSyntaxException {
	public ExpectedAnExpessionSyntaxException(int[] enclosingLines, int errorLine, int errorOffset) {
		super("expected an expression", enclosingLines, errorLine, errorOffset);
	}
	
	public ExpectedAnExpessionSyntaxException(int errorLine, int errorOffset) {
		super("expected an expression", errorLine, errorOffset);
	}
}
