package ru.itmo.icompiler.semantic.exception;

public class UndefinedVariableSemanticException extends SemanticException {
	private String variableName;
	
	public UndefinedVariableSemanticException(String variableName, int errorLine, int errorOffset) {
		super(String.format("undefined variable %s", variableName), errorLine, errorOffset);
		
		this.variableName = variableName;
	}
	
	public String getVariableName() {
		return variableName;
	}
}
