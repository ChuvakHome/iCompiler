package ru.itmo.compiler.codegen.jvm.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.itmo.compiler.codegen.jvm.JVMBytecodeEntity;
import ru.itmo.compiler.codegen.jvm.JVMBytecodeInstruction;
import ru.itmo.compiler.codegen.jvm.utils.JVMBytecodeUtils;
import ru.itmo.compiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.ExpressionVisitorContext;
import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.visitor.ExpressionNodeVisitor;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.EmptyExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ImplicitCastExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

public class JVMCodeEmitterExpressionVisitor implements ExpressionNodeVisitor<List<JVMBytecodeEntity>, ExpressionVisitorContext> {
	private static final Map<VarType, String> OPCODE_PREFIX_MAPPER = Map.ofEntries(
		Map.entry(VarType.BOOLEAN_PRIMITIVE_TYPE, "i"),
		Map.entry(VarType.INTEGER_PRIMITIVE_TYPE, "i"),
		Map.entry(VarType.REAL_PRIMITIVE_TYPE, "f")
	);
	
	private static String getPrefixByType(VarType varType) {
		String prefix = OPCODE_PREFIX_MAPPER.get(varType);
		
		if (prefix != null)
			return prefix;
		
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(BooleanValueExpressionNode node, ExpressionVisitorContext ctx) {
		return Arrays.asList(
					new JVMBytecodeInstruction("iconst_" + (node.getValue() ? 1 : 0)) 
				);
	}

	@Override
	public List<JVMBytecodeEntity> visit(IntegerValueExpressionNode node, ExpressionVisitorContext ctx) {
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
	public List<JVMBytecodeEntity> visit(RealValueExpressionNode node, ExpressionVisitorContext ctx) {
		return Arrays.asList(new JVMBytecodeInstruction("ldc", node.getValue()));
	}

	@Override
	public List<JVMBytecodeEntity> visit(VariableExpressionNode node, ExpressionVisitorContext ctx) {
		String varName = node.getVariable();
		VarType varType = node.getExpressionType();
		
		final String opcode = getPrefixByType(varType) + "load";
		
		return Arrays.asList(
					new JVMBytecodeInstruction(opcode, ctx.getLocalVariableContext().getLocalVarIndex(varName))
				);
	}

	@Override
	public List<JVMBytecodeEntity> visit(RoutineCallExpressionNode node, ExpressionVisitorContext ctx) {
		List<JVMBytecodeEntity> instructions = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(JVMCodeEmitterVisitor.PROGRAM_CLASS_NAME)
			.append('/')
			.append(node.getRoutineName())
			.append('(')
		;
		
		FunctionType routineType = node.getRoutineType();
		
		Iterator<ExpressionASTNode> argsIter = node.getArguments().iterator();
		Iterator<VarType> argTypesIter = routineType.getArgumentsTypes().values().iterator();
		
		while (argsIter.hasNext() && argTypesIter.hasNext()) {
			ExpressionASTNode arg = argsIter.next();
			VarType actualArgType = arg.getExpressionType();
			
			VarType requiredArgType = argTypesIter.next();
			
			instructions.addAll(arg.accept(this, ctx));
//			instructions.addAll(JVMBytecodeUtils.getConvertInstruction(requiredArgType, actualArgType));
		}
		
		sb.append(')');
		sb.append(JVMBytecodeUtils.getTypeDescriptor(routineType.getReturnType()));
		
		instructions.add(
			new JVMBytecodeInstruction("invokestatic", sb.toString())
		);
		
		return instructions;
	}

	@Override
	public List<JVMBytecodeEntity> visit(UnaryOperatorExpressionNode node, ExpressionVisitorContext ctx) {
		return null;
	}
	
//	private List<JVMBytecodeEntity> emitSCEJVMCode(BinaryOperatorExpressionNode node, ExpressionVisitorContext ctx) {
//		List<JVMBytecodeEntity> instructions = new ArrayList<>();
//		instructions.addAll(
//			node.accept(this, ctx)
//		);
//	}
	
	@Override
	public List<JVMBytecodeEntity> visit(BinaryOperatorExpressionNode node, ExpressionVisitorContext ctx) {
//		List<JVMBytecodeEntity> instructions = new ArrayList<>();
//		instructions.addAll(
//			node.accept(this, ctx)
//		);
//		
//		BinaryOperatorType binopType = node.getBinaryOperatorType();
//		
//		switch (binopType) {
//			case ADD_BINOP:
//				
//			case AND_BINOP:
//			case OR_BINOP:
////				instructions.addAll(emitSCEJVMCode(node, ctx));
//		}
		
		return null;
	}

	@Override
	public List<JVMBytecodeEntity> visit(PropertyAccessExpressionNode node, ExpressionVisitorContext ctx) {
		return null;
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(ImplicitCastExpressionNode node, ExpressionVisitorContext ctx) {
		ExpressionASTNode castExpr = node.getArgument();
		
		List<JVMBytecodeEntity> instructions = new ArrayList<>(
			castExpr.accept(this, ctx)
		);
		
		VarType targetType = node.getTargetType();
		VarType actualCastExprType = castExpr.getExpressionType();
		
		if (!targetType.equals(actualCastExprType)) {
			if (actualCastExprType == VarType.INTEGER_PRIMITIVE_TYPE && targetType == VarType.REAL_PRIMITIVE_TYPE)
				instructions.add(new JVMBytecodeInstruction("i2f"));
			else if (actualCastExprType == VarType.REAL_PRIMITIVE_TYPE && targetType == VarType.INTEGER_PRIMITIVE_TYPE)
				instructions.add(new JVMBytecodeInstruction("f2i"));
		}
		
		return instructions;
	}
	
	@Override
	public List<JVMBytecodeEntity> visit(EmptyExpressionNode node, ExpressionVisitorContext ctx) {
		return Collections.emptyList();
	}
}
