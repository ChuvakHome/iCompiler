package ru.itmo.icompiler.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

import ru.itmo.icompiler.semantic.VarType.CompoundType;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;

public class RecordType extends CompoundType {
	public static class RecordProperty {
		public final VarType type;
		public final String name;
		public final ExpressionASTNode defaultValue;
		
		public final int line, offset;
		
		public RecordProperty(VarType type, String name, ExpressionASTNode defaultValue, int line, int offset) {
			this.type = type;
			this.name = name;
			this.defaultValue = defaultValue;
			
			this.line = line;
			this.offset = offset;
		}
		
		public RecordProperty(VarType type, String name, int line, int offset) {
			this(type, name, null, line, offset);
		}
		
		public int hashCode() {
			return Objects.hash(name, type);
		}
		
		public boolean equals(Object o) {
			if (o instanceof RecordProperty prop)
				return prop.name.equals(name) && prop.type.equals(type);
			
			return false;
		}
	}
	
	private Map<String, RecordProperty> properties;
	
	public RecordType(List<RecordProperty> properties) {
		super(CompoundType.Tag.RECORD, String.format(
					"record { %s }",
					String.join(
						", ",
						properties.stream().map(p -> {
							String name = p.name;
							VarType type = p.type;
							
							return String.format("%s: %s", name, type);
						}).toList()
					)
				));
		
		this.properties = new LinkedHashMap<>();
		
		for (RecordProperty prop: properties)
			this.properties.put(prop.name, prop);
	}
	
	public Map<String, VarType> getPropertiesTypes() {
		Map<String, VarType> propTypes = new LinkedHashMap<>();
		
		for (Map.Entry<String, RecordProperty> entry: properties.entrySet())
			propTypes.put(entry.getKey(), entry.getValue().type);
		
		return propTypes;
	}
	
	public boolean hasProperty(String prop) {
		return properties.containsKey(prop);
	}
	
	public RecordProperty getProperty(String prop) {
		return properties.get(prop);
	}
	
	public Collection<RecordProperty> getProperties() {
		return properties.values();
	}
	
	public VarType getPropertyType(String prop) {
		return properties.get(prop).type;
	}
	
	@Override
	public int hashCode() {
		List<Object> objects = new ArrayList<>();
		
		objects.add(VarType.Tag.RECORD);
		properties.values().forEach(objects::add);
		
		return Objects.hash(objects.toArray());
	}

	@Override
	public boolean equalsType(CompoundType type) {
		RecordType recordType = (RecordType) type;
		
		TreeSet<String> keys = new TreeSet<>(properties.keySet());
		TreeSet<String> otherKeys = new TreeSet<>(recordType.properties.keySet());
		
		if (!otherKeys.equals(keys))
			return false;
		
		for (String key: keys) {
			VarType propType = properties.get(key).type;
			VarType otherPropType = recordType.getPropertyType(key);
			
			if (!otherPropType.equals(propType))
				return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isConvertibleTo(VarType type) {
		if (type.getTag() != VarType.Tag.RECORD)
			return false;
		
		RecordType recordType = (RecordType) type;
		
//		for (Entry<String, RecordProperty> propEntry: recordType.properties.entrySet()) {
//			String propName = propEntry.getKey();
//			
//			if (hasProperty(propName)) {
//				RecordProperty otherProp = propEntry.getValue();
//				VarType propType = getPropertyType(propName);
//				
//				if (!propType.isConformingType(otherProp.type))
//					return false;
//			}
//			else
//				return false;
//		}
		
		return equalsType(recordType);
	}
}