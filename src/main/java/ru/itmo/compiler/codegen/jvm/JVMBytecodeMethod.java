package ru.itmo.compiler.codegen.jvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils;

public class JVMBytecodeMethod extends JVMBytecodeEntity {
	private AccessSpec[] accessSpecs;
	private String methodName;
	private List<String> argsDescriptors;
	private String returnTypeDescriptor;
	
	private int localVariablesLimit;
	private int stackLimit;
	
	private List<JVMBytecodeEntity> instructions;
	
	public JVMBytecodeMethod(
			AccessSpec[] accessSpecs, 
			String methodName, 
			List<String> argsDescriptors, 
			String returnTypeDescriptor,
			int localVariablesLimit,
			int stackLimit,
			List<JVMBytecodeEntity> instructions
		) {
			this.accessSpecs = accessSpecs;
			this.methodName = methodName;
			this.argsDescriptors = Collections.unmodifiableList(
						argsDescriptors
							.stream()
							.map(JVMBytecodeUtils::formatDescriptor)
							.toList()
					);
			this.returnTypeDescriptor = JVMBytecodeUtils.formatDescriptor(returnTypeDescriptor);
			
			this.localVariablesLimit = localVariablesLimit;
			this.stackLimit = stackLimit;
			
			this.instructions = instructions;
		}
	
	public JVMBytecodeMethod(
		AccessSpec[] accessSpecs, 
		String methodName, 
		List<String> argsDescriptors, 
		String returnTypeDescriptor,
		int localVariablesLimit,
		int stackLimit
	) {
		this(accessSpecs, methodName, argsDescriptors, returnTypeDescriptor, localVariablesLimit, stackLimit, new ArrayList<>());
	}
	
	public AccessSpec[] getAccessSpecs() {
		return accessSpecs;
	}
	
	public String getMethodName() {
		return methodName;
	}

	public void addBytecodeEntity(JVMBytecodeInstruction instruction) {
		instructions.add(instruction);
	}
	
	public void addBytecodeEntity(Collection<JVMBytecodeInstruction> instructions) {
		this.instructions.addAll(instructions);
	}
	
	@Override
	public String toString() {
		return String.format(
				".method %s %s(%s)%s\n"
				+ ".limit locals %d\n"
				+ ".limit stack %d\n"
				+ "%s\n"
				+ ".end method", 
				String.join(
					" ", 
					Arrays
						.stream(accessSpecs)
						.map(spec -> spec.name().toLowerCase())
						.toList()
				), 
				methodName,
				String.join(
					"", 
					argsDescriptors
				),
				returnTypeDescriptor,
				
				localVariablesLimit,
				stackLimit,
				
				String.join(
					"\n", 
					instructions.stream()
						.map(JVMBytecodeEntity::toString)
						.toList()
				).indent(4)
			);
	}
	
	public static enum AccessSpec {
		PUBLIC,
		PRIVATE,
		PROTECTED,
		STATIC,
		FINAL,
	}
}
