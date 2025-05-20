package ru.itmo.icompiler.semantic.exception;

import java.util.Map;

import ru.itmo.icompiler.exception.CompilerException;

public class SemanticException extends CompilerException {
	public SemanticException(String message, int[] enclosingLines, int errorLine, int errorOffset, Map<String, int[]> additionalLines) {
		super("Semantic error", message, enclosingLines, errorLine, errorOffset, additionalLines);
	}
	
	public SemanticException(String message, int[] enclosingLines, int errorLine, int errorOffset) {
		this(message, enclosingLines, errorLine, errorOffset, null);
	}
	
	public SemanticException(String message, int errorLine, int errorOffset,  Map<String, int[]> additionalLines) {
		super("Semantic error", message, new int[] { errorLine }, errorLine, errorOffset, additionalLines);
	}
	
	public SemanticException(String message, int errorLine, int errorOffset) {
		this(message, errorLine, errorOffset, null);
	}
}
