package org.springframework.beans.factory.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holder for constructor argument values for a bean.
 * Supports values for a specific index in the constructor argument list
 * and generic matches by type.
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class ConstructorArgumentValues {

	private Map indexedArgumentValues = new HashMap();

	private Set genericArgumentValues = new HashSet();

	/**
	 * Add argument value for the given index in the constructor argument list.
	 * @param index the index in the constructor argument list
	 * @param value the argument value
	 */
	public void addIndexedArgumentValue(int index, Object value) {
		this.indexedArgumentValues.put(new Integer(index), value);
	}

	/**
	 * Get argument value for the given index in the constructor argument list.
	 * @param index the index in the constructor argument list
	 * @return the argument value, or null if none set
	 */
	public Object getIndexedArgumentValue(int index) {
		return this.indexedArgumentValues.get(new Integer(index));
	}

	/**
	 * Return the map of indexed argument values.
	 * @return Map with Integer indizes as keys and argument values as values
	 */
	public Map getIndexedArgumentValues() {
		return indexedArgumentValues;
	}

	/**
	 * Add generic argument value to be matched by type.
	 * @param value the argument value
	 */
	public void addGenericArgumentValue(Object value) {
		this.genericArgumentValues.add(value);
	}

	/**
	 * Look for a generic argument value that matches the given type.
	 * @param requiredType the type to match
	 */
	public Object getGenericArgumentValue(Class requiredType) {
		for (Iterator it = this.genericArgumentValues.iterator(); it.hasNext();) {
			Object value = it.next();
			if (requiredType.isInstance(value) || (requiredType.isArray() && value instanceof List)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Return the set of generic argument values.
	 * @return Set with argument values
	 */
	public Set getGenericArgumentValues() {
		return this.genericArgumentValues;
	}

	/**
	 * Look for an argument value that either corresponds to the given index
	 * in the constructor argument list or generically matches by type.
	 * @param index the index in the constructor argument list
	 * @param requiredType the type to match
	 * @return the argument value, or null if none found
	 */
	public Object getArgumentValue(int index, Class requiredType) {
		Object value = getIndexedArgumentValue(index);
		if (value == null) {
			value = getGenericArgumentValue(requiredType);
		}
		return value;
	}

	/**
	 * Return the number of arguments held in this instance.
	 */
	public int getNrOfArguments() {
		return this.indexedArgumentValues.size() + this.genericArgumentValues.size();
	}

	/**
	 * Return if this holder does not contain any argument values,
	 * neither indexed ones nor generic ones.
	 */
	public boolean isEmpty() {
		return this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty();
	}

}
