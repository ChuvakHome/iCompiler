package ru.itmo.icompiler.semantic.exception;

import ru.itmo.icompiler.semantic.VarType;

public class NonIterableInForEachSemanticException extends SemanticException {
	private VarType nonIterableType;
	
	public NonIterableInForEachSemanticException(VarType nonIterableType, int errorLine, int errorOffset) {
		super("expected iterable type, got " + nonIterableType.toString(), errorLine, errorOffset);
		
		this.nonIterableType = nonIterableType;
	}
	
	public VarType getNonIterableType() {
		return nonIterableType;
	}
}
