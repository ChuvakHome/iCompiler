package ru.itmo.icompiler.codegen.jvm.visitor;

import ru.itmo.icompiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.IntCounter;

public final class CodeEmitterUtils {
	private CodeEmitterUtils() {}
	
	public static String allocateLabel(IntCounter labelCounter) {
		String label = "L" + labelCounter;
		labelCounter.incCounter();
		
		return label;
	}
	
	public static String getOrAllocateLabel(String label, IntCounter labelCounter) {
		return label != null ? label : allocateLabel(labelCounter);
	}
}
