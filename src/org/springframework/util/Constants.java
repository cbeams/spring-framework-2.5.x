/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * This class can be used to parse other classes containing constant definitions
 * in public static final members. The asXXXX() methods of this class allow these
 * constant values to be accessed via their string names.
 *
 * <p>Consider class Foo containing public final static int CONSTANT1 = 66;
 * An instance of this class wrapping Foo.class will return the 
 * constant value of 66 from its asInt() method given the argument "CONSTANT1". 
 *
 * <p>This class is ideal for use in PropertyEditors, enabling them to recognize
 * the same names as the constants themselves, and freeing them from
 * maintaining their own mapping.
 *
 * @version $Id: Constants.java,v 1.2 2003-08-18 15:42:59 jhoeller Exp $
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16-Mar-2003
 */
public class Constants {

	/** Map from String field name to object value */
	private Map map = new HashMap();

	/** Class analyzed */
	private final Class clazz;

	/**
	 * Create a new Constants converter class wrapping the given class.
	 * All public static final variables will be exposed, whatever their type.
	 * @param clazz class to analyze.
	 */
	public Constants(Class clazz) {
		this.clazz = clazz;
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			if (Modifier.isFinal(f.getModifiers())
				&& Modifier.isStatic(f.getModifiers())
				&& Modifier.isPublic(f.getModifiers())) {
				String name = f.getName();
				try {
					Object value = f.get(null);
					this.map.put(name, value);
				}
				catch (IllegalAccessException ex) {
					// Just leave this field and continue
				}
			}
		}
	}

	/**
	 * Return the number of constants exposed.
	 * @return int the number of constants exposed
	 */
	public int getSize() {
		return this.map.size();
	}

	/**
	 * Return a constant value cast to a Number.
	 * @param code name of the field
	 * @return long value if successful
	 * @see #asObject
	 * @throws ConstantException if the field name wasn't found or
	 * if the type wasn't compatible with Number
	 */
	public Number asNumber(String code) throws ConstantException {
		Object o = asObject(code);
		if (!(o instanceof Number))
			throw new ConstantException(this.clazz, code, "not a Number");
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
		Object val = this.map.get(code);
		if (val == null)
			throw new ConstantException(this.clazz, code, "not found");
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
		for (Iterator it = this.map.keySet().iterator(); it.hasNext();) {
			String code = (String) it.next();
			if (code.startsWith(namePrefix)) {
				values.add(this.map.get(code));
			}
		}
		return values;
	}

	/**
	 * Look up the given value within the given group of constants.
	 * Will return the first match.
	 * @param value constant value to look up
	 * @param namePrefix prefix of the constant names to search
	 * @return the name of the field
	 * @throws ConstantException if the value wasn't found
	 */
	public String toCode(Object value, String namePrefix) throws ConstantException {
		namePrefix = namePrefix.toUpperCase();
		for (Iterator it = this.map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			if (key.startsWith(namePrefix) && entry.getValue().equals(value))
				return key;
		}
		throw new ConstantException(this.clazz, namePrefix, value);
	}

}
