package ru.itmo.icompiler.codegen.jvm;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class JVMBytecodeInstruction extends JVMBytecodeEntity {
	private String label;
	protected String opcode;
	private String[] args;
	
	public JVMBytecodeInstruction(String opcode, Object... args) {
		this.opcode = opcode;
		this.args = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
	}

	public String getOpcode() {
		return opcode;
	}

	public String getArg(int i) {
		return args[i];
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
	
	public static class LookupSwitchInstruction extends JVMBytecodeInstruction {
		private Map<Integer, String> switchesMap;
		private String defaultLabel;
		
		public LookupSwitchInstruction(Map<Integer, String> switches, String defaultLabel) {
			super("lookupswitch");
			
			this.switchesMap = new LinkedHashMap<>(switches);
			this.defaultLabel = defaultLabel;
		}
		
		public LookupSwitchInstruction(String defaultLabel) {
			this(Map.of(), defaultLabel);
		}
		
		public void addSwitch(int value, String dest) {
			switchesMap.put(value, dest);
		}
		
		@Override
		public String toString(int ident) {
			StringBuilder sb = new StringBuilder(" ".repeat(ident)).append(this.opcode);
			
			switchesMap.forEach((value, dest) -> {
				sb.append("\n\t")
				.append(value)
				.append(": ")
				.append(dest);
			});
			sb.append("\n\tdefault: ").append(defaultLabel);
			
			return sb.toString();
		}
	}
}
