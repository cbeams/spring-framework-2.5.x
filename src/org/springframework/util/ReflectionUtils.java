/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ReflectionUtils.java,v 1.1 2003-11-28 21:33:23 johnsonr Exp $
 */
public abstract class ReflectionUtils {
	
	private static Log log = LogFactory.getLog(ReflectionUtils.class);
	
	/**
	 * Given the source object and the destination, which must be the same class or a subclass,
	 * copy all fields, including inherited fields. Designed to work on objects with public
	 * no-arg constructors.
	 * @param src
	 * @param dest
	 * @throws IllegalArgumentException if arguments are incompatible or either is null
	 */
	public static void shallowCopyFieldState(Object src, Object dest) throws IllegalArgumentException {
		if (src == null)
			throw new IllegalArgumentException("Source for field copy cannot be null");
		if (dest == null)
			throw new IllegalArgumentException("Destination for field copy cannot be null");
		if (!src.getClass().isAssignableFrom(dest.getClass()))
			throw new IllegalArgumentException("Destination class '" + dest.getClass().getName() + 
						"' must be same or subclass as source class '" + src.getClass().getName() + "'");
		
		log.debug("Copying fields from instance of " + src.getClass() + " to instance of " + dest.getClass());
		
		// Keep backing up the inheritance hierarchy
		Class targetClass = src.getClass();
		do {
			// Copy each field declared on this class unless it's static or file
			Field[] fields = targetClass.getDeclaredFields();
			
			log.debug("Found " + fields.length + " fields on " + targetClass);
			for (int i = 0; i < fields.length; i++) {
				//	Skip static and final fields
				 if (Modifier.isStatic(fields[i].getModifiers()) ||
				 		Modifier.isFinal(fields[i].getModifiers())	)
					 continue;
						 
				try {
					fields[i].setAccessible(true);
					Object srcValue = fields[i].get(src);
					//System.out.println("Copying field " + fields[i].getName());
					fields[i].set(dest, srcValue);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException("Shouldn't be illegal to access field '" + fields[i].getName() + "': " + ex);
				}
			}
			
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);
		
	}

}
