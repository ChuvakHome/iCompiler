package ru.itmo.icompiler.semantic.exception;

public class UndefinedRoutineSemanticException extends SemanticException {
	private String variableName;
	
	public UndefinedRoutineSemanticException(String variableName, int errorLine, int errorOffset) {
		super(String.format("Undefined routine %s", variableName), errorLine, errorOffset);
		
		this.variableName = variableName;
	}
	
	public String getVariableName() {
		return variableName;
	}
}
