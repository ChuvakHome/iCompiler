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

	public static int maxStackSize(List<JVMBytecodeEntity> instructions) {
		int maxStackSize = 0;
		int currentStackSize = 0;

		for (JVMBytecodeEntity entity : instructions) {
			if (!(entity instanceof JVMBytecodeInstruction)) {
				continue;
			}

			JVMBytecodeInstruction instruction = (JVMBytecodeInstruction)entity;
			String opcode = instruction.getOpcode();
			if (opcode == null) {
				continue;
			}
			int delta = 0;
			switch (opcode) {
				case "aaload":
					delta = -1;
					break;
				case "aastore":
					delta = -3;
					break;
				case "aconst_null":
					delta = 1;
					break;
				case "aload":
					delta = 1;
					break;
				case "aload_0":
					delta = 1;
					break;
				case "aload_1":
					delta = 1;
					break;
				case "aload_2":
					delta = 1;
					break;
				case "aload_3":
					delta = 1;
					break;
				case "anewarray":
					delta = 0;
					break;
				case "areturn":
					delta = -currentStackSize;
					break;
				case "arraylength":
					delta = 0;
					break;
				case "astore":
					delta = -1;
					break;
				case "astore_0":
					delta = -1;
					break;
				case "astore_1":
					delta = -1;
					break;
				case "astore_2":
					delta = -1;
					break;
				case "astore_3":
					delta = -1;
					break;
				case "athrow":
					delta = -currentStackSize + 1;
					break;
				case "baload":
					delta = -1;
					break;
				case "bastore":
					delta = -3;
					break;
				case "bipush":
					delta = 1;
					break;
				case "breakpoint":
					delta = 0;
					break;
				case "caload":
					delta = -1;
					break;
				case "castore":
					delta = -3;
					break;
				case "checkcast":
					delta = 0;
					break;
				case "d2f":
					delta = 0;
					break;
				case "d2i":
					delta = 0;
					break;
				case "d2l":
					delta = 0;
					break;
				case "dadd":
					delta = -1;
					break;
				case "daload":
					delta = -1;
					break;
				case "dastore":
					delta = -3;
					break;
				case "dcmpg":
					delta = -1;
					break;
				case "dcmpl":
					delta = -1;
					break;
				case "dconst_0":
					delta = 1;
					break;
				case "dconst_1":
					delta = 1;
					break;
				case "ddiv":
					delta = -1;
					break;
				case "dload":
					delta = 1;
					break;
				case "dload_0":
					delta = 1;
					break;
				case "dload_1":
					delta = 1;
					break;
				case "dload_2":
					delta = 1;
					break;
				case "dload_3":
					delta = 1;
					break;
				case "dmul":
					delta = -1;
					break;
				case "dneg":
					delta = 0;
					break;
				case "drem":
					delta = -1;
					break;
				case "dreturn":
					delta = -currentStackSize;
					break;
				case "dstore":
					delta = -1;
					break;
				case "dstore_0":
					delta = -1;
					break;
				case "dstore_1":
					delta = -1;
					break;
				case "dstore_2":
					delta = -1;
					break;
				case "dstore_3":
					delta = -1;
					break;
				case "dsub":
					delta = -1;
					break;
				case "dup":
					delta = 1;
					break;
				case "dup_x1":
					delta = 1;
					break;
				case "dup_x2":
					delta = 1;
					break;
				case "dup2":
					delta = 2;
					break;
				case "dup2_x1":
					delta = 2;
					break;
				case "dup2_x2":
					delta = 2;
					break;
				case "f2d":
					delta = 0;
					break;
				case "f2i":
					delta = 0;
					break;
				case "f2l":
					delta = 0;
					break;
				case "fadd":
					delta = -1;
					break;
				case "faload":
					delta = -1;
					break;
				case "fastore":
					delta = -3;
					break;
				case "fcmpg":
					delta = -1;
					break;
				case "fcmpl":
					delta = -1;
					break;
				case "fconst_0":
					delta = 1;
					break;
				case "fconst_1":
					delta = 1;
					break;
				case "fconst_2":
					delta = 1;
					break;
				case "fdiv":
					delta = -1;
					break;
				case "fload":
					delta = 1;
					break;
				case "fload_0":
					delta = 1;
					break;
				case "fload_1":
					delta = 1;
					break;
				case "fload_2":
					delta = 1;
					break;
				case "fload_3":
					delta = 1;
					break;
				case "fmul":
					delta = -1;
					break;
				case "fneg":
					delta = 0;
					break;
				case "frem":
					delta = -1;
					break;
				case "freturn":
					delta = -currentStackSize;
					break;
				case "fstore":
					delta = -1;
					break;
				case "fstore_0":
					delta = -1;
					break;
				case "fstore_1":
					delta = -1;
					break;
				case "fstore_2":
					delta = -1;
					break;
				case "fstore_3":
					delta = -1;
					break;
				case "fsub":
					delta = -1;
					break;
				case "getfield":
					delta = 0;
					break;
				case "getstatic":
					delta = 1;
					break;
				case "goto":
					delta = 0;
					break;
				case "goto_w":
					delta = 0;
					break;
				case "i2b":
					delta = 0;
					break;
				case "i2c":
					delta = 0;
					break;
				case "i2d":
					delta = 0;
					break;
				case "i2f":
					delta = 0;
					break;
				case "i2l":
					delta = 0;
					break;
				case "i2s":
					delta = 0;
					break;
				case "iadd":
					delta = -1;
					break;
				case "iaload":
					delta = -1;
					break;
				case "iand":
					delta = -1;
					break;
				case "iastore":
					delta = -3;
					break;
				case "iconst_m1":
					delta = 1;
					break;
				case "iconst_0":
					delta = 1;
					break;
				case "iconst_1":
					delta = 1;
					break;
				case "iconst_2":
					delta = 1;
					break;
				case "iconst_3":
					delta = 1;
					break;
				case "iconst_4":
					delta = 1;
					break;
				case "iconst_5":
					delta = 1;
					break;
				case "idiv":
					delta = -1;
					break;
				case "if_acmpeq":
					delta = -2;
					break;
				case "if_acmpne":
					delta = -2;
					break;
				case "if_icmpeq":
					delta = -2;
					break;
				case "if_icmpge":
					delta = -2;
					break;
				case "if_icmpgt":
					delta = -2;
					break;
				case "if_icmple":
					delta = -2;
					break;
				case "if_icmplt":
					delta = -2;
					break;
				case "if_icmpne":
					delta = -2;
					break;
				case "ifeq":
					delta = -1;
					break;
				case "ifge":
					delta = -1;
					break;
				case "ifgt":
					delta = -1;
					break;
				case "ifle":
					delta = -1;
					break;
				case "iflt":
					delta = -1;
					break;
				case "ifne":
					delta = -1;
					break;
				case "ifnonnull":
					delta = -1;
					break;
				case "ifnull":
					delta = -1;
					break;
				case "iinc":
					delta = 0;
					break;
				case "iload":
					delta = 1;
					break;
				case "iload_0":
					delta = 1;
					break;
				case "iload_1":
					delta = 1;
					break;
				case "iload_2":
					delta = 1;
					break;
				case "iload_3":
					delta = 1;
					break;
				case "impdep1":
					delta = 0;
					break;
				case "impdep2":
					delta = 0;
					break;
				case "imul":
					delta = -1;
					break;
				case "ineg":
					delta = 0;
					break;
				case "instanceof":
					delta = 0;
					break;
				case "invokedynamic":
					throw new AssertionError();

				case "invokeinterface": {
					String method = instruction.getArg(0);
					int argsCount = argsCount(method);
					// objectref + N args - result
					delta = -argsCount;
					break;
				}
				case "invokespecial": {
					String method = instruction.getArg(0);
					int argsCount = argsCount(method);
					// objectref + N args - result
					delta = -argsCount;
					break;
				}
				case "invokestatic": {
					String method = instruction.getArg(0);
					int argsCount = argsCount(method);
					// N args - result
					delta = -argsCount + 1;
					break;
				}
				case "invokevirtual": {
					String argsCount = instruction.getArg(1);
					// Args declared - result
					delta = -Integer.parseInt(argsCount) + 1;
					break;
				}
				case "ior":
					delta = -1;
					break;
				case "irem":
					delta = -1;
					break;
				case "ireturn":
					delta = -currentStackSize;
					break;
				case "ishl":
					delta = -1;
					break;
				case "ishr":
					delta = -1;
					break;
				case "istore":
					delta = -1;
					break;
				case "istore_0":
					delta = -1;
					break;
				case "istore_1":
					delta = -1;
					break;
				case "istore_2":
					delta = -1;
					break;
				case "istore_3":
					delta = -1;
					break;
				case "isub":
					delta = -1;
					break;
				case "iushr":
					delta = -1;
					break;
				case "ixor":
					delta = -1;
					break;
				case "jsr":
					delta = 1;
					break;
				case "jsr_w":
					delta = 1;
					break;
				case "l2d":
					delta = 0;
					break;
				case "l2f":
					delta = 0;
					break;
				case "l2i":
					delta = 0;
					break;
				case "ladd":
					delta = -1;
					break;
				case "laload":
					delta = -1;
					break;
				case "land":
					delta = -1;
					break;
				case "lastore":
					delta = -3;
					break;
				case "lcmp":
					delta = -1;
					break;
				case "lconst_0":
					delta = 1;
					break;
				case "lconst_1":
					delta = 1;
					break;
				case "ldc":
					delta = 1;
					break;
				case "ldc_w":
					delta = 1;
					break;
				case "ldc2_w":
					delta = 1;
					break;
				case "ldiv":
					delta = -1;
					break;
				case "lload":
					delta = 1;
					break;
				case "lload_0":
					delta = 1;
					break;
				case "lload_1":
					delta = 1;
					break;
				case "lload_2":
					delta = 1;
					break;
				case "lload_3":
					delta = 1;
					break;
				case "lmul":
					delta = -1;
					break;
				case "lneg":
					delta = 0;
					break;
				case "lookupswitch":
					delta = -1;
					break;
				case "lor":
					delta = -1;
					break;
				case "lrem":
					delta = -1;
					break;
				case "lreturn":
					delta = -currentStackSize;
					break;
				case "lshl":
					delta = -1;
					break;
				case "lshr":
					delta = -1;
					break;
				case "lstore":
					delta = -1;
					break;
				case "lstore_0":
					delta = -1;
					break;
				case "lstore_1":
					delta = -1;
					break;
				case "lstore_2":
					delta = -1;
					break;
				case "lstore_3":
					delta = -1;
					break;
				case "lsub":
					delta = -1;
					break;
				case "lushr":
					delta = -1;
					break;
				case "lxor":
					delta = -1;
					break;
				case "monitorenter":
					delta = -1;
					break;
				case "monitorexit":
					delta = -1;
					break;
				case "multianewarray":
					// TODO
					delta = -1;
					break;
				case "new":
					delta = 1;
					break;
				case "newarray":
					delta = 0;
					break;
				case "nop":
					delta = 0;
					break;
				case "pop":
					delta = -1;
					break;
				case "pop2":
					delta = -2;
					break;
				case "putfield":
					delta = -2;
					break;
				case "putstatic":
					delta = -1;
					break;
				case "ret":
					delta = 0;
					break;
				case "return":
					delta = -currentStackSize;
					break;
				case "saload":
					delta = -1;
					break;
				case "sastore":
					delta = -3;
					break;
				case "sipush":
					delta = 1;
					break;
				case "swap":
					delta = 0;
					break;
				case "tableswitch":
					delta = -1;
					break;
				case "wide":
					throw new AssertionError();
				
				default:
					throw new AssertionError();
			}

			currentStackSize += delta;
			maxStackSize = Math.max(delta, maxStackSize);
		}

		return maxStackSize;
	}

	public static int argsCount(String name) {
		int result = 0;
		int start = name.indexOf('(');
		int current = start + 1;
		while (current < name.length() && name.charAt(current) != ')') {
			if (name.charAt(current) == 'L') {
				while(current < name.length() && name.charAt(current) != ';') {
					current++;
				}
			} else if (name.charAt(current) == '[') {
				current++;
				continue;
			}
			result++;
			current++;
		}
		return result;
	}
}
