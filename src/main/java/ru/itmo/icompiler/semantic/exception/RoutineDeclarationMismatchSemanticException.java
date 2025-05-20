package ru.itmo.icompiler.semantic.exception;

import java.util.Map;

public class RoutineDeclarationMismatchSemanticException extends SemanticException {
	public RoutineDeclarationMismatchSemanticException(String routineName, int errorLine, int errorOffset, int[] declarationLines) {
		super(
			String.format("declaration of the routine \"%s\" differ from the previous one", routineName), 
			errorLine, 
			errorOffset,
			Map.of("previously declared as", declarationLines)
		);
	}
}
