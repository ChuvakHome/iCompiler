package ru.itmo.icompiler.semantic.exception;

import ru.itmo.icompiler.semantic.VarType;

public class AttemptToCallNonCallableSemanticException extends SemanticException {
	private VarType nonCallableType;
	
	public AttemptToCallNonCallableSemanticException(VarType nonCallableType, int errorLine, int errorOffset) {
		super("attempt to call non-callable object of type " + nonCallableType, errorLine, errorOffset);
		
		this.nonCallableType = nonCallableType;
	}
	
	public VarType getNonCallableType() {
		return nonCallableType;
	}
}
