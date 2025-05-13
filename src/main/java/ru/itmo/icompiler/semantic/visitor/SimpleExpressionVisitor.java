package ru.itmo.icompiler.semantic.visitor;

import java.util.Iterator;

import ru.itmo.icompiler.semantic.FunctionType;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
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
		
		FunctionType routineType = ctx.getScope().deepLookupRoutine(routineName);
		node.setRoutineType(routineType);
		
		Iterator<ExpressionASTNode> argsIter = node.getArguments().iterator();
		Iterator<VarType> argTypesIter = routineType.getArgumentsTypes().values().iterator();
		
		while (argsIter.hasNext() && argTypesIter.hasNext()) {
			ExpressionASTNode arg = argsIter.next();

			VarType requiredArgType = argTypesIter.next();
			
			arg.accept(this, ctx);
			addCastExpr(requiredArgType, arg);
		}
		
		return ctx;
	}

	@Override
	public SemanticContext visit(UnaryOperatorExpressionNode node, SemanticContext ctx) {
		node.getValue().accept(this, ctx);
		
		return ctx;
	}

	@Override
	public SemanticContext visit(BinaryOperatorExpressionNode node, SemanticContext ctx) {
		node.getLeftChild().accept(this, ctx);
		node.getRightChild().accept(this, ctx);
		
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
