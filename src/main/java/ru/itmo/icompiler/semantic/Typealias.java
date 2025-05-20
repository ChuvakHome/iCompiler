package ru.itmo.icompiler.semantic;

public class Typealias extends VarType {
	private VarType realType;
	
	public Typealias(String typename) {
		super(null, typename);
	}
	
	public void setRealType(VarType realType) {
		this.realType = realType;
	}
	
	public VarType getRealType() {
		return realType;
	}
	
	@Override
	public VarType.Tag getTag() {
		return realType != null ? realType.getTag() : null;
	}
	
	@Override
	public boolean isAlias() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		
		return realType != null ? realType.equals(o) : false;
	}

	@Override
	public boolean isConvertibleTo(VarType type) {
		return realType != null ? realType.isConvertibleTo(type) : false;
	}
}
