package ru.itmo.icompiler.codegen.jvm;

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
	
	public String toString(int ident) {
		String labelStr = label != null && !label.isBlank() ? label + ":\n" : "";
		
		if (opcode == null)
			return labelStr.strip();
		
		return String.format(
				"%s%s %s",
				labelStr,
				" ".repeat(ident) + opcode,
				String.join(" ", args)
			);
	}
	
	public String toString() {
		return toString(0);
	}
	
	public static class JVMBytecodeInstructionLabeled extends JVMBytecodeInstruction {
		public JVMBytecodeInstructionLabeled(String label, String opcode, Object... args) {
			super(opcode, args);
			
			setLabel(label);
		}
	}
	
	public static class JVMBytecodeLabel extends JVMBytecodeInstructionLabeled {
		public JVMBytecodeLabel(String label) {
			super(label, null);
		}
	}
}
