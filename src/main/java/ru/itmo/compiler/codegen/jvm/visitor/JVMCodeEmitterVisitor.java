package ru.itmo.compiler.codegen.jvm.visitor;

import static ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils.classSpecs;
import static ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils.fieldSpecs;
import static ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils.methodSpecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ru.itmo.compiler.codegen.jvm.JVMBytecodeClass;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeEntity;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeField;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeInstruction;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeMethod;
import ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils;
import ru.itmo.compiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.ExpressionVisitorContext;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.BreakStatementASTNode;
import ru.itmo.icompiler.syntax.ast.CompoundStatementASTNode;
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
import ru.itmo.icompiler.syntax.ast.WhileStatementASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
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
		
		public int getLocalVarIndex(String varName) {
			Integer index = indices.get(varName);
			
			if (index == null && parentContext != null)
				index = parentContext.getLocalVarIndex(varName);
			
			return index;
		}
	}
	
	public static class ExpressionVisitorContext {
		private LocalVariableContext localVarCtx;
		
		private int labelCounter;
		
		private String thenLabel; // short-circuit eval
		private String elseLabel; // short-circuit eval
		
		public ExpressionVisitorContext(LocalVariableContext localVarCtx, int labelCounter, String thenLabel, String elseLabel) {
			this.localVarCtx = localVarCtx;
			this.labelCounter = labelCounter;
			
			this.thenLabel = thenLabel;
			this.elseLabel = elseLabel;
		}
		
		public ExpressionVisitorContext() {
			this(new LocalVariableContext(), 0, null, null);
		}
		
		public LocalVariableContext getLocalVariableContext() {
			return localVarCtx;
		}
		
		public int getLabelCounter() {
			return labelCounter;
		}
		
		public void incLabelCounter(int delta) {
			labelCounter += delta;
		}
		
		public String getThenLabel() {
			return thenLabel;
		}
		
		public String getElseLabel() {
			return elseLabel;
		}
	}
	
	private int maxLocalVarNumber = 0;
	
	private Map<String, VarType> globalVarTypes;
	
	private Stack<Limits> contextStack;
	private JVMCodeEmitterExpressionVisitor expressionVisitor;
	
	private FunctionType currentRoutineType;
	
	public JVMCodeEmitterVisitor() {
		globalVarTypes = new HashMap<>();
		contextStack = new Stack<>();
		expressionVisitor = new JVMCodeEmitterExpressionVisitor();
	}
	
	public static final Map<VarType, String> PRIMITIVE_TYPE_MAPPER = Map.ofEntries(
		Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "Z"),
		Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "I"),
		Map.entry(VarType.REAL_PRIMITIVE_TYPE, "F"),
		
		Map.entry(VarType.VOID_TYPE, "V")
	);
	
	private static String getJVMTypeDescriptor(VarType varType) {
		switch (varType.getTag()) {
			case ARRAY: {
				ArrayType arrayType = (ArrayType) varType;
				return "[" + getJVMTypeDescriptor(arrayType.getElementType());
			}
			case RECORD: {
				return "L" + PROGRAM_JVM_PACKAGE + "/" + JVMBytecodeUtils.getTypeDescriptor(varType) + ";"; // to be continued
			}
			default:
				return PRIMITIVE_TYPE_MAPPER.get(varType);
		}
	}
		
	private JVMBytecodeField processGlobalVarDecl(VariableDeclarationASTNode node) {
		String varName = node.getVarName();
		VarType varType = node.getVarType();
		
		globalVarTypes.put(varName, varType);
		
		String fieldName = varName;
		String typeDescriptor = getJVMTypeDescriptor(varType);
		
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
		
		for (ASTNode child: node.getChildren()) {
			switch (child.getNodeType()) {
				case VAR_DECL_NODE: {
					JVMBytecodeField field = processGlobalVarDecl((VariableDeclarationASTNode) child);
					programClassFields.add(field);
					
					break;
				}
				case ROUTINE_DEF_NODE: {
					JVMBytecodeMethod jvmMethod = (JVMBytecodeMethod) child.accept(this, ctx).get(0);
					programClassMethods.add(jvmMethod);
					
					break;
				}
				default:
					break;
			}
		}
		
		return Arrays.asList(
					new JVMBytecodeClass(
						classSpecs(JVMBytecodeClass.AccessSpec.PUBLIC, JVMBytecodeClass.AccessSpec.FINAL),
						PROGRAM_CLASS_NAME,
						programClassFields,
						programClassMethods
					)
				);
	}

	@Override
	public List<JVMBytecodeEntity> visit(CompoundStatementASTNode node, ExpressionVisitorContext ctx) {
		Limits topCtx = contextStack.peek();
		contextStack.add(topCtx.clone());
		
		LocalVariableContext subctx = new LocalVariableContext(ctx.localVarCtx);
		ExpressionVisitorContext subexprctx = new ExpressionVisitorContext(subctx, ctx.labelCounter, ctx.thenLabel, ctx.elseLabel);
		
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
	public List<JVMBytecodeEntity> visit(VariableDeclarationASTNode node, ExpressionVisitorContext ctx) {
		String varName = node.getVarName();
		
		int lvIndex = contextStack.peek().localVariablesCount++;
		
		ctx.localVarCtx.addLocalVariable(varName, lvIndex);
		
		if (node.getChildren().isEmpty())
			return Collections.emptyList();
		
		return node.getChild(0).accept(this, ctx);
	}

	@Override
	public List<JVMBytecodeEntity> visit(VariableAssignmentASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> assignInsrs = new ArrayList<>();
		
		ExpressionASTNode leftSideNode = node.getLeftSide();
		VarType requiredType = leftSideNode.getExpressionType();
		
		ExpressionASTNode valueNode = node.getValueNode();
		
		List<JVMBytecodeEntity> valueCompInstrs = valueNode.accept(expressionVisitor, ctx);
		assignInsrs.addAll(valueCompInstrs);
		
		switch (leftSideNode.getExpressionNodeType()) {
			case VARIABLE_EXPR_NODE: {
				VariableExpressionNode varExprNode = (VariableExpressionNode) leftSideNode;
				
				assignInsrs.add(
					new JVMBytecodeInstruction(
							JVMBytecodeUtils.getOpcodePrefix(requiredType) + "store", 
							ctx.localVarCtx.getLocalVarIndex(varExprNode.getVariable())
					)
				);
				
				break;
			}
		}
		
		return assignInsrs;
	}

	@Override
	public List<JVMBytecodeEntity> visit(TypeDeclarationASTNode node, ExpressionVisitorContext ctx) {
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(RoutineDeclarationASTNode node, ExpressionVisitorContext ctx) {
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(RoutineDefinitionASTNode node, ExpressionVisitorContext ctx) {
		RoutineDeclarationASTNode routineHeader = node.getRoutineDeclaration();
		
		String methodName = routineHeader.getRoutineName();
		
		int argsCount = routineHeader.getArgumentsDeclarations().size();
		
		List<String> argsTypesDescriptors = new ArrayList<>(argsCount);
		String returnTypeDescriptor = getJVMTypeDescriptor(routineHeader.getResultType());
		
		int argnum = 0;
		
		LocalVariableContext routineLVCtx = new LocalVariableContext(ctx.localVarCtx);
		ExpressionVisitorContext routineExprCtx = new ExpressionVisitorContext(routineLVCtx, 0, null, null);
		
		for (VariableDeclarationASTNode argDecl: routineHeader.getArgumentsDeclarations()) {
			String argName = argDecl.getVarName();
			VarType argType = argDecl.getVarType();
			
			Stack<Integer> indicesStack = new Stack<>();
			indicesStack.add(argnum++);
			routineLVCtx.addLocalVariable(argName, argnum++);
			
			String jvmTypeDescriptor = JVMCodeEmitterVisitor.getJVMTypeDescriptor(argType);
			
			argsTypesDescriptors.add(jvmTypeDescriptor);
		}
		
		contextStack.add(new Limits(argsCount, 0));
		maxLocalVarNumber = argsCount;
		
		currentRoutineType = new FunctionType(
			routineHeader.getArgumentsDeclarations().stream().map(VariableDeclarationASTNode::getVarType).toList(),
			routineHeader.getResultType()
		);
		
		List<JVMBytecodeEntity> routineInstructions = node.getBody().accept(this, routineExprCtx);
		
		if (routineHeader.getResultType() == VarType.VOID_TYPE)
			routineInstructions.add(new JVMBytecodeInstruction("return"));
		
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
					10,
					routineInstructions
				)
			);
	}

	@Override
	public List<JVMBytecodeEntity> visit(ReturnStatementASTNode node, ExpressionVisitorContext ctx) {
		ExpressionASTNode returnValueExprNode = node.getResultNode();
		
		VarType requiredReturnType = currentRoutineType.getReturnType();
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>(); 
		instructions.addAll(
			returnValueExprNode.accept(expressionVisitor, ctx)
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
		return Collections.emptyList();
	}

	@Override
	public List<JVMBytecodeEntity> visit(ForInRangeStatementASTNode node, ExpressionVisitorContext ctx) {
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(ForEachStatementASTNode node, ExpressionVisitorContext ctx) {
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(WhileStatementASTNode node, ExpressionVisitorContext ctx) {
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(BreakStatementASTNode node, ExpressionVisitorContext ctx) {
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(PrintStatementASTNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
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
			
			List<JVMBytecodeEntity> computeArgInstructions = arg.accept(expressionVisitor, ctx);
			
			VarType argType = arg.getExpressionType();
			
			String typeDescriptor = getJVMTypeDescriptor(argType);
			
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
		return node.accept(expressionVisitor, ctx);
	}
}
