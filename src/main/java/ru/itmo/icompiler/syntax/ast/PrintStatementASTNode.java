package ru.itmo.icompiler.syntax.ast;

import java.util.Arrays;
import java.util.List;

import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class PrintStatementASTNode extends ASTNode {
	public PrintStatementASTNode(ASTNode parentNode, List<ExpressionASTNode> expressions) {
		super(parentNode, ASTNodeType.PRINT_STMT_NODE);
		
		addChildren(expressions);
	}
	
	public PrintStatementASTNode(ASTNode parentNode, ExpressionASTNode... expressions) {
		this(parentNode, Arrays.asList(expressions));
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

	@Override
    public Token getToken() {
        return getChild(0).getToken();
    }
}
