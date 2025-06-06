package ru.itmo.icompiler.codegen.jvm.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import ru.itmo.icompiler.codegen.jvm.JVMBytecodeClass;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeDirective;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeEntity;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeField;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeInstruction;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeInstruction.JVMBytecodeInstructionLabeled;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeInstruction.JVMBytecodeLabel;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeMethod;
import ru.itmo.icompiler.codegen.jvm.utils.JVMBytecodeUtils;
import static ru.itmo.icompiler.codegen.jvm.utils.JVMBytecodeUtils.classSpecs;
import static ru.itmo.icompiler.codegen.jvm.utils.JVMBytecodeUtils.fieldSpecs;
import static ru.itmo.icompiler.codegen.jvm.utils.JVMBytecodeUtils.methodSpecs;
import ru.itmo.icompiler.codegen.jvm.visitor.JVMCodeEmitterExpressionVisitor.BranchContext;
import ru.itmo.icompiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.ExpressionVisitorContext;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.lex.Token.TokenType;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.ArrayType.SizedArrayType;
import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.RecordType;
import ru.itmo.icompiler.semantic.RecordType.RecordProperty;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.VarType.Tag;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.BreakStatementASTNode;
import ru.itmo.icompiler.syntax.ast.CompoundStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ContinueStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForEachStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForInRangeStatementASTNode;
import ru.itmo.icompiler.syntax.ast.IfThenElseStatementASTNode;
import ru.itmo.icompiler.syntax.ast.PrintStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ProgramASTNode;
import ru.itmo.icompiler.syntax.ast.ReturnStatementASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDefinitionASTNode;
import ru.itmo.icompiler.syntax.ast.TypeDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.VariableAssignmentASTNode;
import ru.itmo.icompiler.syntax.ast.VariableDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.WhileBodyStatementASTNode;
import ru.itmo.icompiler.syntax.ast.WhileStatementASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ArrayAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode.BinaryOperatorType;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

public class JVMCodeEmitterVisitor implements ASTVisitor<List<JVMBytecodeEntity>, ExpressionVisitorContext> {
	public static final String PROGRAM_JVM_PACKAGE = "ilang"; 
	public static final String PROGRAM_CLASS_NAME = PROGRAM_JVM_PACKAGE + "/" + "Program"; 
	
	private static class Limits implements Cloneable {
		private int localVariablesCount;
		private int stackSize;
		
		public Limits(int localVariablesCount, int stackSize) {
			this.localVariablesCount = localVariablesCount;
			this.stackSize = stackSize;
		}
		
		public Limits clone() {
			return new Limits(localVariablesCount, stackSize);
		}
	}
	
	public static class LocalVariableContext {
		private LocalVariableContext parentContext;
		private Map<String, Integer> indices;
		
		public LocalVariableContext(LocalVariableContext parentContext, Map<String, Integer> indices) {
			this.parentContext = parentContext;
			this.indices = indices;
		}
		
		public LocalVariableContext(LocalVariableContext parentContext) {
			this(parentContext, new HashMap<>());
		}
		
		public LocalVariableContext() {
			this(null);
		}
		
		public void addLocalVariable(String varName, int index) {
			indices.put(varName, index);
		}
		
		public boolean containsLocalVarIndex(String varName) {
			return indices.containsKey(varName) || parentContext != null && parentContext.containsLocalVarIndex(varName);
		}
		
		public int getLocalVarIndex(String varName) {
			Integer index = indices.get(varName);
			
			if (index == null && parentContext != null)
				index = parentContext.getLocalVarIndex(varName);
			
			return index;
		}
	}
	
	public static class IntCounter {
		private int counter;
		
		public IntCounter(int counter) {
			this.counter = counter;
		}
		
		public IntCounter() {
			this(0);
		}
		
		public void setCounter(int counter) {
			this.counter = counter;
		}
		
		public void incCounter() {
			++counter;
		}
		
		public void incCounter(int delta) {
			counter += delta;
		}
		
		public int getCounter() {
			return counter;
		}
		
		public String toString() {
			return String.valueOf(counter);
		}
	}
	
	public static class ExpressionVisitorContext {
		private LocalVariableContext localVarCtx;
		
		private IntCounter labelCounter;
		
		private String thenLabel; // short-circuit eval
		private String elseLabel; // short-circuit eval
		
		private String loopStartLabel; 
		private String loopConditionalLabel;
		private String loopEndLabel;
		
		public ExpressionVisitorContext(
					LocalVariableContext localVarCtx, 
					IntCounter labelCounter, 
					String thenLabel, 
					String elseLabel,
					String loopStartLabel,
					String loopConditionalLabel,
					String loopEndLabel
				) {
			this.localVarCtx = localVarCtx;
			this.labelCounter = labelCounter;
			
			this.thenLabel = thenLabel;
			this.elseLabel = elseLabel;
			
			this.loopStartLabel = loopStartLabel;
			this.loopConditionalLabel = loopConditionalLabel;
			this.loopEndLabel = loopEndLabel;
		}
		
		public ExpressionVisitorContext() {
			this(new LocalVariableContext(), new IntCounter(), null, null, null, null, null);
		}
		
		public LocalVariableContext getLocalVariableContext() {
			return localVarCtx;
		}
		
		public IntCounter getLabelCounter() {
			return labelCounter;
		}
		
		public String getThenLabel() {
			return thenLabel;
		}
		
		public String getElseLabel() {
			return elseLabel;
		}
		
		public BranchContext toBranchContext(Boolean condition) {
			return toBranchContext(condition, thenLabel, elseLabel);
		}
		
		public BranchContext toBranchContext(Boolean condition, String thenLabel, String elseLabel) {
			return new BranchContext(condition, thenLabel, elseLabel, localVarCtx, labelCounter);
		}
	}
	
	private int freshVariableCount = 0;
	private int maxLocalVarNumber = 0;
	
	private Stack<Limits> contextStack;
	private JVMCodeEmitterExpressionVisitor expressionVisitor;
	
	private FunctionType currentRoutineType;
	
	private Set<RecordType> declaredRecords;
	
	private String sourceName;
	
	public JVMCodeEmitterVisitor(String sourceName) {
		contextStack = new Stack<>();
		expressionVisitor = new JVMCodeEmitterExpressionVisitor();
		
		declaredRecords = new HashSet<>();
		
		this.sourceName = sourceName;
	}
	
	public static final Map<VarType, String> PRIMITIVE_TYPE_MAPPER = Map.ofEntries(
		Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "Z"),
		Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "I"),
		Map.entry(VarType.REAL_PRIMITIVE_TYPE, "F"),
		
		Map.entry(VarType.VOID_TYPE, "V")
	);
	
	public static String getMangledTypeName(VarType varType) {
		switch (varType.getTag()) {
			case ARRAY: {
				ArrayType arrayType = (ArrayType) varType;
				return "[" + getMangledTypeName(arrayType.getElementType());
			}
			case RECORD: {
				return "L" + PROGRAM_JVM_PACKAGE + "/" + JVMBytecodeUtils.getTypeDescriptor(varType) + ";"; // to be continued
			}
			default:
				return PRIMITIVE_TYPE_MAPPER.get(varType);
		}
	}
		
	private String newFreshVariable() {
		return "FV#" + freshVariableCount++;
	}
	
	private static List<JVMBytecodeEntity> initArrayVar(SizedArrayType arrayType) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		VarType elementType = arrayType.getElementType();
		
		instructions.add(
			JVMCodeEmitterExpressionVisitor.getLoadIntConstInstruction(arrayType.getSize())
		);
		
		if (elementType.getTag() == Tag.PRIMITIVE)
			instructions.add(new JVMBytecodeInstruction("newarray", JVMBytecodeUtils.getTypename(elementType)));
		else if (elementType.getTag() == Tag.ARRAY) {
			int dimensions = 1;
			
			while (elementType.getTag() == Tag.ARRAY) {
				SizedArrayType elementArrayType = (SizedArrayType) elementType; 
				
				instructions.add(
					JVMCodeEmitterExpressionVisitor.getLoadIntConstInstruction(elementArrayType.getSize())
				);
				++dimensions;
				
				elementType = elementArrayType.getElementType(); 
			}
			
			instructions.add(
				new JVMBytecodeInstruction(
					"multianewarray", 
					getMangledTypeName(arrayType),
					dimensions
				)
			);
		}
		
		return instructions;
	}
	
	private static List<JVMBytecodeEntity> initRecordVar(RecordType recordType) {
		String recordJVMClass = PROGRAM_JVM_PACKAGE + "/" + JVMBytecodeUtils.getTypeDescriptor(recordType);
		
		return Arrays.asList(
			new JVMBytecodeInstruction("new", recordJVMClass),
			
			new JVMBytecodeInstruction("dup"),
			new JVMBytecodeInstruction("invokespecial", recordJVMClass + "/<init>()V")
		);
	}
	
	private JVMBytecodeMethod emitCodeForProgramClassInit(Map<String, VariableDeclarationASTNode> globalDeclarations) {
		List<JVMBytecodeEntity> clinitMethodInstructions = new ArrayList<>();
		
		int localVarsCount = 1;
		int stackSize = 10;
		
		IntCounter labelCounter = new IntCounter(); 
		
		for (Map.Entry<String, VariableDeclarationASTNode> entry: globalDeclarations.entrySet()) {
			String varName = entry.getKey();
			
			VariableDeclarationASTNode declNode = entry.getValue();
			VarType varType = declNode.getVarType();
			
			switch (varType.getTag()) {
				case PRIMITIVE: {
					if (declNode.getChildren().isEmpty()) {
						clinitMethodInstructions.addAll(
							JVMBytecodeUtils.pushDefaultValueForType(varType)
						);
					} else {
						VariableAssignmentASTNode assignNode = (VariableAssignmentASTNode) declNode.getChild(0);
						ExpressionASTNode assignExprNode = assignNode.getValueNode();
						clinitMethodInstructions.addAll(
							assignExprNode.accept(expressionVisitor, new BranchContext(null, null, null, null, labelCounter))
						);
					}
					
					break;
				} 
				case ARRAY: {
					clinitMethodInstructions.addAll(
						initArrayVar((SizedArrayType) varType)
					);
					break;
				} 
				case RECORD: {
					clinitMethodInstructions.addAll(
						initRecordVar((RecordType) varType)
					);
					break;
				}
			}
			
			clinitMethodInstructions.add(
				new JVMBytecodeInstruction("putstatic", PROGRAM_CLASS_NAME + "/" + varName, getMangledTypeName(varType))
			);
		}
		
		clinitMethodInstructions.add(
			new JVMBytecodeInstruction("return")
		);
		
		stackSize = JVMBytecodeUtils.maxStackSize(clinitMethodInstructions);

		return new JVMBytecodeMethod(
					methodSpecs(JVMBytecodeMethod.AccessSpec.STATIC),
					"<clinit>",
					Collections.emptyList(),
					"V",
					localVarsCount,
					stackSize,
					clinitMethodInstructions
				);
	}
	
	private static JVMBytecodeMethod emitCodeForMainMethod(List<RoutineDeclarationASTNode> declaredRoutines) {
		class RoutineCaseInfo {
			String routineName;
			int hashCode;
			String label;
			List<JVMBytecodeEntity> caseInstructions;
			
			public RoutineCaseInfo(String routineName, int hashCode, String label, List<JVMBytecodeEntity> caseInstructions) {
				this.routineName = routineName;
				this.hashCode = hashCode;
				this.label = label;
				this.caseInstructions = caseInstructions;
			}
		}
		
		List<RoutineDeclarationASTNode> routines = new ArrayList<>(declaredRoutines);
		routines.removeIf(node -> 
			node.getArgumentsDeclarations().stream().anyMatch(argDecl ->  
				argDecl.getVarType().getTag() != VarType.Tag.PRIMITIVE
			)
		);
		
		IntCounter labelCounter = new IntCounter();
		
		Map<Integer, String> switchMap = new TreeMap<>();
		List<RoutineCaseInfo> routineCaseInfos = new ArrayList<>();
		
		int locals = 0;
		int stackSize = 10;
		
		for (RoutineDeclarationASTNode declNode: routines) {
			final String routineName = declNode.getRoutineName(); 
			final String caseLabel = CodeEmitterUtils.allocateLabel(labelCounter);
			
			List<VariableDeclarationASTNode> argsDeclarations = declNode.getArgumentsDeclarations(); 
			
			List<JVMBytecodeEntity> caseBody = new ArrayList<>();
			List<JVMBytecodeEntity> loadArgsInstrs = new ArrayList<>();
			
			StringBuilder routineFullMangledName = new StringBuilder("_").append(routineName).append("(");
			
			for (int i = 1; i <= argsDeclarations.size(); ++i) {
				VariableDeclarationASTNode argDecl = argsDeclarations.get(i - 1);

				caseBody.addAll(
					List.of(
						new JVMBytecodeInstruction("aload_0"),
						JVMCodeEmitterExpressionVisitor.getLoadIntConstInstruction(i),
						new JVMBytecodeInstruction("aaload")
					)
				);
				
				VarType argType = argDecl.getVarType();
				routineFullMangledName.append(getMangledTypeName(argType));
				
				if (argType == VarType.BOOLEAN_PRIMITIVE_TYPE) {
					caseBody.addAll(
						List.of(
							new JVMBytecodeInstruction("invokestatic", "java/lang/Boolean/parseBoolean(Ljava/lang/String;)Z"),
							new JVMBytecodeInstruction("istore", i)
						)
					);
					
					loadArgsInstrs.add(new JVMBytecodeInstruction("iload", i));
				} else if (argType == VarType.INTEGER_PRIMITIVE_TYPE) {
					caseBody.addAll(
						List.of(
							new JVMBytecodeInstruction("invokestatic", "java/lang/Integer/parseInt(Ljava/lang/String;)I"),
							new JVMBytecodeInstruction("istore", i)
						)
					);
					
					loadArgsInstrs.add(new JVMBytecodeInstruction("iload", i));
				} else {
					caseBody.addAll(
						List.of(
							new JVMBytecodeInstruction("invokestatic", "java/lang/Float/parseFloat(Ljava/lang/String;)F"),
							new JVMBytecodeInstruction("fstore", i)
						)
					);
					
					loadArgsInstrs.add(new JVMBytecodeInstruction("fload", i));
				}
			}
			
			routineFullMangledName
				.append(")")
				.append(
					getMangledTypeName(declNode.getResultType())
				);
			
			caseBody.addAll(loadArgsInstrs);
			caseBody.add(
				new JVMBytecodeInstruction("invokestatic", PROGRAM_CLASS_NAME + "/" + routineFullMangledName.toString())
			);
			
			if (declNode.getResultType() != VarType.VOID_TYPE) {
				caseBody.add(
					new JVMBytecodeInstruction("pop")
				);
			}
			
			locals = Math.max(locals, argsDeclarations.size());
			
			switchMap.put(routineName.hashCode(), caseLabel);
			
			RoutineCaseInfo caseInfo = new RoutineCaseInfo(routineName, routineName.hashCode(), caseLabel, caseBody);
			routineCaseInfos.add(caseInfo);
		}
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		String retLabel = CodeEmitterUtils.allocateLabel(labelCounter);
		
		instructions.addAll(
			List.of(
				new JVMBytecodeInstruction("aload_0"),
				new JVMBytecodeInstruction("iconst_0"),
				new JVMBytecodeInstruction("aaload"),
				
				new JVMBytecodeInstruction("invokevirtual", "java/lang/String/hashCode()I"),
				new JVMBytecodeInstruction.LookupSwitchInstruction(switchMap, retLabel)
			)
		);
		
		routineCaseInfos.forEach(caseInfo -> {
			instructions.add(new JVMBytecodeLabel(caseInfo.label));
			instructions.addAll(caseInfo.caseInstructions);
			instructions.add(new JVMBytecodeInstruction("goto", retLabel));
		});
		
		instructions.add(new JVMBytecodeInstructionLabeled(retLabel, "return"));
		
		return new JVMBytecodeMethod(
					methodSpecs(JVMBytecodeMethod.AccessSpec.PUBLIC, JVMBytecodeMethod.AccessSpec.STATIC), 
					"main", 
					List.of("[Ljava/lang/String;"), 
					"V", 
					locals + 1,
					stackSize,
					instructions
				);
	}
	
	private static List<JVMBytecodeEntity> emitCodeForRecordClass(RecordType recordType) {
		List<JVMBytecodeEntity> initMethodInstructions = new ArrayList<>();
		
		int localVarsCount = 1;
		int stackSize = 10;
		
		initMethodInstructions.addAll(
			Arrays.asList(
				new JVMBytecodeInstruction("aload_0"),
				new JVMBytecodeInstruction("invokespecial", "java/lang/Object/<init>()V")
			)
		);
		
		final String recordClassName = PROGRAM_JVM_PACKAGE + "/" + JVMBytecodeUtils.getTypeDescriptor(recordType);
		List<JVMBytecodeField> programClassFields = new ArrayList<>();
		
		int fieldCounter = 0;
		
		for (VarType propType: recordType.getPropertiesTypes().values()) {
			String fieldName = "field" + fieldCounter;
			String propJVMTypeDesc = getMangledTypeName(propType);
			
			programClassFields.add(
				new JVMBytecodeField(
					fieldSpecs(JVMBytecodeField.AccessSpec.PUBLIC),
					fieldName,
					propJVMTypeDesc
				)
			);
			
			if (propType.getTag() != Tag.PRIMITIVE) {
				initMethodInstructions.add(
					new JVMBytecodeInstruction("aload_0") // load this
				);
				
				switch (propType.getTag()) {
					case RECORD: {
						initMethodInstructions.addAll(
							initRecordVar((RecordType) propType)
						);
						break;
					}
					case ARRAY: {
						initMethodInstructions.addAll(
							initArrayVar((SizedArrayType) propType)
						);
						break;
					}
				}
				
				initMethodInstructions.add(
					new JVMBytecodeInstruction(
							"putfield", 
							recordClassName + "/" + fieldName,
							propJVMTypeDesc
						)
				);
			}
			
			++fieldCounter;
		}
		
		initMethodInstructions.add(new JVMBytecodeInstruction("return"));
		
		stackSize = JVMBytecodeUtils.maxStackSize(initMethodInstructions);

		return Arrays.asList(
				new JVMBytecodeClass(
					classSpecs(JVMBytecodeClass.AccessSpec.PUBLIC, JVMBytecodeClass.AccessSpec.FINAL),
					JVMBytecodeUtils.getTypeDescriptor(recordType),
					recordClassName,
					programClassFields,
					Arrays.asList(
						new JVMBytecodeMethod(
							methodSpecs(),
							"<init>",
							Collections.emptyList(),
							"V",
							localVarsCount,
							stackSize,
							initMethodInstructions
						)
					)
				)
			);
	}
	
	private void saveRecordVarDeclaration(RecordType recordType) {
		Stack<RecordType> recordTypes = new Stack<>();
		
		recordTypes.add(recordType);
		
		while (!recordTypes.empty()) {
			RecordType newRecordType = recordTypes.pop();
			declaredRecords.add(newRecordType);
			
			newRecordType.getPropertiesTypes().values().forEach(propertyType -> {
				while (propertyType.getTag() == VarType.Tag.ARRAY)
					propertyType = ((ArrayType) propertyType).getElementType();
				
				if (propertyType.getTag() == VarType.Tag.RECORD && !recordTypes.contains(propertyType))
					recordTypes.add((RecordType) propertyType);
			});
		}
	}
	
	private JVMBytecodeField processGlobalVarDecl(VariableDeclarationASTNode node) {
		String varName = node.getVarName();
		VarType varType = node.getVarType();
		
		String fieldName = varName;
		String typeDescriptor = getMangledTypeName(varType);
		
		if (varType.getTag() == Tag.RECORD)
			saveRecordVarDeclaration((RecordType) varType);
		
		return new JVMBytecodeField(
				fieldSpecs(
					JVMBytecodeField.AccessSpec.PUBLIC,
					JVMBytecodeField.AccessSpec.STATIC
				), 
				fieldName, 
				typeDescriptor
			);
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(ProgramASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeField> programClassFields = new ArrayList<>();
		List<JVMBytecodeMethod> programClassMethods = new ArrayList<>();
		
		Map<String, VariableDeclarationASTNode> globalVarsDeclarations = new LinkedHashMap<>();
		List<RoutineDeclarationASTNode> declaredRoutines = new ArrayList<>();
		
		for (ASTNode child: node.getChildren()) {
			switch (child.getNodeType()) {
				case VAR_DECL_NODE: {
					VariableDeclarationASTNode varDeclNode = (VariableDeclarationASTNode) child;
					JVMBytecodeField field = processGlobalVarDecl(varDeclNode);
					programClassFields.add(field);
					globalVarsDeclarations.put(field.getFieldName(), varDeclNode);
					
					break;
				}
				case ROUTINE_DEF_NODE: {
					RoutineDeclarationASTNode routineDeclNode = ((RoutineDefinitionASTNode) child).getRoutineDeclaration();
					
					JVMBytecodeMethod jvmMethod = (JVMBytecodeMethod) child.accept(this, ctx).get(0);
					programClassMethods.add(jvmMethod);
					declaredRoutines.add(routineDeclNode);
					
					break;
				}
				default:
					break;
			}
		}
		
		programClassMethods.addAll(
			List.of(
				emitCodeForProgramClassInit(globalVarsDeclarations), // add <clinit>
				emitCodeForMainMethod(declaredRoutines)
			)
		);
		
		List<JVMBytecodeEntity> programClasses = new ArrayList<>();
		programClasses.add(new JVMBytecodeClass(
				classSpecs(JVMBytecodeClass.AccessSpec.PUBLIC, JVMBytecodeClass.AccessSpec.FINAL),
				sourceName,
				PROGRAM_CLASS_NAME,
				programClassFields,
				programClassMethods
			));
		
		declaredRecords.stream().map(JVMCodeEmitterVisitor::emitCodeForRecordClass).forEachOrdered(programClasses::addAll);
		
		return programClasses;
	}

	@Override
	public List<JVMBytecodeEntity> visit(CompoundStatementASTNode node, ExpressionVisitorContext ctx) {
		Limits topCtx = contextStack.peek();
		contextStack.add(topCtx.clone());
		
		LocalVariableContext subctx = new LocalVariableContext(ctx.localVarCtx);
		ExpressionVisitorContext subexprctx = new ExpressionVisitorContext(subctx, ctx.labelCounter, ctx.thenLabel, ctx.elseLabel, ctx.loopStartLabel, ctx.loopConditionalLabel, ctx.loopEndLabel);
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		for (ASTNode child: node.getChildren()) {
			instructions.addAll(
				child.accept(this, subexprctx)
			);
		}
		
		topCtx = contextStack.pop();
		maxLocalVarNumber = Math.max(topCtx.localVariablesCount, maxLocalVarNumber);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(WhileBodyStatementASTNode node, ExpressionVisitorContext ctx) {
		Limits topCtx = contextStack.peek();
		contextStack.add(topCtx.clone());

		LocalVariableContext subctx = new LocalVariableContext(ctx.localVarCtx);
		ExpressionVisitorContext subexprctx = new ExpressionVisitorContext(subctx, ctx.labelCounter, ctx.thenLabel, ctx.elseLabel, ctx.loopStartLabel, ctx.loopConditionalLabel, ctx.loopEndLabel);

		List<JVMBytecodeEntity> instructions = new ArrayList<>();


		for (int i = 0; i + 1 < node.getChildren().size(); i++) {
			instructions.addAll(
					node.getChildren().get(i).accept(this, subexprctx)
			);
		}
		instructions.addAll(
				Arrays.asList(
						new JVMBytecodeLabel(ctx.loopConditionalLabel)
				)
		);
		instructions.addAll(
			node.getChildren().get(node.getChildren().size() - 1).accept(this, subexprctx)
		);

		topCtx = contextStack.pop();
		maxLocalVarNumber = Math.max(topCtx.localVariablesCount, maxLocalVarNumber);

		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(VariableDeclarationASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		if (node.getToken() != null)
			instructions.add(new JVMBytecodeDirective("line", node.getLineNumber()));
		
		String varName = node.getVarName();
		VarType varType = node.getVarType();
		
		int lvIndex = contextStack.peek().localVariablesCount++;
		
		ctx.localVarCtx.addLocalVariable(varName, lvIndex);
		
		switch (varType.getTag()) {
			case PRIMITIVE: {
					instructions.addAll(
						JVMBytecodeUtils.pushDefaultValueForType(varType)
					);
					String prefix = JVMBytecodeUtils.PRIMITIVE_TYPE_OPCODE_MAPPER.get(varType);
					instructions.add(new JVMBytecodeInstruction(prefix + "store", lvIndex));
					
					break;
				} 
			case ARRAY: {
				SizedArrayType arrayType = (SizedArrayType) node.getVarType();
				
				instructions.addAll(
					initArrayVar(arrayType)
				);
				
				instructions.add(new JVMBytecodeInstruction("astore", lvIndex));
				
				return instructions;
			}
			case RECORD: {
				RecordType varRecordType = (RecordType) varType;
				saveRecordVarDeclaration(varRecordType);
				
				instructions.addAll(
					initRecordVar(varRecordType)
				);
				instructions.add(new JVMBytecodeInstruction("astore", lvIndex));
				
				return instructions;
			}
		}
		
		if (node.getChildren().isEmpty())
			return instructions;
		
		instructions.addAll(
			node.getChild(0).accept(this, ctx)
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(VariableAssignmentASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		instructions.add(new JVMBytecodeDirective("line", node.getLineNumber()));
		
		ExpressionASTNode leftSideNode = node.getLeftSide();
		VarType requiredType = leftSideNode.getExpressionType();
		
		ExpressionASTNode valueNode = node.getValueNode();
		
		List<JVMBytecodeEntity> valueCompInstrs = valueNode.accept(expressionVisitor, ctx.toBranchContext(null));
		
		switch (leftSideNode.getExpressionNodeType()) {
			case VARIABLE_EXPR_NODE: {
				VariableExpressionNode varExprNode = (VariableExpressionNode) leftSideNode;
				
				String assignVarName = varExprNode.getVariable();
				
				instructions.addAll(valueCompInstrs);
				
				if (ctx.localVarCtx.containsLocalVarIndex(assignVarName)) {
					int lvIndex = ctx.localVarCtx.getLocalVarIndex(assignVarName);
					
					instructions.add(
						lvIndex <= 3 
						? new JVMBytecodeInstruction(
								JVMBytecodeUtils.getOpcodePrefix(requiredType) + "store_" + lvIndex
							)
						: new JVMBytecodeInstruction(
								JVMBytecodeUtils.getOpcodePrefix(requiredType) + "store", 
								lvIndex
							)
					);
				} else {
					instructions.add(
						new JVMBytecodeInstruction(
							"putstatic", 
							PROGRAM_CLASS_NAME + "/" + assignVarName, 
							JVMBytecodeUtils.getTypeDescriptor(requiredType)
						)
					);
				}
				
				break;
			}
			case ARRAY_ACCESS_EXPR_NODE: {
				ArrayAccessExpressionNode arrAccNode = (ArrayAccessExpressionNode) leftSideNode;
				
				ExpressionASTNode holderExpr = arrAccNode.getHolder();
				ArrayType arrayType = (ArrayType) holderExpr.getExpressionType();
				
				instructions.addAll(
					holderExpr.accept(expressionVisitor, ctx.toBranchContext(null))
				);
				instructions.addAll(
					arrAccNode.getIndex().accept(expressionVisitor, ctx.toBranchContext(null))
				);
				instructions.addAll(
					Arrays.asList(
						JVMCodeEmitterExpressionVisitor.getLoadIntConstInstruction(-1),
						new JVMBytecodeInstruction("iadd")
					)
				);
				instructions.addAll(valueCompInstrs);
				
				final String opcode = JVMBytecodeUtils.getOpcodePrefixForArray(arrayType.getElementType());
				
				instructions.add(
					new JVMBytecodeInstruction(opcode + "astore")
				);
				
				break;
			}
			case PROPERTY_ACCESS_EXPR_NODE: {
				PropertyAccessExpressionNode propAccNode = (PropertyAccessExpressionNode) leftSideNode;
				
				ExpressionASTNode holder = propAccNode.getPropertyHolder();
				instructions.addAll(
					holder.accept(expressionVisitor, ctx.toBranchContext(null))
				);
				
				RecordType recordType = (RecordType) holder.getExpressionType();
				
				String prop = propAccNode.getPropertyName();
				
				VarType fieldType = null;
				int fieldIndex = 0;
				
				for (RecordProperty recordProperty: recordType.getProperties()) {
					if (recordProperty.name.equals(prop)) {
						fieldType = recordProperty.type;
						
						break;
					}
					
					++fieldIndex;
				}
				
				instructions.addAll(valueCompInstrs);
				instructions.add(new JVMBytecodeInstruction(
						"putfield", 
						JVMCodeEmitterVisitor.PROGRAM_JVM_PACKAGE + "/" + JVMBytecodeUtils.getTypeDescriptor(recordType) + "/field" + fieldIndex,
						JVMCodeEmitterVisitor.getMangledTypeName(fieldType)
					)
				);
				
				break;
			}
		}
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(TypeDeclarationASTNode node, ExpressionVisitorContext ctx) {
		return new ArrayList<>();
	}

	@Override
	public List<JVMBytecodeEntity> visit(RoutineDeclarationASTNode node, ExpressionVisitorContext ctx) {
		VarType routineRetType = node.getResultType();
		
		if (routineRetType.getTag() == Tag.RECORD)
			saveRecordVarDeclaration((RecordType) routineRetType);
		
		for (VariableDeclarationASTNode argDecl: node.getArgumentsDeclarations()) {
			if (argDecl.getVarType().getTag() == Tag.RECORD)
				saveRecordVarDeclaration((RecordType) argDecl.getVarType());
		}
		
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(RoutineDefinitionASTNode node, ExpressionVisitorContext ctx) {
		RoutineDeclarationASTNode routineHeader = node.getRoutineDeclaration();
		routineHeader.accept(this, ctx); // save info about return type
		
		String methodName = "_" + routineHeader.getRoutineName();
		
		int argsCount = routineHeader.getArgumentsDeclarations().size();
		
		List<String> argsTypesDescriptors = new ArrayList<>(argsCount);
		String returnTypeDescriptor = getMangledTypeName(routineHeader.getResultType());
		
		int argnum = 0;
		
		LocalVariableContext routineLVCtx = new LocalVariableContext(ctx.localVarCtx);
		ExpressionVisitorContext routineExprCtx = new ExpressionVisitorContext(routineLVCtx, new IntCounter(), null, null, null, null, null);
		
		for (VariableDeclarationASTNode argDecl: routineHeader.getArgumentsDeclarations()) {
			String argName = argDecl.getVarName();
			VarType argType = argDecl.getVarType();
			
			routineLVCtx.addLocalVariable(argName, argnum++);
			
			String jvmTypeDescriptor = JVMCodeEmitterVisitor.getMangledTypeName(argType);
			
			argsTypesDescriptors.add(jvmTypeDescriptor);
		}
		
		contextStack.add(new Limits(argsCount, 0));
		maxLocalVarNumber = argsCount;
		
		currentRoutineType = new FunctionType(
			routineHeader.getArgumentsDeclarations().stream().map(VariableDeclarationASTNode::getVarType).toList(),
			routineHeader.getResultType()
		);
		
		List<JVMBytecodeEntity> routineInstructions = node.getBody().accept(this, routineExprCtx);
		
//		if (routineHeader.getResultType() == VarType.VOID_TYPE)
			routineInstructions.add(new JVMBytecodeInstruction("return"));
		
		int stackSize = JVMBytecodeUtils.maxStackSize(routineInstructions);

		return Arrays.asList(
				new JVMBytecodeMethod(
					methodSpecs(
						JVMBytecodeMethod.AccessSpec.PUBLIC,
						JVMBytecodeMethod.AccessSpec.STATIC
					), 
					methodName, 
					argsTypesDescriptors,
					returnTypeDescriptor, 
					maxLocalVarNumber, 
					stackSize,
					routineInstructions
				)
			);
	}

	@Override
	public List<JVMBytecodeEntity> visit(ReturnStatementASTNode node, ExpressionVisitorContext ctx) {
		ExpressionASTNode returnValueExprNode = node.getResultNode();
		
		VarType requiredReturnType = currentRoutineType.getReturnType();
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		instructions.add(new JVMBytecodeDirective("line", node.getLineNumber()));
		
		instructions.addAll(
			returnValueExprNode.accept(expressionVisitor, ctx.toBranchContext(null))
		);
		instructions.add(
			new JVMBytecodeInstruction(
				JVMBytecodeUtils.getOpcodePrefix(requiredReturnType) + "return"
			)
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(IfThenElseStatementASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		instructions.add(new JVMBytecodeDirective("line", node.getLineNumber()));
		
		String thenLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
		
		String elseLabel = null;
		
		ASTNode elseBranch = node.getElseBranch(); 
		
		if (elseBranch != null)
			elseLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
		
		String endLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
		
		if (elseLabel == null)
			elseLabel = endLabel;
		
		instructions.addAll(
			node.getConditionExpression().accept(expressionVisitor, ctx.toBranchContext(false, thenLabel, elseLabel))
		);
		
		instructions.add(new JVMBytecodeLabel(thenLabel));
		instructions.addAll(
			node.getTrueBranch().accept(this, ctx)
		);
		
		if (elseBranch != null) {
			instructions.addAll(
				Arrays.asList(
					new JVMBytecodeInstruction("goto", endLabel),
					new JVMBytecodeLabel(elseLabel)
				)
			);
			instructions.addAll(
				elseBranch.accept(this, ctx)
			);
		}
		
		instructions.add(new JVMBytecodeLabel(endLabel));
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(ForInRangeStatementASTNode node, ExpressionVisitorContext ctx) {
		Token fakeToken = new Token(node.getLineNumber(), node.getLineOffset(), null, null);
		
		CompoundStatementASTNode scopedStmt = new CompoundStatementASTNode(null);
		
		VariableExpressionNode counterVarExpr = new VariableExpressionNode(null, new Token(node.getLineNumber(), node.getLineOffset(), TokenType.IDENTIFIER, node.getIterVariable()));
		counterVarExpr.setExpressionType(VarType.INTEGER_PRIMITIVE_TYPE);
		
		VariableDeclarationASTNode counterVarDecl = new VariableDeclarationASTNode(null, VarType.INTEGER_PRIMITIVE_TYPE, fakeToken, node.getIterVariable());
		counterVarDecl.addChild(
			new VariableAssignmentASTNode(null, 
				counterVarExpr, 
				node.isReversed() ? node.getToExpression() : node.getFromExpression()
			)
		);

		String forCounterEnd = newFreshVariable();

		VariableExpressionNode counterEndValueExpr = new VariableExpressionNode(null, new Token(node.getLineNumber(), node.getLineOffset(), TokenType.IDENTIFIER, forCounterEnd));
		counterEndValueExpr.setExpressionType(VarType.INTEGER_PRIMITIVE_TYPE);

		VariableDeclarationASTNode counterEndValueDecl = new VariableDeclarationASTNode(null, VarType.INTEGER_PRIMITIVE_TYPE, fakeToken, forCounterEnd);
		counterEndValueDecl.addChild(
			new VariableAssignmentASTNode(null,
				counterEndValueExpr,
				node.isReversed() ? node.getFromExpression() : node.getToExpression()
			)
		);

		scopedStmt.addChild(counterVarDecl);
		scopedStmt.addChild(counterEndValueDecl);

		BinaryOperatorType conditionalComp = node.isReversed() ? BinaryOperatorType.GE_BINOP : BinaryOperatorType.LE_BINOP;
		ExpressionASTNode conditionExpr = new BinaryOperatorExpressionNode(null, fakeToken, conditionalComp, counterVarExpr, counterEndValueExpr);

		conditionExpr.setExpressionType(VarType.BOOLEAN_PRIMITIVE_TYPE);
		
		ExpressionASTNode mutExpr = new BinaryOperatorExpressionNode(null, fakeToken, node.isReversed() ? BinaryOperatorType.SUB_BINOP : BinaryOperatorType.ADD_BINOP, counterVarExpr, new IntegerValueExpressionNode(null, null, 1));
		mutExpr.setExpressionType(VarType.INTEGER_PRIMITIVE_TYPE);
		
		WhileStatementASTNode whileStmt = new WhileStatementASTNode(null, conditionExpr, node.getBody());
		whileStmt.addBodyStatement(
			new VariableAssignmentASTNode(null,
				counterVarExpr,
				mutExpr
			)
		);
		
		scopedStmt.addChild(whileStmt);
		
		return scopedStmt.accept(this, ctx);
	}

	@Override
	public List<JVMBytecodeEntity> visit(ForEachStatementASTNode node, ExpressionVisitorContext ctx) {
		Token fakeToken = new Token(node.getLineNumber(), node.getLineOffset(), null, null);
		
		ExpressionASTNode arrayExpr = node.getArrayExpression();
		
		String forCounter = newFreshVariable();
		String arrayElementVarname = node.getIterVariable();
		
		ExpressionASTNode counterExpr = new VariableExpressionNode(null, new Token(node.getLineNumber(), node.getLineOffset(), TokenType.IDENTIFIER, forCounter));
		counterExpr.setExpressionType(VarType.INTEGER_PRIMITIVE_TYPE);
		
		ExpressionASTNode lvalue = new VariableExpressionNode(null, new Token(node.getLineNumber(), node.getLineOffset(), TokenType.IDENTIFIER, arrayElementVarname));
		lvalue.setExpressionType(((ArrayType) arrayExpr.getExpressionType()).getElementType());
		
		VariableDeclarationASTNode counterDeclNode = new VariableDeclarationASTNode(null, VarType.INTEGER_PRIMITIVE_TYPE, fakeToken, arrayElementVarname);
		counterDeclNode.addChild(
			new VariableAssignmentASTNode(
				null, 
				lvalue, 
				new ArrayAccessExpressionNode(null, fakeToken, node.getArrayExpression(), counterExpr)
			)
		);
				
		CompoundStatementASTNode loopBody = new CompoundStatementASTNode(null);
		loopBody.addChild(
			counterDeclNode
		);
		loopBody.addChildren(
			node.getBody().getChildren()
		);
		
		return new ForInRangeStatementASTNode(
			null,
			forCounter,
			new IntegerValueExpressionNode(null, fakeToken, 1),
			new PropertyAccessExpressionNode(
				null, 
				fakeToken, 
				node.getArrayExpression(), 
				new Token(node.getLineNumber(), node.getLineOffset(), TokenType.IDENTIFIER, "length")
			),
			node.isReversed(),
			loopBody
		).accept(this, ctx);
	}

	@Override
	public List<JVMBytecodeEntity> visit(WhileStatementASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		if (node.getToken() != null)
			instructions.add(new JVMBytecodeDirective("line", node.getLineNumber()));
		
		String loopStartLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
		
		instructions.add(new JVMBytecodeLabel(loopStartLabel));

		String loopBodyLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
		String loopConditionalLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
		String loopEndLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);

		instructions.addAll(
			node.getConditionExpression().accept(expressionVisitor, ctx.toBranchContext(false, loopBodyLabel, loopEndLabel))
		);
		instructions.addAll(
			Arrays.asList(
				new JVMBytecodeLabel(loopBodyLabel)
			)
		);
		instructions.addAll(
			node.getBody().accept(this, new ExpressionVisitorContext(
											ctx.localVarCtx,
											ctx.labelCounter,
											null,
											null,
											loopStartLabel,
											loopConditionalLabel,
											loopEndLabel
										))
		);
		instructions.addAll(
			Arrays.asList(
				new JVMBytecodeInstruction("goto", loopStartLabel),
				new JVMBytecodeLabel(loopEndLabel)
			)
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(BreakStatementASTNode node, ExpressionVisitorContext ctx) {
		return Arrays.asList(
					new JVMBytecodeDirective("line", node.getLineNumber()),
					new JVMBytecodeInstruction("goto", ctx.loopEndLabel)
				);
	}

	@Override
	public List<JVMBytecodeEntity> visit(ContinueStatementASTNode node, ExpressionVisitorContext ctx) {
		return Arrays.asList(
					new JVMBytecodeDirective("line", node.getLineNumber()),
					new JVMBytecodeInstruction("goto", ctx.loopConditionalLabel) // fixme
				);
	}

	@Override
	public List<JVMBytecodeEntity> visit(PrintStatementASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		instructions.add(new JVMBytecodeDirective("line", node.getLineNumber()));
		
		JVMBytecodeInstruction getStdoutInstr = new JVMBytecodeInstruction("getstatic", "java/lang/System/out", "Ljava/io/PrintStream;");
		JVMBytecodeInstruction invokePrint = new JVMBytecodeInstruction("invokevirtual", "java/io/PrintStream/print(Ljava/lang/String;)V");
		
		List<JVMBytecodeInstruction> printSpaceInstrs = Arrays.asList(
			getStdoutInstr,
			new JVMBytecodeInstruction("ldc", "\" \""),
			invokePrint
		);
		
		boolean printSeparatorFlag = false;
		
		for (ASTNode child: node.getChildren()) {
			if (printSeparatorFlag)
				instructions.addAll(printSpaceInstrs);
			else
				printSeparatorFlag = true;
			
			ExpressionASTNode arg = (ExpressionASTNode) child;
			
			List<JVMBytecodeEntity> computeArgInstructions = arg.accept(expressionVisitor, ctx.toBranchContext(null));
			
			VarType argType = arg.getExpressionType();
			
			String typeDescriptor = getMangledTypeName(argType);
			
			instructions.add(getStdoutInstr);
			instructions.addAll(computeArgInstructions);
			instructions.add(
				new JVMBytecodeInstruction("invokevirtual", "java/io/PrintStream/print(" + typeDescriptor + ")V")
			);
		}
		
		instructions.addAll(
			Arrays.asList(
				getStdoutInstr,
				new JVMBytecodeInstruction("invokevirtual", "java/io/PrintStream/println()V")
			)
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(ExpressionASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		instructions.add(
			new JVMBytecodeDirective("line", node.getLineNumber())
		);
		
		if (node.getExpressionType() == VarType.BOOLEAN_PRIMITIVE_TYPE) {
			String thenLabel = CodeEmitterUtils.getOrAllocateLabel(ctx.thenLabel, ctx.labelCounter);
			String elseLabel = CodeEmitterUtils.getOrAllocateLabel(ctx.elseLabel, ctx.labelCounter);
			
			instructions.addAll(
				node.accept(expressionVisitor, ctx.toBranchContext(null, thenLabel, elseLabel))
			);
			
			String endLabel = CodeEmitterUtils.allocateLabel(ctx.labelCounter);
			
			if (ctx.thenLabel == null) {
				instructions.addAll(
					Arrays.asList(
						new JVMBytecodeInstructionLabeled(thenLabel, "iconst_1"),
						new JVMBytecodeInstruction("goto", endLabel)
					)
				);
			}
			
			if (ctx.elseLabel == null) {
				instructions.addAll(
					Arrays.asList(
							new JVMBytecodeInstructionLabeled(elseLabel, "iconst_0"),
							new JVMBytecodeLabel(endLabel)
						)
				);
			}
		} else {
			instructions.addAll(
				node.accept(expressionVisitor, ctx.toBranchContext(null))
			);
		}
		
		return instructions;
	}
}
