package ru.itmo.icompiler.semantic.exception;

public class NoReturnSemanticException extends SemanticException {

    public NoReturnSemanticException(int errorLine, int errorOffset) {
        super("missing returns on some traces", errorLine, errorOffset);
    }

}
