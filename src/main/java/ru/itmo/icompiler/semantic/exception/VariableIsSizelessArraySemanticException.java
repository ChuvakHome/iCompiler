package ru.itmo.icompiler.semantic.exception;

public class VariableIsSizelessArraySemanticException extends SemanticException {
	public VariableIsSizelessArraySemanticException(int errorLine, int errorOffset) {
		super("sizeless arrays can only be a parameter of a routine", errorLine, errorOffset);
	}
}
