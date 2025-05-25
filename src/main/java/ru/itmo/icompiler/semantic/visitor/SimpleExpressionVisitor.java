package ru.itmo.icompiler.semantic.visitor;

import java.util.Iterator;

import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.syntax.ast.expression.ArrayAccessExpressionNode;
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

public class SimpleExpressionVisitor extends AbstractExpressionASTVisitor {
	static void addCastExpr(VarType targetType, ExpressionASTNode expr) {
		VarType actualType = expr.getExpressionType();
		
		if (!targetType.equals(actualType))
			expr.insertAdjacentBefore(new ImplicitCastExpressionNode(null, targetType, expr));
	}
	
	@Override
	public SemanticContext visit(BooleanValueExpressionNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(IntegerValueExpressionNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(RealValueExpressionNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(VariableExpressionNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(RoutineCallExpressionNode node, SemanticContext ctx) {
		String routineName = node.getRoutineName();
		
		FunctionType routineType = (FunctionType)ctx.getScope().deepLookup(routineName);
		node.setRoutineType(routineType);
		
		if (routineType == null)
			return ctx;
		
		Iterator<ExpressionASTNode> argsIter = node.getArguments().iterator();
		Iterator<VarType> argTypesIter = routineType.getArgumentsTypes().values().iterator();
		
		int argnum = 0;
		
		while (argsIter.hasNext() && argTypesIter.hasNext()) {
			ExpressionASTNode arg = argsIter.next();

			VarType requiredArgType = argTypesIter.next();
			
			arg.accept(this, ctx);
			
			if (!arg.getExpressionType().equals(requiredArgType))
				node.setArgument(argnum, new ImplicitCastExpressionNode(null, requiredArgType, arg));
			
			++argnum;
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(UnaryOperatorExpressionNode node, SemanticContext ctx) {
		node.getValue().accept(this, ctx);
		
		return ctx;
	}
	
	private void processComparisonOperators(BinaryOperatorExpressionNode node) {
		ExpressionASTNode leftChild = node.getLeftChild();
		VarType leftType = leftChild.getExpressionType();
		
		ExpressionASTNode rightChild = node.getRightChild();
		VarType rightType = rightChild.getExpressionType();
		
		VarType commonType = leftType != VarType.REAL_PRIMITIVE_TYPE && rightType != VarType.REAL_PRIMITIVE_TYPE
								? VarType.INTEGER_PRIMITIVE_TYPE
								: VarType.REAL_PRIMITIVE_TYPE
								;
								
		if (!leftType.equals(commonType))
			node.setLeftChild(new ImplicitCastExpressionNode(null, commonType, leftChild));
		
		if (!rightType.equals(commonType))
			node.setRightChild(new ImplicitCastExpressionNode(null, commonType, rightChild));
	}

	@Override
	public SemanticContext visit(BinaryOperatorExpressionNode node, SemanticContext ctx) {
		ExpressionASTNode leftChild = node.getLeftChild();
		leftChild.accept(this, ctx);
		VarType leftType = leftChild.getExpressionType();
		
		ExpressionASTNode rightChild = node.getRightChild();
		rightChild.accept(this, ctx);
		VarType rightType = rightChild.getExpressionType();
		
		VarType exprType = node.getExpressionType();
		
		switch (node.getBinaryOperatorType()) {
			case ADD_BINOP:
			case SUB_BINOP:
			case MUL_BINOP:
			case DIV_BINOP:
			case MOD_BINOP:
			case AND_BINOP:
			case OR_BINOP:
			case XOR_BINOP:
				if (!leftType.equals(exprType))
					node.setLeftChild(new ImplicitCastExpressionNode(null, exprType, leftChild));
				
				if (!rightType.equals(exprType))
					node.setRightChild(new ImplicitCastExpressionNode(null, exprType, rightChild));
					
				break;
			case EQ_BINOP:
			case NE_BINOP:
			case LT_BINOP:
			case LE_BINOP:
			case GT_BINOP:
			case GE_BINOP:
				processComparisonOperators(node);
				break;
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(ArrayAccessExpressionNode node, SemanticContext ctx) {
		node.getHolder().accept(this, ctx);
		
		ExpressionASTNode indexExpr = node.getIndex();
		indexExpr.accept(this, ctx);
		
		if (!indexExpr.getExpressionType().equals(VarType.INTEGER_PRIMITIVE_TYPE))
			node.setIndex(new ImplicitCastExpressionNode(null, VarType.INTEGER_PRIMITIVE_TYPE, indexExpr));
		
		return ctx;
	}
	
	@Override
	public SemanticContext visit(PropertyAccessExpressionNode node, SemanticContext ctx) {
		return ctx;
	}
	
	@Override
	public SemanticContext visit(ImplicitCastExpressionNode node, SemanticContext ctx) {
		node.getArgument().accept(this, ctx);
		
		return ctx;
	}
	
	@Override
	public SemanticContext visit(EmptyExpressionNode node, SemanticContext ctx) {
		return ctx;
	}
}
