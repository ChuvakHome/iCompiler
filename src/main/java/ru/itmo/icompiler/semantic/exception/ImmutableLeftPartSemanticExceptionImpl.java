package ru.itmo.icompiler.semantic.exception;

public class ImmutableLeftPartSemanticExceptionImpl extends SemanticException {
    public ImmutableLeftPartSemanticExceptionImpl(int errorLine, int errorOffset) {
        super("left hand-side of the assignment should be mutable", errorLine, errorOffset);
    }
}
