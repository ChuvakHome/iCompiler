package ru.itmo.icompiler.codegen.jvm;

import java.util.Arrays;
import java.util.List;

public class JVMBytecodeClass extends JVMBytecodeEntity {
	private AccessSpec[] accessSpecs;
	private String sourceName;
	private String className;
	private List<JVMBytecodeField> fields;
	private List<JVMBytecodeMethod> methods;
	
	public JVMBytecodeClass(AccessSpec[] accessSpecs, String sourceName, String className, List<JVMBytecodeField> fields, List<JVMBytecodeMethod> methods) {
		this.accessSpecs = accessSpecs;
		
		this.sourceName = sourceName;
		this.className = className;
		
		this.fields = fields;
		this.methods = methods;
	}
	
	public JVMBytecodeClass(AccessSpec[] accessSpecs, String className, List<JVMBytecodeField> fields, List<JVMBytecodeMethod> methods) {
		this(accessSpecs, className, className, fields, methods);
	}
	
	public AccessSpec[] getAccessSpecs() {
		return accessSpecs;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public List<JVMBytecodeField> getFields() {
		return fields;
	}
	
	public List<JVMBytecodeMethod> getMethods() {
		return methods;
	}

	@Override
	public String toString() {
		return String.format(
				".source %s\n"
				+ ".class %s %s\n"
				+ ".super java/lang/Object\n"
				+ "\n%s\n"
				+ "\n%s",
				
				sourceName,
				String.join(
					" ", 
					Arrays
						.stream(accessSpecs)
						.map(spec -> spec.name().toLowerCase())
						.toList()
				), 
				className,
				
				String.join(
					"\n", 
					fields
						.stream()
						.map(JVMBytecodeField::toString)
						.toList()
				),
				
				String.join(
					"\n\n", 
					methods
						.stream()
						.map(JVMBytecodeMethod::toString)
						.toList()
				)
			);
	}
	
	public static enum AccessSpec {
		PUBLIC,
		FINAL,
		SUPER,
		ABSTRACT,
		INTERFACE,
	}
}
