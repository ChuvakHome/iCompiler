package ru.itmo.icompiler.semantic;

public class VarType {
	public static final VarType BOOLEAN_PRIMITIVE_TYPE = new VarType("boolean");
	public static final VarType INTEGER_PRIMITIVE_TYPE = new VarType("integer");
	public static final VarType REAL_PRIMITIVE_TYPE = new VarType("real");
	
	public static final VarType VOID_TYPE = new VarType("void");
	
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
	
	public static class ArrayType extends VarType {
		private VarType elementType;
		private int size;
		
		public ArrayType(VarType elementType, int size) {
			super(String.format("array[%d] %s", size, elementType));
			
			this.elementType = elementType;
			this.size = size;
		}
		
		public VarType getElementType() {
			return elementType;
		}
		
		public int getSize() {
			return size;
		}
	}
}
