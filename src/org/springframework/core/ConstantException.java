/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.core;

/**
 * Exception thrown when the Constants class is asked for an invalid
 * constant name.
 * @see org.springframework.core.Constants
 * @version $Id: ConstantException.java,v 1.1 2004-02-02 11:24:51 jhoeller Exp $
 * @author Rod Johnson
 * @since 28-Apr-2003
 */
public class ConstantException extends IllegalArgumentException {
	
	/**
	 * Thrown when an invalid constant name is requested.
	 * @param clazz class containing the constant definitions
	 * @param field invalid constant name
	 * @param message description of the problem
	 */
	public ConstantException(Class clazz, String field, String message) {
		super("Field '" + field + "' " + message + " in " + clazz);
	}

	/**
	 * Thrown when an invalid constant value is looked up.
	 * @param clazz class containing the constant definitions
	 * @param namePrefix prefix of the searched constant names
	 * @param value the looked up constant value
	 */
	public ConstantException(Class clazz, String namePrefix, Object value) {
		super("No '" + namePrefix + "' field with value '" + value + "' found in " + clazz);
	}

}
