package ru.itmo.icompiler.syntax.ast;

import java.util.ArrayList;
import java.util.List;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public abstract class ASTNode {
	private ASTNode parentNode;
	private ASTNodeType nodeType;
	protected List<ASTNode> children;
	
	public ASTNode(ASTNode parentNode, ASTNodeType nodeType) {
		setParentNode(parentNode);
		this.nodeType = nodeType;
		this.children = new ArrayList<>();
	}
	
	public ASTNodeType getNodeType() {
		return nodeType;
	}
	
	protected void setParentNode(ASTNode parentNode) {
		this.parentNode = parentNode;
		
		if (parentNode != null && !parentNode.getChildren().contains(this))
			parentNode.addChild(this);
	}
	
	public void insertAdjacentBefore(ASTNode node) {
		if (node == null)
			return;
		
		if (parentNode != null) {
			int index = parentNode.children.indexOf(this);
						
			parentNode.addChild(index, node);
		}

		node.parentNode = parentNode;
		detach();
		node.addChild(this);
	}
	
	public void detach() {
		if (parentNode != null)
			parentNode.removeChild(this);
	}
	
	public ASTNode getParentNode() {
		return parentNode;
	}
	
	public void addChild(int index, ASTNode node) {
		if (node == null)
			return;
		
		children.add(index, node);
		
		if (node.parentNode != this)
			node.setParentNode(this);
	}
	
	public void addChild(ASTNode node) {
		if (node == null)
			return;
		
		children.add(node);
		
		if (node.parentNode != this)
			node.setParentNode(this);
	}
	
	public void addChildren(List<? extends ASTNode> nodes) {
		for (ASTNode child: nodes)
			addChild(child);
	}
	
	public void removeChild(ASTNode child) {
		if (child == null || this != child.getParentNode())
			return;
		
		children.remove(child);
		child.setParentNode(null);
	}
	
	public List<ASTNode> getChildren() {
		return children;
	}
	
	public ASTNode getChild(int i) {
		return children.get(i);
	}
	
	public void validate(SemanticContext ctx) throws CompilerException {
		
	}

	public abstract Token getToken();

	public int getLineNumber() {
		return getToken().lineNumber;
	}

	public int getLineOffset() {
		return getToken().lineOffset;
	}
	
//	public String toString(int tabs) {
//		return String.format("%s", toString());
//	}
	
	protected List<String> stringifyChildren(int tabs) {
		String sep = "\n" + " ".repeat((tabs + 1) * 4);
		
		return children.stream().map(t -> String.format("%s%s", sep, t.toString(tabs))).toList();
	}
	
	public abstract<R, A> R accept(ASTVisitor<R, A> visitor, A arg);
	
	public String toString(int tabs) {
		return children.isEmpty()
				? toString()
				: String.format(
					"%s[%s]",
					toString(),
					String.join(
						",",
						stringifyChildren(tabs + 1)
					)
				);
	}
	
	public String toString() {
		return nodeType.name();
	}
	
	public static enum ASTNodeType {
		PROGRAM_START_NODE,
		
		EXPRESSION_NODE,
		
		VAR_DECL_NODE,
		VAR_ASSIGN_NODE,
		
		TYPE_ALIAS_DECL_NODE,
		
		ROUTINE_DECL_NODE,
		ROUTINE_DEF_NODE,
		
		EXPR_STMT_NODE,
		
		IF_ELSE_STMT_NODE,
		WHILE_LOOP_NODE,
		
		FOR_EACH_LOOP_NODE,
		FOR_IN_RANGE_LOOP_NODE,
		
		BREAK_STMT_NODE,
		CONTINUE_STMT_NODE,

		RETURN_STMT_NODE,
		
		PRINT_STMT_NODE,
		
		COMPOUND_STMT_NODE,
		WHILE_BODY_STMT_NODE
	}
}
