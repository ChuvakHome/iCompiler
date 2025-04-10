package ru.itmo.icompiler.semantic.exception;

import ru.itmo.icompiler.semantic.RecordType;

public class NoSuchPropertySemanticException extends SemanticException {
	private RecordType recordType;
	private String property;
	
	public NoSuchPropertySemanticException(RecordType recordType, String property, int errorLine, int errorOffset) {
		super(
			String.format(
					"no property '%s' in record of type %s", 
					property, recordType
			), 
			errorLine,
			errorOffset
		);
		
		this.recordType = recordType;
		this.property = property;
	}
	
	public RecordType getRecordType() {
		return recordType;
	}
	
	public String getProperty() {
		return property;
	}
}
