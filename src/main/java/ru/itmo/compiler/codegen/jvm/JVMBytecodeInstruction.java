package ru.itmo.compiler.codegen.jvm;

import java.util.Arrays;

public class JVMBytecodeInstruction extends JVMBytecodeEntity {
	private String label;
	private String opcode;
	private String[] args;
	
	public JVMBytecodeInstruction(String opcode, Object... args) {
		this.opcode = opcode;
		this.args = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String toString() {
		return String.format(
				"%s%s %s", 
				label != null && !label.isBlank() ? label + ": " : "", 
				opcode, 
				String.join(" ", args)
			);
	}
}
