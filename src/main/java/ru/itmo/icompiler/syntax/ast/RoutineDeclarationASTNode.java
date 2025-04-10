package ru.itmo.icompiler.syntax.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.semantic.exception.DuplicateArgumentNameSemanticException;
import ru.itmo.icompiler.semantic.visitor.ASTVisitor;

public class RoutineDeclarationASTNode extends ASTNode {
	private VarType resultType;
	private String routineName;
	private List<VariableDeclarationASTNode> argumentsDeclarations;
	
	private Token token;
	
	public RoutineDeclarationASTNode(ASTNode parentNode, VarType resultType, Token token, String routineName, List<VariableDeclarationASTNode> argumentsDeclarations) {
		super(parentNode, ASTNodeType.ROUTINE_DECL_NODE);
		
		this.resultType = resultType;
		this.routineName = routineName;
		this.argumentsDeclarations = new ArrayList<>(argumentsDeclarations);
		
		this.token = token;
	}
	
	public RoutineDeclarationASTNode(ASTNode parentNode, VarType resultType, Token token, String routineName, VariableDeclarationASTNode... argumentsDeclarations) {
		this(parentNode, resultType, token, routineName, Arrays.asList(argumentsDeclarations));
	}
	
	public void setResultType(VarType resultType) {
		this.resultType = resultType;
	}
	
	public VarType getResultType() {
		return resultType;
	}
	
	public Token getToken() {
		return token;
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
	
	public List<VariableDeclarationASTNode> getArgumentsDeclarations() {
		return argumentsDeclarations;
	}
	
	public String toString(int tabs) {
		return String.format("%s[routine = %s, resultType = %s, args = %s]",
					getNodeType(),
					routineName,
					resultType,
					argumentsDeclarations
				);
	}
	
	@Override
	public void validate(SemanticContext ctx) throws CompilerException {
		Set<String> args = new HashSet<>();
		
		for (VariableDeclarationASTNode argDecl: argumentsDeclarations) {
			String argName = argDecl.getVarName();
			
			if (args.contains(argName)) {
				Token tk = argDecl.getToken();
				ctx.addCompilerError(new DuplicateArgumentNameSemanticException(argName, tk.lineNumber, tk.lineOffset));
			}
			else
				args.add(argName);
		}
	}
	
	public<R, A> R accept(ASTVisitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}
}
