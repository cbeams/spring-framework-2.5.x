/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.beans.factory.config;

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

	private final Map indexedArgumentValues = new HashMap();

	private final Set genericArgumentValues = new HashSet();


	/**
	 * Create new ConstructorArgumentValues.
	 */
	public ConstructorArgumentValues() {
	}

	/**
	 * Deep copy constructor.
	 */
	public ConstructorArgumentValues(ConstructorArgumentValues other) {
		addArgumentValues(other);
	}

	/**
	 * Copy all given argument values into this object.
	 */
	public void addArgumentValues(ConstructorArgumentValues other) {
		if (other != null) {
			this.genericArgumentValues.addAll(other.genericArgumentValues);
			this.indexedArgumentValues.putAll(other.indexedArgumentValues);
		}
	}

	/**
	 * Add argument value for the given index in the constructor argument list.
	 * @param index the index in the constructor argument list
	 * @param value the argument value
	 */
	public void addIndexedArgumentValue(int index, Object value) {
		this.indexedArgumentValues.put(new Integer(index), new ValueHolder(value));
	}

	/**
	 * Add argument value for the given index in the constructor argument list.
	 * @param index the index in the constructor argument list
	 * @param value the argument value
	 */
	public void addIndexedArgumentValue(int index, Object value, String type) {
		this.indexedArgumentValues.put(new Integer(index), new ValueHolder(value, type));
	}

	/**
	 * Get argument value for the given index in the constructor argument list.
	 * @param index the index in the constructor argument list
	 * @return the ValueHolder for the argument, or null if none set
	 */
	public ValueHolder getIndexedArgumentValue(int index, Class requiredType) {
		ValueHolder valueHolder = (ValueHolder) this.indexedArgumentValues.get(new Integer(index));
		if (valueHolder != null) {
			if (valueHolder.getType() == null || requiredType.getName().equals(valueHolder.getType())) {
				return valueHolder;
			}
		}
		return null;
	}

	/**
	 * Return the map of indexed argument values.
	 * @return Map with Integer indizes as keys and ValueHolders as values
	 * @see ValueHolder
	 */
	public Map getIndexedArgumentValues() {
		return indexedArgumentValues;
	}

	/**
	 * Add generic argument value to be matched by type.
	 * <p>Note: A single generic argument value will just be used once,
	 * rather than matched multiple times (as of Spring 1.1).
	 * @param value the argument value
	 */
	public void addGenericArgumentValue(Object value) {
		this.genericArgumentValues.add(new ValueHolder(value));
	}

	/**
	 * Add generic argument value to be matched by type.
	 * <p>Note: A single generic argument value will just be used once,
	 * rather than matched multiple times (as of Spring 1.1).
	 * @param value the argument value
	 */
	public void addGenericArgumentValue(Object value, String type) {
		this.genericArgumentValues.add(new ValueHolder(value, type));
	}

	/**
	 * Look for a generic argument value that matches the given type.
	 * @param requiredType the type to match
	 * @return the ValueHolder for the argument, or null if none set
	 */
	public ValueHolder getGenericArgumentValue(Class requiredType) {
		for (Iterator it = this.genericArgumentValues.iterator(); it.hasNext();) {
			ValueHolder valueHolder = (ValueHolder) it.next();
			Object value = valueHolder.getValue();
			if (valueHolder.getType() != null) {
				if (valueHolder.getType().equals(requiredType.getName())) {
					return valueHolder;
				}
			}
			else if (requiredType.isInstance(value) || (requiredType.isArray() && List.class.isInstance(value))) {
				return valueHolder;
			}
		}
		return null;
	}

	/**
	 * Return the set of generic argument values.
	 * @return Set of ValueHolders
	 * @see ValueHolder
	 */
	public Set getGenericArgumentValues() {
		return this.genericArgumentValues;
	}

	/**
	 * Look for an argument value that either corresponds to the given index
	 * in the constructor argument list or generically matches by type.
	 * @param index the index in the constructor argument list
	 * @param requiredType the type to match
	 * @return the ValueHolder for the argument, or null if none set
	 */
	public ValueHolder getArgumentValue(int index, Class requiredType) {
		ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType);
		if (valueHolder == null) {
			valueHolder = getGenericArgumentValue(requiredType);
		}
		return valueHolder;
	}

	/**
	 * Return the number of arguments held in this instance.
	 */
	public int getArgumentCount() {
		return getNrOfArguments();
	}

	/**
	 * Return the number of arguments held in this instance.
	 * @deprecated in favor of getArgumentCount
	 * @see #getArgumentCount
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


	/**
	 * Holder for a constructor argument value, with an optional type
	 * attribute indicating the target type of the actual constructor argument.
	 */
	public static class ValueHolder {

		private Object value;

		private String type;

		private ValueHolder(Object value) {
			this.value = value;
		}

		private ValueHolder(Object value, String type) {
			this.value = value;
			this.type = type;
		}

		/**
		 * Set the value for the constructor argument.
		 * Only necessary for manipulating a registered value,
		 * for example in BeanFactoryPostProcessors.
		 * @see PropertyPlaceholderConfigurer
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Return the value for the constructor argument.
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * Set the type of the constructor argument.
		 * Only necessary for manipulating a registered value,
		 * for example in BeanFactoryPostProcessors.
		 * @see PropertyPlaceholderConfigurer
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Return the type of the constructor argument.
		 */
		public String getType() {
			return type;
		}
	}

}
