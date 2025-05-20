package ru.itmo.compiler.codegen.jvm;

import java.util.Arrays;

public class JVMBytecodeField extends JVMBytecodeEntity {
	private AccessSpec[] accessSpecs;
	private String fieldName;
	private String fieldDescriptor;
	
	public JVMBytecodeField(AccessSpec[] accessSpecs, String fieldName, String fieldDescriptor) {
		this.accessSpecs = accessSpecs;
		this.fieldName = fieldName;
		this.fieldDescriptor = fieldDescriptor;
	}
	
	public AccessSpec[] getAccessSpecs() {
		return accessSpecs;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public String getFieldDescriptor() {
		return fieldDescriptor;
	}
	
	@Override
	public String toString() {
		return String.format(
				".field %s %s %s", 
				String.join(
					" ", 
					Arrays
						.stream(accessSpecs)
						.map(spec -> spec.name().toLowerCase())
						.toList()
				), 
				fieldName,
				fieldDescriptor
			);
	}
	
	public static enum AccessSpec {
		PUBLIC,
		PRIVATE,
		PROTECTED,
		STATIC,
		FINAL,
		VOLATILE,
		TRANSIENT,
	}
}
