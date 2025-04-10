package ru.itmo.icompiler.semantic.exception;

public class LoopStatementOutsideLoopSemanticException extends SemanticException {
	public LoopStatementOutsideLoopSemanticException(int errorLine, int errorOffset) {
		super("loop statement outside 'while' or 'for' loop", errorLine, errorOffset);
	}
}
