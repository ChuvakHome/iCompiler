package ru.itmo.icompiler.semantic.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.itmo.icompiler.semantic.VarType;

public class UnexpectedTypeSemanticException extends SemanticException {
	private List<VarType> expectedTypes;
	private VarType actualType;
	
	public UnexpectedTypeSemanticException(Collection<VarType> expectedTypes, VarType actualType, int errorLine, int errorOffset) {
		super(
			String.format(
					"unexpected type: expected %s, got %s", 
					String.join(", ", expectedTypes.stream().map(VarType::toString).toList()), 
					actualType), 
			errorLine, 
			errorOffset
		);
		
		this.expectedTypes = new ArrayList<>(expectedTypes);
		this.actualType = actualType;
	}
	
	public List<VarType> getExpectedTypes() {
		return expectedTypes;
	}
	
	public VarType getActualType() {
		return actualType;
	}
}
