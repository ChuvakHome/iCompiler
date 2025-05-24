package ru.itmo.icompiler.semantic.exception;

public class RoutineNotDefinedSemanticException extends SemanticException {
	public RoutineNotDefinedSemanticException(String routineName, int errorLine, int errorOffset) {
		super(
			String.format("routine \"%s\" is declared but not defined", routineName), 
			errorLine, 
			errorOffset
		);
	}
}
