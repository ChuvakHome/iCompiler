package ru.itmo.icompiler.semantic.exception;

import ru.itmo.icompiler.semantic.VarType;

public class IllegalPropertyAccessSemanticException extends SemanticException {
	private VarType nonRecordType;
	
	public IllegalPropertyAccessSemanticException(VarType nonRecordType, int errorLine, int errorOffset) {
		super("illegal property access for non-record type " + nonRecordType, errorLine, errorOffset);
		
		this.nonRecordType = nonRecordType;
	}
	
	public VarType getNonRecordType() {
		return nonRecordType;
	}
}
