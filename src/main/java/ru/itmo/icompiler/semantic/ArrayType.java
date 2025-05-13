package ru.itmo.icompiler.semantic;

import ru.itmo.icompiler.semantic.VarType.CompoundType;

public class ArrayType extends CompoundType {
	protected VarType elementType;
	
	public ArrayType(VarType elementType) {
		this(elementType, String.format("array[] %s", elementType));
	}
	
	private ArrayType(VarType elementType, String typename) {
		super(CompoundType.Tag.ARRAY, typename);
		
		this.elementType = elementType;
	}
	
	public VarType getElementType() {
		return elementType;
	}
	
	@Override
	public boolean equalsType(CompoundType type) {
		return ((ArrayType) type).elementType.equals(elementType);
	}

	@Override
	public boolean isConvertibleTo(VarType type) {
		if (type.getTag() != VarType.Tag.ARRAY)
			return false;
		
		return ((ArrayType) type).elementType.isConvertibleTo(elementType);
	}
	
	public static class SizedArrayType extends ArrayType {
		private int size;
		
		public SizedArrayType(VarType elementType, int size) {
			super(elementType, String.format("array[%d] %s", size, elementType));
			
			this.size = size;
		}
		
		public int getSize() {
			return size;
		}
		
		@Override
		public boolean equalsType(CompoundType type) {
			if (type instanceof SizedArrayType arrayType)
				return arrayType.size == size && super.equalsType(arrayType);
				
			return false;
		}

		@Override
		public boolean isConvertibleTo(VarType type) {
			if (type.getTag() != VarType.Tag.ARRAY)
				return false;
			
			if (type instanceof SizedArrayType arrayType)
				return size == arrayType.size && arrayType.elementType.isConvertibleTo(elementType);
			
			return super.isConvertibleTo(type);
		}
	}
}
