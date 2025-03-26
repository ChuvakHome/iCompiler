package ru.itmo.icompiler.syntax.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.itmo.icompiler.semantic.VarType;

public class RoutineDeclarationASTNode extends ASTNode {
	private VarType resultType;
	private String routineName;
	private List<VariableDeclarationASTNode> argumentsDeclarations;
	
	public RoutineDeclarationASTNode(ASTNode parentNode, VarType resultType, String routineName, List<VariableDeclarationASTNode> argumentsDeclarations) {
		super(parentNode, ASTNodeType.ROUTINE_DECL_NODE);
		
		this.resultType = resultType;
		this.routineName = routineName;
		this.argumentsDeclarations = new ArrayList<>(argumentsDeclarations);
	}
	
	public RoutineDeclarationASTNode(ASTNode parentNode, VarType resultType, String routineName, VariableDeclarationASTNode... argumentsDeclarations) {
		this(parentNode, resultType, routineName, Arrays.asList(argumentsDeclarations));
	}
	
	public VarType getResultType() {
		return resultType;
	}
	
	public String getRoutineName() {
		return routineName;
	}
	
	public void addArgumentsDeclarations(VariableDeclarationASTNode... argumentsDeclarations) {
		addArgumentsDeclarations(Arrays.asList(argumentsDeclarations));
	}
	
	public void addArgumentsDeclarations(List<VariableDeclarationASTNode> argumentsDeclarations) {
		this.argumentsDeclarations.addAll(argumentsDeclarations);
	}
	
	public String toString() {
		return String.format("%s[routine = %s, resultType = %s, args = %s]",
					getNodeType(),
					routineName,
					resultType,
					argumentsDeclarations
				);
	}
}
