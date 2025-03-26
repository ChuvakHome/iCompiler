package ru.itmo.icompiler.syntax.ast;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
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
	
	public ASTNode getParentNode() {
		return parentNode;
	}
	
	public void addChild(ASTNode node) {
		children.add(node);
		
		if (node != null && node.parentNode != this)
			node.setParentNode(this);
	}
	
	public List<ASTNode> getChildren() {
		return children;
	}
	
	public String toString(int tabs) {
		return String.format("%s", toString());
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
		
		IF_STMT_NODE,
		IF_ELSE_STMT_NODE,
		WHILE_LOOP_NODE,
		FOR_LOOP_NODE,
		
		PRINT_STMT_NODE,
	}
}
