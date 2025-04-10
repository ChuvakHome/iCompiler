package ru.itmo.icompiler.semantic.exception;

import ru.itmo.icompiler.semantic.VarType;

public class IllegalArrayAccessSemanticException extends SemanticException {
	private VarType nonArrayType;
	
	public IllegalArrayAccessSemanticException(VarType nonRecordType, int errorLine, int errorOffset) {
		super("illegal array access for non-array type " + nonRecordType, errorLine, errorOffset);
		
		this.nonArrayType = nonRecordType;
	}
	
	public VarType getNonArrayType() {
		return nonArrayType;
	}
}
