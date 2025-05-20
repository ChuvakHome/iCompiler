package ru.itmo.icompiler.semantic.exception;

public class UndefinedTypeSemanticException extends SemanticException {
	private String typeName;
	
	public UndefinedTypeSemanticException(String typeName, int errorLine, int errorOffset) {
		super(String.format("undefined type %s", typeName), errorLine, errorOffset);
		
		this.typeName = typeName;
	}
	
	public String getTypeName() {
		return typeName;
	}
}
