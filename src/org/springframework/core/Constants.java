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

package org.springframework.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class can be used to parse other classes containing constant definitions
 * in public static final members. The asXXXX() methods of this class allow these
 * constant values to be accessed via their string names.
 *
 * <p>Consider class Foo containing public final static int CONSTANT1 = 66;
 * An instance of this class wrapping Foo.class will return the constant value
 * of 66 from its asInt() method given the argument "CONSTANT1".
 *
 * <p>This class is ideal for use in PropertyEditors, enabling them to
 * recognize the same names as the constants themselves, and freeing them
 * from maintaining their own mapping.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16-Mar-2003
 */
public class Constants {

	private final String className;

	/** Map from String field name to object value */
	private final Map fieldCache = new HashMap();

	/**
	 * Create a new Constants converter class wrapping the given class.
	 * All public static final variables will be exposed, whatever their type.
	 * @param clazz class to analyze
	 */
	public Constants(Class clazz) {
		this.className = clazz.getName();
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())	&&
			    Modifier.isPublic(field.getModifiers())) {
				String name = field.getName();
				try {
					Object value = field.get(null);
					this.fieldCache.put(name, value);
				}
				catch (IllegalAccessException ex) {
					// just leave this field and continue
				}
			}
		}
	}

	/**
	 * Return the number of constants exposed.
	 */
	public int getSize() {
		return this.fieldCache.size();
	}

	/**
	 * Return a constant value cast to a Number.
	 * @param code name of the field
	 * @return long value if successful
	 * @see #asObject
	 * @throws ConstantException if the field name wasn't found
	 * or if the type wasn't compatible with Number
	 */
	public Number asNumber(String code) throws ConstantException {
		Object o = asObject(code);
		if (!(o instanceof Number)) {
			throw new ConstantException(this.className, code, "not a Number");
		}
		return (Number) o;
	}

	/**
	 * Return a constant value as a String.
	 * @param code name of the field
	 * @return String string value if successful.
	 * Works even if it's not a string (invokes toString()).
	 * @see #asObject
	 * @throws ConstantException if the field name wasn't found
	 */
	public String asString(String code) throws ConstantException {
		return asObject(code).toString();
	}

	/**
	 * Parse the given string (upper or lower case accepted) and return 
	 * the appropriate value if it's the name of a constant field in the
	 * class we're analysing.
	 * @throws ConstantException if there's no such field
	 */
	public Object asObject(String code) throws ConstantException {
		code = code.toUpperCase();
		Object val = this.fieldCache.get(code);
		if (val == null) {
			throw new ConstantException(this.className, code, "not found");
		}
		return val;
	}

	/**
	 * Return all values of the given group of constants.
	 * @param namePrefix prefix of the constant names to search
	 * @return the set of values
	 */
	public Set getValues(String namePrefix) {
		namePrefix = namePrefix.toUpperCase();
		Set values = new HashSet();
		for (Iterator it = this.fieldCache.keySet().iterator(); it.hasNext();) {
			String code = (String) it.next();
			if (code.startsWith(namePrefix)) {
				values.add(this.fieldCache.get(code));
			}
		}
		return values;
	}

	/**
	 * Return all values of the group of constants for the
	 * given bean property name.
	 * @param propertyName the name of the bean property
	 * @return the set of values
	 * @see #propertyToConstantNamePrefix
	 */
	public Set getValuesForProperty(String propertyName) {
		return getValues(propertyToConstantNamePrefix(propertyName));
	}

	/**
	 * Look up the given value within the given group of constants.
	 * Will return the first match.
	 * @param value constant value to look up
	 * @param namePrefix prefix of the constant names to search
	 * @return the name of the constant field
	 * @throws ConstantException if the value wasn't found
	 */
	public String toCode(Object value, String namePrefix) throws ConstantException {
		namePrefix = namePrefix.toUpperCase();
		for (Iterator it = this.fieldCache.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			if (key.startsWith(namePrefix) && entry.getValue().equals(value)) {
				return key;
			}
		}
		throw new ConstantException(this.className, namePrefix, value);
	}

	/**
	 * Look up the given value within the group of constants for
	 * the given bean property name. Will return the first match.
	 * @param value constant value to look up
	 * @param propertyName the name of the bean property
	 * @return the name of the constant field
	 * @throws ConstantException if the value wasn't found
	 * @see #propertyToConstantNamePrefix
	 */
	public String toCodeForProperty(Object value, String propertyName) throws ConstantException {
		return toCode(value, propertyToConstantNamePrefix(propertyName));
	}

	/**
	 * Convert the given bean property name to a constant name prefix.
	 * Uses a common naming idiom: turning all lower case characters to
	 * upper case, and prepending upper case characters with an underscore.
	 * <p>Example: "imageSize" -> "IMAGE_SIZE".
	 * @param propertyName the name of the bean property
	 * @return the corresponding constant name prefix
	 * @see #getValuesForProperty
	 * @see #toCodeForProperty
	 */
	public String propertyToConstantNamePrefix(String propertyName) {
	  StringBuffer parsedPrefix = new StringBuffer();
	  for(int i = 0; i < propertyName.length(); i++) {
	    char c = propertyName.charAt(i);
	    if (Character.isUpperCase(c)) {
	      parsedPrefix.append("_");
	      parsedPrefix.append(c);
	    }
	    else {
	      parsedPrefix.append(Character.toUpperCase(c));
	    }
	  }
		return parsedPrefix.toString();
	}

}
