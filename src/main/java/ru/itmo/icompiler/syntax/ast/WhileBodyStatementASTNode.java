package ru.itmo.icompiler.syntax.ast;

import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class WhileBodyStatementASTNode extends CompoundStatementASTNode {
    public WhileBodyStatementASTNode(ASTNode parentNode) {
        super(parentNode, ASTNodeType.WHILE_BODY_STMT_NODE);
    }

    @Override
    public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }
}
