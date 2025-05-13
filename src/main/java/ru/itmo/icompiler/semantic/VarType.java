package ru.itmo.icompiler.semantic;

public abstract class VarType {
	public static final VarType BOOLEAN_PRIMITIVE_TYPE = new PrimitiveType("boolean") {
		@Override
		public boolean isConvertibleTo(VarType type) {
			return type.getTag() == VarType.Tag.PRIMITIVE && type != VarType.VOID_TYPE;
		}	
	};
	
	public static final VarType INTEGER_PRIMITIVE_TYPE = new PrimitiveType("integer") {
		@Override
		public boolean isConvertibleTo(VarType type) {
			return type.getTag() == VarType.Tag.PRIMITIVE && type != VarType.VOID_TYPE;
		}
	};
	
	public static final VarType REAL_PRIMITIVE_TYPE = new PrimitiveType("real") {
		@Override
		public boolean isConvertibleTo(VarType type) {
			return type.getTag() == VarType.Tag.PRIMITIVE && type != VarType.VOID_TYPE && type != VarType.BOOLEAN_PRIMITIVE_TYPE;
		}
	};
	
	public static final VarType VOID_TYPE = new PrimitiveType("void") {
		@Override
		public boolean isConvertibleTo(VarType type) {
			return type == this;
		}
	};
	
	public static final VarType AUTO_TYPE = new VarType(null, "auto") {
		@Override
		public boolean equals(Object o) {
			return false;
		}

		@Override
		public boolean isConvertibleTo(VarType type) {
			return false;
		}
	};
	
	protected Tag tag;
	private String typename;
	
	public static enum Tag {
		PRIMITIVE,
		ARRAY,
		RECORD,
		FUNCTION,
	}
	
	public VarType(Tag tag, String typename) {
		this.tag = tag;
		this.typename = typename;
	}
	
	public Tag getTag() {
		return tag;
	}
	
	public String getTypename() {
		return typename;
	}
	
	public abstract boolean equals(Object o);
	
	public abstract boolean isConvertibleTo(VarType type);
	
	public boolean isAlias() {
		return false;
	}
	
	public String toString() {
		return typename;
	}
	
	private static abstract class PrimitiveType extends VarType {
		private PrimitiveType(String typename) {
			super(Tag.PRIMITIVE, typename);
		}
		
		public boolean equals(Object o) {
			return o == this;
		}
	}
	
	public abstract static class CompoundType extends VarType {		
		public CompoundType(Tag tag, String typename) {
			super(tag, typename);
		}
		
		public abstract boolean equalsType(CompoundType type);
		
		public boolean equals(Object o) {
			if (o instanceof CompoundType type && tag == type.tag)
				return equalsType(type);
			
			return false;
		}
	}
}
