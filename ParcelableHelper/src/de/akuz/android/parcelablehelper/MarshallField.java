package de.akuz.android.parcelablehelper;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class MarshallField {

	public String fieldName;
	public String getterName;
	public String typeName;
	public String setterName;
	boolean accessDirectly;

	public MarshallField(Element e) {
		accessDirectly = hasDirectAccess(e);
		fieldName = e.getSimpleName().toString();
		typeName = getTypeName(e);
		getterName = createGetterNameFromFieldName(fieldName);
		setterName = createSetterNameFromFieldName(fieldName);
	}

	private String createGetterNameFromFieldName(String fieldName) {
		String getter = "get" + fieldNameUpperCase(fieldName) + "()";
		return getter;
	}

	public String fieldNameUpperCase(String fieldName) {
		char first = fieldName.charAt(0);
		char upperCase = Character.toUpperCase(first);
		return upperCase + fieldName.substring(1);
	}

	private String createSetterNameFromFieldName(String fieldName) {
		String getter = "set" + fieldNameUpperCase(fieldName);
		return getter;
	}

	private boolean hasDirectAccess(Element e) {
		Set<Modifier> modifiers = e.getModifiers();
		if (modifiers.contains(Modifier.PRIVATE)) {
			return false;
		}
		if (modifiers.contains(Modifier.PROTECTED)) {
			return false;
		}
		return true;
	}

	private String getTypeName(Element e) {
		TypeMirror type = e.asType();
		return type.toString();
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getGetterName() {
		return getterName;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getSetterName() {
		return setterName;
	}

	public boolean isAccessDirectly() {
		return accessDirectly;
	}

}
