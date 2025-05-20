package ru.itmo.icompiler.semantic.exception;

import ru.itmo.icompiler.semantic.VarType;

public class NonFunctionCallSemanticException extends SemanticException {
	private String functionName;
	private VarType type; 
	
	public NonFunctionCallSemanticException(String functionName, VarType type, int errorLine, int errorOffset) {
		super(String.format("attempt to call non-callable variable %s of type %s", functionName, type), errorLine, errorOffset);
		
		this.functionName = functionName;
		this.type = type;
	}
	
	public String getFunctionName() {
		return functionName;
	}
	
	public VarType getType() {
		return type;
	}
}
