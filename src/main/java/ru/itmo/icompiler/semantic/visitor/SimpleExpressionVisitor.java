package ru.itmo.icompiler.semantic.visitor;

import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

public class SimpleExpressionVisitor extends AbstractExpressionASTVisitor {

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
		return ctx;
	}

	@Override
	public SemanticContext visit(UnaryOperatorExpressionNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(BinaryOperatorExpressionNode node, SemanticContext ctx) {
		return ctx;
	}

	@Override
	public SemanticContext visit(PropertyAccessExpressionNode node, SemanticContext ctx) {
		return ctx;
	}
}
