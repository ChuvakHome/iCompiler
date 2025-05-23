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
		
		return ctx.getScope().deepLookup(typealias.getTypename());
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
		VarType type;
		
		if (deepSearch)
			type = ctx.getScope().deepLookup(entity);
		else
			type = ctx.getScope().lookup(entity);
		
		// TODO
		FunctionType routineType = (FunctionType)type;

		return Optional.ofNullable(routineType).orElseThrow(() -> e);
	}
	
	public static VarType checkTypealias(String typename, SemanticContext ctx, boolean deepSearch, SemanticException e) throws SemanticException {
		VarType type;
		
		if (deepSearch)
			type = ctx.getScope().deepLookup(typename);
		else
			type = ctx.getScope().lookup(typename);
		
		// TODO
		VarType realType = type;

		return Optional.ofNullable(realType).orElseThrow(() -> e);
	}
}
