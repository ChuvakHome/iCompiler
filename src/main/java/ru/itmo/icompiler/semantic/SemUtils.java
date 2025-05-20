package ru.itmo.icompiler.semantic;

import java.util.Optional;

import ru.itmo.icompiler.semantic.exception.SemanticException;

public class SemUtils {
	public static VarType getRealType(VarType providedType, SemanticContext ctx, SemanticException e) throws SemanticException {
		VarType realType = getRealType(providedType, ctx);
		
		return Optional.ofNullable(realType).orElseThrow(() -> e);
	}
	
	public static VarType getRealType(VarType providedType, SemanticContext ctx) {
		if (providedType == null || !providedType.isAlias())
			return providedType;
		
		Typealias typealias = (Typealias) providedType;
		
		return ctx.getScope().deepLookupTypealias(typealias.getTypename());
	}
	
	public static VarType checkEntity(String entity, SemanticContext ctx, boolean deepSearch, SemanticException e) throws SemanticException {
		VarType entityType = null;
		
		if (deepSearch)
			entityType = ctx.getScope().deepLookupEntity(entity);
		else
			entityType = ctx.getScope().lookupEntity(entity);
		
		return Optional.ofNullable(entityType).orElseThrow(() -> e);
	}
	
	public static FunctionType checkRoutine(String entity, SemanticContext ctx, boolean deepSearch, SemanticException e) throws SemanticException {
		FunctionType routineType = null;
		
		if (deepSearch)
			routineType = ctx.getScope().deepLookupRoutine(entity);
		else
			routineType = ctx.getScope().lookupRoutine(entity);
		
		return Optional.ofNullable(routineType).orElseThrow(() -> e);
	}
	
	public static VarType checkTypealias(String typename, SemanticContext ctx, boolean deepSearch, SemanticException e) throws SemanticException {
		VarType realType = null;
		
		if (deepSearch)
			realType = ctx.getScope().deepLookupTypealias(typename);
		else
			realType = ctx.getScope().lookupTypealias(typename);
		
		return Optional.ofNullable(realType).orElseThrow(() -> e);
	}
}
