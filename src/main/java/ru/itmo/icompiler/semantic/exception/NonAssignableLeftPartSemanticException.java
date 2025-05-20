package ru.itmo.icompiler.semantic.exception;

public class NonAssignableLeftPartSemanticException extends SemanticException {
	public NonAssignableLeftPartSemanticException(int errorLine, int errorOffset) {
		super("left hand-side of the assignment should be lvalue", errorLine, errorOffset);
	}
}
