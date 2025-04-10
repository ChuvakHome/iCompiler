package ru.itmo.icompiler.semantic;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import ru.itmo.icompiler.semantic.VarType.CompoundType;

public class FunctionType extends CompoundType {
	private LinkedHashMap<String, VarType> argsTypes;
	private VarType retType;
	
	public FunctionType(LinkedHashMap<String, VarType> argsTypes, VarType retType) {
		super(
			CompoundType.Tag.FUNCTION,
			String.format(
				"(%s) -> %s",
				String.join(", ", argsTypes.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList()),
				retType
			)
		);
		
		this.argsTypes = new LinkedHashMap<>(argsTypes);
		this.retType = retType;
	}
	
	public FunctionType(Collection<VarType> argsTypes, VarType retType) {
		this(orderArguments(argsTypes), retType);
	}
	
	public VarType getArgumentType(int i) {
		if (i < 0)
			throw new IndexOutOfBoundsException("Index should be non-negative");
		
		Set<Entry<String, VarType>> entrySet = argsTypes.entrySet();
		
		if (i >= entrySet.size())
			throw new IndexOutOfBoundsException(String.format("Index %d should be less than size: %d", i, entrySet.size()));
		
		Iterator<Entry<String, VarType>> iter = entrySet.iterator();
		
		while (i > 0)
			iter.next();
		
		return iter.next().getValue();
	}
	
	public VarType getArgumentType(String argument) {
		return argsTypes.get(argument);
	}
	
	public LinkedHashMap<String, VarType> getArgumentsTypes() {
		return argsTypes;
	}
	
	public VarType getReturnType() {
		return retType;
	}
	
	private static LinkedHashMap<String, VarType> orderArguments(Collection<VarType> types) {
		LinkedHashMap<String, VarType> args = new LinkedHashMap<>();
		
		Iterator<VarType> iter = types.iterator();
		
		int arg = 0;
		
		while (iter.hasNext()) {
			VarType type = iter.next();
			
			args.put("$" + arg++, type);
		}
		
		return args;
	}

	@Override
	public boolean equalsType(CompoundType type) {
		FunctionType funcType = (FunctionType) type;
		
		Collection<VarType> argsTypes = this.argsTypes.values();
		Collection<VarType> otherArgsTypes = funcType.argsTypes.values();
		
		if (argsTypes.size() != otherArgsTypes.size())
			return false;
		
		Iterator<VarType> it1 = argsTypes.iterator();
		Iterator<VarType> it2 = argsTypes.iterator();
		
		while (it1.hasNext() && it2.hasNext()) {
			VarType argType = it1.next();
			VarType otherArgType = it2.next();
			
			if (!otherArgType.equals(argType))
				return false;
		}
		
		return funcType.retType.equals(retType);
	}
	
	@Override
	public boolean isConformingType(VarType type) {
		return false;
	}
}
