package ru.itmo.icompiler.semantic.visitor;

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
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public interface ASTVisitor<R, A> {
	R visit(ProgramASTNode node, A arg);
	
	R visit(CompoundStatementASTNode node, A arg);
	R visit(WhileBodyStatementASTNode node, A arg);

	R visit(VariableDeclarationASTNode node, A arg);
	R visit(VariableAssignmentASTNode node, A arg);
	
	R visit(TypeDeclarationASTNode node, A arg);
	
	R visit(RoutineDeclarationASTNode node, A arg);
	R visit(RoutineDefinitionASTNode node, A arg);
	R visit(ReturnStatementASTNode node, A arg);
	
	R visit(IfThenElseStatementASTNode node, A arg);
	
	R visit(ForInRangeStatementASTNode node, A arg);
	R visit(ForEachStatementASTNode node, A arg);
	R visit(WhileStatementASTNode node, A arg);
	R visit(BreakStatementASTNode node, A arg);
	R visit(ContinueStatementASTNode node, A arg);

	R visit(PrintStatementASTNode node, A arg);
	
	R visit(ExpressionASTNode node, A arg);
}
