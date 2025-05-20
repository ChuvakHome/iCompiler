package ru.itmo.icompiler.semantic.exception;

public class TooManyArgumentsForRoutineCallSemanticException extends SemanticException {
	public TooManyArgumentsForRoutineCallSemanticException(int errorLine, int errorOffset) {
		super("Too many arguments to match routine call", errorLine, errorOffset);
	}
}
