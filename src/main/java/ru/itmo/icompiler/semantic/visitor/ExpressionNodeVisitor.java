package ru.itmo.icompiler.semantic.visitor;

import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;

public interface ExpressionNodeVisitor<R, A> {
	R visit(BooleanValueExpressionNode node, A arg);
	R visit(IntegerValueExpressionNode node, A arg);
	R visit(RealValueExpressionNode node, A arg);
	
	R visit(VariableExpressionNode node, A arg);

	R visit(RoutineCallExpressionNode node, A arg);
	
	R visit(UnaryOperatorExpressionNode node, A arg);
	
	R visit(BinaryOperatorExpressionNode node, A arg);
	
	R visit(PropertyAccessExpressionNode node, A arg);
}
