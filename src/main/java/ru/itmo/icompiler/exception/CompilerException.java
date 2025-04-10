package ru.itmo.icompiler.exception;

import java.util.Map;

public class CompilerException extends Exception {
	private String message; 
	private int[] enclosingLines;
	private int errorLine, errorOffset;
	
	private Map<String, int[]> additionalLines;
	
	public CompilerException(String message, int[] enclosingLines, int errorLine, int errorOffset, Map<String, int[]> additionalLines) {
		this.message = message;
		this.enclosingLines = enclosingLines;
		this.errorLine = errorLine;
		this.errorOffset = errorOffset;
		this.additionalLines = additionalLines;
	}
	
	public CompilerException(String message, int[] enclosingLines, int errorLine, int errorOffset) {
		this(message, enclosingLines, errorLine, errorOffset, null);
	}
	
	public CompilerException(String prefix, String message, int[] enclosingLines, int errorLine, int errorOffset, Map<String, int[]> additionalLines) {
		this(
			String.format("%s: %s", prefix, message),
			enclosingLines,
			errorLine,
			errorOffset,
			additionalLines
		);
	}
	
	public CompilerException(String prefix, String message, int[] enclosingLines, int errorLine, int errorOffset) {
		this(prefix, message, enclosingLines, errorLine, errorOffset, null);
	}
	
	public CompilerException(String prefix, String message, int errorLine, int errorOffset, Map<String, int[]> additionalLines) {
		this(String.format("%s: %s", prefix, message), new int[] { errorLine }, errorLine, errorOffset, additionalLines);
	}
	
	public CompilerException(String prefix, String message, int errorLine, int errorOffset) {
		this(prefix, message, errorLine, errorOffset, null);
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
	
	public Map<String, int[]> getAdditionalLines() {
		return additionalLines;
	}
}
