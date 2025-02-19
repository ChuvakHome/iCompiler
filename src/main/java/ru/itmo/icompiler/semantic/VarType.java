package ru.itmo.icompiler.semantic;

public class VarType {
	public static final VarType BOOLEAN_PRIMITIVE_TYPE = new VarType("boolean");
	public static final VarType INTEGER_PRIMITIVE_TYPE = new VarType("integer");
	public static final VarType REAL_PRIMITIVE_TYPE = new VarType("real");
	
	public static final VarType VOID_TYPE = new VarType(null);
	
	private String typename;
	
	public VarType(String typename) {
		this.typename = typename;
	}
	
	public String getTypename() {
		return typename;
	}
	
	public String toString() {
		return typename;
	}
	
	public static class PrimitiveType {
		int size;
	}
}
