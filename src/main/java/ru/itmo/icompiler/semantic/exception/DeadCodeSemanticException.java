package ru.itmo.icompiler.semantic.exception;

public class DeadCodeSemanticException extends SemanticException {

    public DeadCodeSemanticException(int errorLine, int errorOffset) {
        super("dead code", errorLine, errorOffset);
    }

}
