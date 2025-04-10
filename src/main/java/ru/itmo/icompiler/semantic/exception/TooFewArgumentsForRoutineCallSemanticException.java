package ru.itmo.icompiler.semantic.exception;

public class TooFewArgumentsForRoutineCallSemanticException extends SemanticException {
	public TooFewArgumentsForRoutineCallSemanticException(int errorLine, int errorOffset) {
		super("Too few arguments to match routine call", errorLine, errorOffset);
	}
}
