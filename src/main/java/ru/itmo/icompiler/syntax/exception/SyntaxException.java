package ru.itmo.icompiler.syntax.exception;

public class SyntaxException extends Exception {
	private String message; 
	private int[] enclosingLines;
	private int errorLine, errorOffset;
	
	public SyntaxException(String message, int[] enclosingLines, int errorLine, int errorOffset) {
		this.message = String.format("Syntax error: %s", message);
		this.enclosingLines = enclosingLines;
		this.errorLine = errorLine;
		this.errorOffset = errorOffset;
	}
	
	public SyntaxException(String message, int errorLine, int errorOffset) {
		this(message, new int[] { errorLine }, errorLine, errorOffset);
	}
	
	public String getMessage() {
		return message;
	}
	
	public int[] getEnclosingLines() {
		return enclosingLines;
	}
	
	public int getErrorLine() {
		return errorLine;
	}
	
	public int getErrorOffset() {
		return errorOffset;
	}
}
