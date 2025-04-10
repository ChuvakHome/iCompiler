package ru.itmo.icompiler.semantic.exception;

import java.util.Map;

public class EntityRedefinitionSemanticException extends SemanticException {
	public EntityRedefinitionSemanticException(String typename, int errorLine, int errorOffset, int[] declarationLines) {
		super(
			String.format("new entity \"%s\" redefines existing one", typename), 
			errorLine, 
			errorOffset,
			Map.of("previously declared here", declarationLines)
		);
	}
}
