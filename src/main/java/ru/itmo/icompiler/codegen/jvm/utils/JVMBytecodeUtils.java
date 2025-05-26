package ru.itmo.icompiler.codegen.jvm.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.itmo.icompiler.codegen.jvm.JVMBytecodeClass;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeEntity;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeField;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeInstruction;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeMethod;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.RecordType;
import ru.itmo.icompiler.semantic.VarType;

public final class JVMBytecodeUtils {
	public static final Map<VarType, String> PRIMITIVE_TYPE_OPCODE_MAPPER = Map.ofEntries(
			Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "i"),
			Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "i"),
			Map.entry(VarType.REAL_PRIMITIVE_TYPE, "f"),
			
			Map.entry(VarType.VOID_TYPE, "")
		);
	
	public static final Map<VarType, String> PRIMITIVE_TYPENAME_MAPPER = Map.ofEntries(
			Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "boolean"),
			Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "int"),
			Map.entry(VarType.REAL_PRIMITIVE_TYPE, "float"),
			
			Map.entry(VarType.VOID_TYPE, "void")
		);
	
	public static final Map<VarType, String> PRIMITIVE_TYPE_DESCRIPTOR_MAPPER = Map.ofEntries(
		Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "Z"),
		Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "I"),
		Map.entry(VarType.REAL_PRIMITIVE_TYPE, "F"),
		
		Map.entry(VarType.VOID_TYPE, "V")
	);
	
	private JVMBytecodeUtils() {}
	
	public static String formatDescriptor(String typeDescriptor) {
		return typeDescriptor.replace('.', '/');
	}
	
	public static JVMBytecodeClass.AccessSpec[] classSpecs(JVMBytecodeClass.AccessSpec... specs) {
		return specs;
	}
	
	public static JVMBytecodeField.AccessSpec[] fieldSpecs(JVMBytecodeField.AccessSpec... specs) {
		return specs;
	}
	
	public static JVMBytecodeMethod.AccessSpec[] methodSpecs(JVMBytecodeMethod.AccessSpec... specs) {
		return specs;
	}
	
	public static String getTypename(VarType varType) {
		return PRIMITIVE_TYPENAME_MAPPER.get(varType);
	}
	
	public static List<JVMBytecodeEntity> pushDefaultValueForType(VarType type) {
		if (type == VarType.INTEGER_PRIMITIVE_TYPE || type == VarType.BOOLEAN_PRIMITIVE_TYPE) {
			return List.of(
				new JVMBytecodeInstruction("iconst_0")
			);
		} else if (type == VarType.REAL_PRIMITIVE_TYPE) {
			return List.of(
				new JVMBytecodeInstruction("ldc", "0.0")
			);
		} else
			return Collections.emptyList();
	}
	
	public static String getTypeDescriptor(VarType varType) {
		switch (varType.getTag()) {
			case ARRAY: {
				ArrayType arrayType = (ArrayType) varType;
				return "A" + getTypeDescriptor(arrayType.getElementType());
			}
			case RECORD: {
				RecordType recordType = (RecordType) varType;
				
				return String.format(
							"R_%s_",
							String.join(
								"", 
								recordType.getPropertiesTypes().values()
									.stream()
									.map(JVMBytecodeUtils::getTypeDescriptor)
									.toList()
							)
						);
			}
			default:
				return JVMBytecodeUtils.PRIMITIVE_TYPE_DESCRIPTOR_MAPPER.get(varType);
		}
	}
	
	public static String getOpcodePrefix(VarType varType) {
		switch (varType.getTag()) {
			case ARRAY:
				return "a";
			case RECORD:
				return "a";
			default:
				return PRIMITIVE_TYPE_OPCODE_MAPPER.get(varType);
		}
	}
	
	public static String getOpcodePrefixForArray(VarType varType) {
		switch (varType.getTag()) {
			case ARRAY:
				return "a";
			case RECORD:
				return "a";
			default:
				return varType == VarType.INTEGER_PRIMITIVE_TYPE
						? "i"
						: varType == VarType.BOOLEAN_PRIMITIVE_TYPE
						? "b"
						: "f";
		}
	}
}
