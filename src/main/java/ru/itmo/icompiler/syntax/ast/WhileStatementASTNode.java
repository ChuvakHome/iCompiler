package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class WhileStatementASTNode extends LoopStatementASTNode {
	private ExpressionASTNode conditionExprNode;

	public WhileStatementASTNode(ASTNode parentNode, ExpressionASTNode condition, CompoundStatementASTNode body) {
		super(parentNode, ASTNodeType.WHILE_LOOP_NODE, body);

		conditionExprNode = condition;
		addChild(conditionExprNode);
	}
	
	public void setConditionExpression(ExpressionASTNode conditionExpr) {
		this.conditionExprNode = conditionExpr;
	}
	
	public WhileStatementASTNode(ASTNode parentNode, ExpressionASTNode condition) {
		this(parentNode, condition, new CompoundStatementASTNode(null));
	}
	
	public ExpressionASTNode getConditionExpression() {
		return conditionExprNode;
	}
	
	public String toString(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
			
		return String.format(
				"%s[%scondition = %s,%sbody = %s]", 
				getNodeType(),
				sep, conditionExprNode != null ? conditionExprNode.toString(tabs + 1) : "<none>",
				sep, bodyNode.toString(tabs + 1)
			);
	}
	
	@Override
	public void validate(SemanticContext ctx) throws CompilerException {
		if (conditionExprNode != null)
			conditionExprNode.checkType(ctx, VarType.BOOLEAN_PRIMITIVE_TYPE);
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	@Override
    public Token getToken() {
        return conditionExprNode.getToken();
    }

	public WhileBodyStatementASTNode getBody() {
		WhileBodyStatementASTNode node = new WhileBodyStatementASTNode(bodyNode.getParentNode());
		node.addChildren(bodyNode.getChildren());
		return node;
	}
}
