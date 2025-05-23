package ru.itmo.icompiler.semantic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.itmo.icompiler.exception.CompilerException;

public class SemanticContext {
	private List<CompilerException> compilerErrors;
	private Scope scope;
	
	public SemanticContext(List<CompilerException> compilerErrors, Scope scope) {
		this.compilerErrors = compilerErrors;
		this.scope = scope;
	}
	
	public void addCompilerError(CompilerException compilerError) {
		compilerErrors.add(compilerError);
	}
	
	public List<CompilerException> getCompilerErrors() {
		return compilerErrors;
	}
	
	public Scope getScope() {
		return scope;
	}
	
	public static class Scope {
		private Scope parentScope;
		private Map<String, VarType> entities;
		private Map<String, FunctionType> routines;
		private Map<String, VarType> typealiases;
		
		public Scope(Scope parentScope, Map<String, VarType> entities, Map<String, FunctionType> routines, Map<String, VarType> typealiases) {
			this.parentScope = parentScope;
			this.entities = new HashMap<>(entities);
			this.routines = new HashMap<>(routines);
			this.typealiases = new HashMap<>(typealiases);
		}
		
		public Scope() {
			this(null);
		}
		
		public Scope(Scope parentScope) {
			this(parentScope, new HashMap<>(), new HashMap<>(), new HashMap<>());
		}
		
		public Scope(Scope parentScope, Map<String, VarType> entities) {
			this(parentScope, entities, new HashMap<>(), new HashMap<>());
		}
		
		public Scope(Scope parentScope, Map<String, VarType> entities, Map<String, FunctionType> routines) {
			this(parentScope, entities, routines, new HashMap<>());
		}
		
		public Scope(Map<String, VarType> entities, Map<String, VarType> typealiases) {
			this(null, entities, new HashMap<>(), typealiases);
		}
		
		public void addEntity(String name, VarType type) {
			entities.put(name, type);
		}
		
		// public void addRoutine(String name, FunctionType type) {
		// 	routines.put(name, type);
		// }
		
		// public void addTypealias(String name, VarType type) {
		// 	typealiases.put(name, type);
		// }
		
		public VarType lookupEntity(String entity) {
			VarType varType = entities.get(entity);
			
			return varType;
		}
		
		public VarType deepLookupEntity(String entity) {
			VarType type = lookupEntity(entity);
			
			if (type != null)
				return type;
			
			return parentScope != null ? parentScope.deepLookupEntity(entity) : null;
		}
		
		// public FunctionType lookupRoutine(String entity) {
		// 	FunctionType varType = routines.get(entity);
			
		// 	return varType;
		// }
		
		// public FunctionType deepLookupRoutine(String entity) {
		// 	FunctionType type = lookupRoutine(entity);
			
		// 	if (type != null)
		// 		return type;
			
		// 	return parentScope != null ? parentScope.deepLookupRoutine(entity) : null;
		// }
		
		// public VarType lookupTypealias(String typename) {
		// 	VarType varType = typealiases.get(typename);
			
		// 	return varType;
		// }
		
		// public VarType deepLookupTypealias(String typename) {
		// 	VarType type = lookupTypealias(typename);
			
		// 	if (type != null)
		// 		return type;
			
		// 	return parentScope != null ? parentScope.deepLookupTypealias(typename) : null;
		// }
		
		public VarType lookup(String name) {
			VarType t = lookupEntity(name);
			
			// if (t != null)
			// 	return t;
			
			// t = lookupRoutine(name);
			
			// if (t != null)
			// 	return t;
			
			// t = lookupTypealias(name);
			
			return t;
		}
		
		public VarType deepLookup(String name) {
			VarType t = lookup(name);
			
			if (t != null)
				return t;
			
			return parentScope != null ? parentScope.deepLookup(name) : null;
		}
		
		public void clear() {
			entities.clear();
			routines.clear();
			typealiases.clear();
		}
		
		public Scope getParentScope() {
			return parentScope;
		}
	}
}