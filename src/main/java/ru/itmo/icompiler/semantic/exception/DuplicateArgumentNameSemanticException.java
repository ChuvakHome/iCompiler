package ru.itmo.icompiler.semantic.exception;

public class DuplicateArgumentNameSemanticException extends SemanticException {
	private String argName;
	
	public DuplicateArgumentNameSemanticException(String argName, int errorLine, int errorOffset) {
		super("duplicate argument name: " + argName, errorLine, errorOffset);
		
		this.argName = argName;
	}
	
	public String getArgumentName() {
		return argName;
	}
}
