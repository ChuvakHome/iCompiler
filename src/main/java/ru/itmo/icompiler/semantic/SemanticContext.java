package ru.itmo.icompiler.semantic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		public class VarTypeWithInfo {
			VarType type;
			boolean isTypeAlias;
			boolean isMutable;

			public VarTypeWithInfo(VarType type, boolean isTypeAlias, boolean mutable) {
				this.type = type;
				this.isTypeAlias = isTypeAlias;
				this.isMutable = mutable;
			}
		}

		private Scope parentScope;

		private Map<String, VarTypeWithInfo> entities;
		
		public Scope(Scope parentScope, Map<String, VarType> entities, Map<String, VarType> immutableEntities, Map<String, VarType> typealiases) {
			this.parentScope = parentScope;
			this.entities = new HashMap<>();
			this.entities.putAll(
				entities.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> new VarTypeWithInfo(e.getValue(), false, true)))
			);
			this.entities.putAll(
				immutableEntities.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> new VarTypeWithInfo(e.getValue(), false, false)))
			);
			this.entities.putAll(
				typealiases.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> new VarTypeWithInfo(e.getValue(), true, false)))
			);
		}
		
		public Scope(Scope parentScope, Map<String, VarType> entities, Map<String, VarType> immutableEntities) {
			this(parentScope, entities, immutableEntities, Map.of());
		}
		
		public Scope() {
			this(null);
		}
		
		public Scope(Scope parentScope) {
			this(parentScope, Map.of());
		}
		
		public Scope(Scope parentScope, Map<String, VarType> entities) {
			this(parentScope, entities, Map.of());
		}
		
		public Scope(Map<String, VarType> entities, Map<String, VarType> immutableEntities) {
			this(null, entities, immutableEntities);
		}
		
		public void addEntity(String name, VarType type) {
			entities.put(name, new VarTypeWithInfo(type, false, true));
		}
		
		public void addImmutableEntity(String name, VarType type) {
			entities.put(name, new VarTypeWithInfo(type, false, false));
		}

		public boolean isEntityImmutable(String name) {
			VarTypeWithInfo entityInfo = entities.get(name);
			
			if (entityInfo != null)
				return !entityInfo.isMutable;
	
			return parentScope != null && parentScope.isEntityImmutable(name);
		}
		
		public void addTypealias(String name, VarType type) {
			entities.put(name, new VarTypeWithInfo(type, true, false));
		}
		
		public VarType lookupEntity(String entity) {
			VarTypeWithInfo varType = entities.get(entity);
			
			if (varType != null && !varType.isTypeAlias) {
				return varType.type;
			}

			return null;
		}
		
		public VarType deepLookupEntity(String entity) {
			VarTypeWithInfo varType = entities.get(entity);
			
			if (varType != null && !varType.isTypeAlias) {
				return varType.type;
			}

			if (parentScope != null && varType == null) {
				return parentScope.deepLookupEntity(entity);
			}

			return null;
		}
		
		public VarType lookupTypealias(String typename) {
			VarTypeWithInfo varType = entities.get(typename);
			
			if (varType != null && varType.isTypeAlias) {
				return varType.type;
			}

			return null;
		}
		
		public VarType deepLookupTypealias(String typename) {
			VarTypeWithInfo varType = entities.get(typename);
			
			if (varType != null && varType.isTypeAlias) {
				return varType.type;
			}

			if (parentScope != null && varType == null) {
				return parentScope.deepLookupTypealias(typename);
			}

			return null;
		}
		
		public VarType lookup(String name) {
			VarTypeWithInfo varType = entities.get(name);
			
			if (varType != null) {
				return varType.type;
			}

			return null;
		}
		
		public VarType deepLookup(String name) {
			VarType t = lookup(name);
			
			if (t != null)
				return t;
			
			return parentScope != null ? parentScope.deepLookup(name) : null;
		}
		
		public void clear() {
			entities.clear();
		}
		
		public Scope getParentScope() {
			return parentScope;
		}
	}
}