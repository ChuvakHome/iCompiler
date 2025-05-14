package ru.itmo.compiler.codegen.jvm.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.itmo.compiler.codegen.jvm.JVMBytecodeEntity;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeInstruction;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeInstruction.JVMBytecodeInstructionLabeled;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeInstruction.JVMBytecodeLabel;
import ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils;
import ru.itmo.compiler.codegen.jvm.visitor.JVMCodeEmitterExpressionVisitor.BranchContext;
import ru.itmo.compiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.IntCounter;
import ru.itmo.compiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.LocalVariableContext;
import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.expression.ArrayAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode.BinaryOperatorType;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.EmptyExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode.ExpressionNodeType;
import ru.itmo.icompiler.syntax.ast.expression.ImplicitCastExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode.UnaryOperatorType;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

public class JVMCodeEmitterExpressionVisitor implements ExpressionNodeVisitor<List<JVMBytecodeEntity>, BranchContext> {
	public static class BranchContext {
		private String thenLabel;
		private String elseLabel;
		private LocalVariableContext localVariableContext;
		private IntCounter labelCounter;
		
		public BranchContext(String thenLabel, String elseLabel, LocalVariableContext localVariableContext, IntCounter labelCounter) {
			this.thenLabel = thenLabel;
			this.elseLabel = elseLabel;
			this.localVariableContext = localVariableContext;
			this.labelCounter = labelCounter;
		}
		
		public BranchContext copy(String thenLabel, String elseLabel) {
			return new BranchContext(
						thenLabel, 
						elseLabel, 
						localVariableContext, 
						labelCounter
					);
		}
	}
	
	private static final Map<VarType, String> OPCODE_PREFIX_MAPPER = Map.ofEntries(
		Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "i"),
		Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "i"),
		Map.entry(VarType.REAL_PRIMITIVE_TYPE, "f")
	);
	
	private static final Map<BinaryOperatorType, String> INTEGER_COMPARISON_OPCODE_MAPPER = Map.ofEntries(
			Map.entry(BinaryOperatorType.LT_BINOP, "if_icmplt"),
			Map.entry(BinaryOperatorType.LE_BINOP, "if_icmple"),
			
			Map.entry(BinaryOperatorType.EQ_BINOP, "if_icmpeq"),
			Map.entry(BinaryOperatorType.NE_BINOP, "if_icmpne"),
			
			Map.entry(BinaryOperatorType.GT_BINOP, "if_icmpgt"),
			Map.entry(BinaryOperatorType.GE_BINOP, "if_icmpge")
	);
	
	private static final Map<BinaryOperatorType, String> REAL_COMPARISON_OPCODE_MAPPER = Map.ofEntries(
			Map.entry(BinaryOperatorType.EQ_BINOP, "ifeq"),
			Map.entry(BinaryOperatorType.NE_BINOP, "ifne"),
			
			Map.entry(BinaryOperatorType.LT_BINOP, "iflt"),
			Map.entry(BinaryOperatorType.LE_BINOP, "ifle"),
			
			Map.entry(BinaryOperatorType.GT_BINOP, "ifgt"),
			Map.entry(BinaryOperatorType.GE_BINOP, "ifge")
	);
	
	private static final Map<BinaryOperatorType, String> BINOP_OPCODE_MAPPER = Map.ofEntries(
			Map.entry(BinaryOperatorType.ADD_BINOP, "add"),
			Map.entry(BinaryOperatorType.SUB_BINOP, "sub"),
			
			Map.entry(BinaryOperatorType.MUL_BINOP, "mul"),
			Map.entry(BinaryOperatorType.DIV_BINOP, "div"),
			Map.entry(BinaryOperatorType.MOD_BINOP, "rem"),
			
			Map.entry(BinaryOperatorType.AND_BINOP, "iand"),
			Map.entry(BinaryOperatorType.OR_BINOP, "ior"),
			Map.entry(BinaryOperatorType.XOR_BINOP, "ixor")
	);
	
	private static final Map<BinaryOperatorType, BinaryOperatorType> REVERSED_BINOP_TYPE_MAPPER = Map.ofEntries(
			Map.entry(BinaryOperatorType.LT_BINOP, BinaryOperatorType.GE_BINOP),
			Map.entry(BinaryOperatorType.LE_BINOP, BinaryOperatorType.GT_BINOP),
			
			Map.entry(BinaryOperatorType.EQ_BINOP, BinaryOperatorType.NE_BINOP),
			Map.entry(BinaryOperatorType.NE_BINOP, BinaryOperatorType.EQ_BINOP),
			
			Map.entry(BinaryOperatorType.GT_BINOP, BinaryOperatorType.LE_BINOP),
			Map.entry(BinaryOperatorType.GE_BINOP, BinaryOperatorType.LT_BINOP)
	);
	
	private static String getPrefixByType(VarType varType) {
		String prefix = OPCODE_PREFIX_MAPPER.get(varType);
		
		if (prefix != null)
			return prefix;
		
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(BooleanValueExpressionNode node, BranchContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		instructions.add(
			new JVMBytecodeInstruction("iconst_" + (node.getValue() ? 1 : 0)) 
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(IntegerValueExpressionNode node, BranchContext ctx) {
		int val = node.getValue();
		
		JVMBytecodeEntity iconstInstr = null;
		
		if (val == -1)
			iconstInstr = new JVMBytecodeInstruction("iconst_m1");
		else if (val >= 0 && val <= 5)
			iconstInstr = new JVMBytecodeInstruction("iconst_" + val);
		else if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE)
			iconstInstr = new JVMBytecodeInstruction("bipush", val);
		else
			iconstInstr = new JVMBytecodeInstruction("ldc", val);
		
		return Arrays.asList(iconstInstr);
	}

	@Override
	public List<JVMBytecodeEntity> visit(RealValueExpressionNode node, BranchContext ctx) {
		return Arrays.asList(new JVMBytecodeInstruction("ldc", node.getValue()));
	}

	@Override
	public List<JVMBytecodeEntity> visit(VariableExpressionNode node, BranchContext ctx) {
		String varName = node.getVariable();
		VarType varType = node.getExpressionType();
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		JVMBytecodeInstruction loadInstr;
		
		if (ctx.localVariableContext.containsLocalVarIndex(varName)) {
			final String opcode = getPrefixByType(varType) + "load";
			
			int lvIndex = ctx.localVariableContext.getLocalVarIndex(varName);
			
			loadInstr = 
				lvIndex <= 3
				? new JVMBytecodeInstruction(opcode + "_" + lvIndex)
				: new JVMBytecodeInstruction(opcode, lvIndex)
				;
		} else
			loadInstr = new JVMBytecodeInstruction(
								"getstatic", 
								JVMCodeEmitterVisitor.PROGRAM_CLASS_NAME + "/" + varName, 
								JVMBytecodeUtils.getTypeDescriptor(varType)
							);
		
		instructions.add(loadInstr);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(RoutineCallExpressionNode node, BranchContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(JVMCodeEmitterVisitor.PROGRAM_CLASS_NAME)
			.append('/')
			.append(node.getRoutineName())
			.append('(')
		;
		
		FunctionType routineType = node.getRoutineType();
		
		node.getArguments().forEach(child -> {
			ExpressionASTNode arg = (ExpressionASTNode) child;
			
			instructions.addAll(
				arg.accept(this, ctx)
			);
			
			sb.append(JVMBytecodeUtils.getTypeDescriptor(arg.getExpressionType()));
		});
		
		sb.append(')')
			.append(JVMBytecodeUtils.getTypeDescriptor(routineType.getReturnType()));
		
		instructions.add(
			new JVMBytecodeInstruction("invokestatic", sb.toString())
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(UnaryOperatorExpressionNode node, BranchContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		ExpressionASTNode unopValue = node.getValue(); 
		
		UnaryOperatorType unopType = node.getUnaryOperatorType();
		
		switch (unopType) {
			case PLUS_BINOP:
			case MINUS_BINOP:
				instructions.addAll(unopValue.accept(this, ctx));
				
				if (unopType == UnaryOperatorType.MINUS_BINOP)
					instructions.add(new JVMBytecodeInstruction(getPrefixByType(unopValue.getExpressionType()) + "neg"));
					
				break;
			case NOT_BINOP: {
				instructions.addAll(
					node.getValue().accept(this, new BranchContext(
										ctx.thenLabel,
										ctx.elseLabel,
										ctx.localVariableContext,
										ctx.labelCounter
									))
				);
			}
		}
		
		return instructions;
	}
	
	private List<JVMBytecodeInstruction> emitIntegerComparisonOpConditionalInstrs(BinaryOperatorType binopType, BranchContext ctx) {
		return Arrays.asList(
					new JVMBytecodeInstruction(INTEGER_COMPARISON_OPCODE_MAPPER.get(REVERSED_BINOP_TYPE_MAPPER.get(binopType)), ctx.elseLabel)
				);
	}
	
	private List<JVMBytecodeInstruction> emitRealComparisonOpConditionalInstrs(BinaryOperatorType binopType, BranchContext ctx) {
		List<JVMBytecodeInstruction> instructions = new ArrayList<>();
		
		switch (binopType) {
			case GT_BINOP:
			case GE_BINOP:
				instructions.add(new JVMBytecodeInstruction("fcmpg"));
				break;
			default:
				instructions.add(new JVMBytecodeInstruction("fcmpl"));
				break;
		}
		
		instructions.add(
			new JVMBytecodeInstruction(REAL_COMPARISON_OPCODE_MAPPER.get(REVERSED_BINOP_TYPE_MAPPER.get(binopType)), ctx.elseLabel)
		);
		
		return instructions;
	}
	
	private List<JVMBytecodeEntity> emitSCEJVMCode(BinaryOperatorExpressionNode node, BranchContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();

		Boolean isORBinop = node.getBinaryOperatorType() == BinaryOperatorType.OR_BINOP;
		
		String thenLabel = ctx.thenLabel;
		
		if (thenLabel == null) {
			thenLabel = "L" + ctx.labelCounter;
			ctx.labelCounter.incCounter();
		}
		
		String elseLabel = ctx.elseLabel;
		
		if (elseLabel == null) {
			elseLabel = "L" + ctx.labelCounter;
			ctx.labelCounter.incCounter();
		}
		
		String rightOperandLabel = "L" + ctx.labelCounter;
		ctx.labelCounter.incCounter();
		
		BranchContext leftOpCtx = ctx.copy(
				isORBinop ? thenLabel: rightOperandLabel,
				isORBinop ? rightOperandLabel : elseLabel
			);
		
		instructions.addAll(
			node.getLeftChild().accept(this, leftOpCtx)
		);
		instructions.add(
			isORBinop 
			? new JVMBytecodeInstruction("ifne", thenLabel)
			: new JVMBytecodeInstruction("ifeq", elseLabel)
		);
		instructions.add(new JVMBytecodeLabel(rightOperandLabel));
		instructions.addAll(
			node.getRightChild().accept(this, ctx)
		);
		
		String endLabel = "L" + ctx.labelCounter;
		ctx.labelCounter.incCounter();
		
		if (ctx.thenLabel == null) {
			instructions.addAll(
				Arrays.asList(
					new JVMBytecodeInstructionLabeled(thenLabel, "iconst_1"),
					new JVMBytecodeInstruction("goto", endLabel)
				)
			);
		}
		
		if (ctx.elseLabel == null) {
			instructions.add(
				new JVMBytecodeInstructionLabeled(elseLabel, "iconst_0")
			);
		}
		
		instructions.add(
			new JVMBytecodeLabel(endLabel)
		);
		
		return instructions;
	}
	
	private List<JVMBytecodeEntity> emitComparisonOpCode(BinaryOperatorExpressionNode node, BranchContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		instructions.addAll(node.getLeftChild().accept(this, ctx));
		instructions.addAll(node.getRightChild().accept(this, ctx));
		
		String thenLabel = "L" + ctx.labelCounter;
		ctx.labelCounter.incCounter();
		
		String elseLabel = "L" + ctx.labelCounter;
		ctx.labelCounter.incCounter();
		
		BranchContext subctx = ctx.copy(thenLabel, elseLabel);
		
		if (node.getLeftChild().getExpressionType() == VarType.INTEGER_PRIMITIVE_TYPE)
			instructions.addAll(emitIntegerComparisonOpConditionalInstrs(node.getBinaryOperatorType(), subctx));
		else
			instructions.addAll(emitRealComparisonOpConditionalInstrs(node.getBinaryOperatorType(), subctx));
		
		String endLabel = "L" + ctx.labelCounter;
		ctx.labelCounter.incCounter();
		
		instructions.addAll(
			Arrays.asList(
				new JVMBytecodeInstructionLabeled(thenLabel, "iconst_1"),
				new JVMBytecodeInstruction("goto", endLabel)
			)
		);
		
		instructions.addAll(
			Arrays.asList(
					new JVMBytecodeInstructionLabeled(elseLabel, "iconst_0"),
					new JVMBytecodeLabel(endLabel)
				)
		);
		
		return instructions;
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(BinaryOperatorExpressionNode node, BranchContext ctx) {		
		String instrPrefix = getPrefixByType(node.getExpressionType());
		
		BinaryOperatorType binop = node.getBinaryOperatorType();
		
		String opcode = BINOP_OPCODE_MAPPER.get(binop);
		
		if (binop == BinaryOperatorType.SUB_BINOP)
			System.out.println(opcode + " " + node.toString(0));
		
//		if (binop == BinaryOperatorType.AND_BINOP || binop == BinaryOperatorType.OR_BINOP)
//			return emitSCEJVMCode(node, ctx);
		
		if (opcode == null)
			return emitComparisonOpCode(node, ctx);
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		instructions.addAll(node.getLeftChild().accept(this, ctx));
		instructions.addAll(node.getRightChild().accept(this, ctx));
		
		instructions.add(
			binop == BinaryOperatorType.AND_BINOP || binop == BinaryOperatorType.OR_BINOP
			? new JVMBytecodeInstruction(opcode)
			: new JVMBytecodeInstruction(instrPrefix + opcode)
		);
		
		return instructions;
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(ArrayAccessExpressionNode node, BranchContext ctx) {
		
		
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(PropertyAccessExpressionNode node, BranchContext ctx) {
		return null;
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(ImplicitCastExpressionNode node, BranchContext ctx) {
		ExpressionASTNode castExpr = node.getArgument();
		
		VarType targetType = node.getTargetType();
		VarType actualCastExprType = castExpr.getExpressionType();
		
		if (targetType.equals(actualCastExprType))
			return Collections.emptyList();
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		if (actualCastExprType == VarType.BOOLEAN_PRIMITIVE_TYPE 
				&& (castExpr.getExpressionNodeType() == ExpressionNodeType.UNOP_EXPR_NODE 
				|| castExpr.getExpressionNodeType() == ExpressionNodeType.BINOP_EXPR_NODE
			)) {
			String thenLabel = "L" + ctx.labelCounter;
			ctx.labelCounter.incCounter();
			
			String elseLabel = "L" + ctx.labelCounter;
			ctx.labelCounter.incCounter();
			
			instructions.addAll(
				castExpr.accept(this, ctx.copy(thenLabel, elseLabel))
			);
			instructions.add(
				new JVMBytecodeInstruction("ifeq", elseLabel)
			);
			
			String endLabel = "L" + ctx.labelCounter;
			ctx.labelCounter.incCounter();
			
			instructions.addAll(
				Arrays.asList(
					new JVMBytecodeInstructionLabeled(thenLabel, "iconst_1"),
					new JVMBytecodeInstruction("goto", endLabel)
				)
			);
			
			instructions.addAll(
				Arrays.asList(
						new JVMBytecodeInstructionLabeled(elseLabel, "iconst_0"),
						new JVMBytecodeLabel(endLabel)
					)
			);
		} else {
			instructions.addAll(
				castExpr.accept(this, ctx)
			);
		}
	
		if (actualCastExprType == VarType.INTEGER_PRIMITIVE_TYPE && targetType == VarType.REAL_PRIMITIVE_TYPE)
			instructions.add(new JVMBytecodeInstruction("i2f"));
		else if (actualCastExprType == VarType.BOOLEAN_PRIMITIVE_TYPE && targetType == VarType.REAL_PRIMITIVE_TYPE)
			instructions.add(new JVMBytecodeInstruction("i2f"));
		else if (actualCastExprType == VarType.REAL_PRIMITIVE_TYPE && targetType == VarType.INTEGER_PRIMITIVE_TYPE)
			instructions.add(new JVMBytecodeInstruction("f2i"));
		
		return instructions;
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(EmptyExpressionNode node, BranchContext ctx) {
		return Collections.emptyList();
	}
}
