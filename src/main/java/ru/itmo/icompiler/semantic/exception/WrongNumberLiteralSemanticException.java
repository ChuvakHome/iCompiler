package ru.itmo.icompiler.semantic.exception;

public class WrongNumberLiteralSemanticException extends SemanticException {
	public WrongNumberLiteralSemanticException(int errorLine, int errorOffset) {
		super("Invalid number literal", errorLine, errorOffset);
	}
}
