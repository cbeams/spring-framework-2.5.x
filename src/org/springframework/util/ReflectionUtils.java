/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple utility class for handling reflection exceptions.
 * Only intended for internal use.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 1.2.2
 */
public abstract class ReflectionUtils {
	
	private static Log log = LogFactory.getLog(ReflectionUtils.class);

	/**
	 * Handle the given reflection exception.
	 * Should only be called if no checked exception is expected to
	 * be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case
	 * of an InvocationTargetException with such a root cause. Throws
	 * an IllegalStateException with an appropriate message else.
	 * @param ex the reflection exception to handle
	 */
	public static void handleReflectionException(Exception ex) {
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		}
		if (ex instanceof InvocationTargetException) {
			handleInvocationTargetException((InvocationTargetException) ex);
		}
		throw new IllegalStateException(
				"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
	}

	/**
	 * Handle the given invocation target exception.
	 * Should only be called if no checked exception is expected to
	 * be thrown by the target method.
	 * <p>Throws the underlying RuntimeException or Error in case
	 * of such a root cause. Throws an IllegalStateException else.
	 * @param ex the invocation target exception to handle
	 */
	public static void handleInvocationTargetException(InvocationTargetException ex) {
		if (ex.getTargetException() instanceof RuntimeException) {
			throw (RuntimeException) ex.getTargetException();
		}
		if (ex.getTargetException() instanceof Error) {
			throw (Error) ex.getTargetException();
		}
		throw new IllegalStateException(
				"Unexpected exception thrown by method - " + ex.getTargetException().getClass().getName() +
				": " + ex.getTargetException().getMessage());
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object with no arguments
	 * The target object can be null when invoking a static {@link Method}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target) {
		 return invokeMethod(method, target, null);
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object with the supplied arguments
	 * The target object can be null when invoking a static {@link Method}.
	 * <p/>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target, Object[] args) {
		try {
			return method.invoke(target, args);
		}
		catch (IllegalAccessException e) {
			handleReflectionException(e);
			throw new IllegalStateException("Unexpected reflection exception - " + e.getMessage());
		}
		catch (InvocationTargetException e) {
			handleReflectionException(e);
			throw new IllegalStateException("Unexpected reflection exception -" + e.getMessage());
		}
	}
	
	
	public static void doWithFields(Class targetClass, FieldCallback fc) throws IllegalArgumentException {		
		doWithFields(targetClass, fc, null);
	}
	
	/**
	 * Invoke the given callback on all private fields in the target class, going
	 * up the class hierarchy to get all declared fields.
	 * @param targetClass
	 * @param fc
	 * @throws IllegalArgumentException
	 */
	public static void doWithFields(Class targetClass, FieldCallback fc, FieldFilter ff) throws IllegalArgumentException {		
		// Keep backing up the inheritance hierarchy
		do {
			// Copy each field declared on this class unless it's static or file
			Field[] fields = targetClass.getDeclaredFields();
			
			log.debug("Found " + fields.length + " fields on " + targetClass);
			for (int i = 0; i < fields.length; i++) {
				//	Skip static and final fields
				 if (ff != null && !ff.matches(fields[i]))
					 continue;
						 
				try {
					fields[i].setAccessible(true);
					fc.doWith(fields[i]);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException("Shouldn't be illegal to access field '" + fields[i].getName() + "': " + ex);
				}
			}
			
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null && targetClass != Object.class);
		
	}
	
	/**
	 * Callback interface invoked on each field in the hierarchy.
	 */
	public interface FieldCallback {
		
		/**
		 * Perform an operation using the given field.
		 * @param f Field, which will have been made accessible before this invocation
		 * @throws IllegalArgumentException
		 * @throws IllegalAccessException
		 */
		void doWith(Field f) throws IllegalArgumentException, IllegalAccessException;
	}
	
	
	/**
	 * Perform the given callback operation on all matching methods of the given class
	 * and superclasses<br>.
	 * The same named method occurring on subclass and superclass will
	 * appear twice, unless excluded by the MethodFilter
	 * @param targetClass class to start looking at
	 * @param mc callback action to take on each method
	 * @param mf filter to use to exclude methods
	 * @throws IllegalArgumentException
	 */
	public static void doWithMethods(Class targetClass, MethodCallback mc, MethodFilter mf) throws IllegalArgumentException {		
		// Keep backing up the inheritance hierarchy
		do {
			Method[] methods = targetClass.getDeclaredMethods();
			log.debug("Found " + methods.length + " methods on " + targetClass);
			for (int i = 0; i < methods.length; i++) {
				 if (mf != null && !mf.matches(methods[i])) {
					 continue;
				 }
						 
				try {
					if (!methods[i].isAccessible()) {
						methods[i].setAccessible(true);					
					}
					mc.doWith(methods[i]);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException("Shouldn't be illegal to access method '" + methods[i].getName() + "': " + ex);
				}
			}
			
			targetClass = targetClass.getSuperclass();
		} while (targetClass != null);
	}
	
	/**
	 * Get all declared methods on the leaf class and all superclasses.
	 * Leaf class methods are included first.
	 * @param leafClass
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Method[] getAllDeclaredMethods(Class leafClass) throws IllegalArgumentException {
		final List l = new LinkedList();
		doWithMethods(leafClass, new MethodCallback() {
			public void doWith(Method m) throws IllegalArgumentException ,IllegalAccessException {
				l.add(m);
			}
		}, DECLARED_METHODS);
		return (Method[]) l.toArray(new Method[l.size()]);
	}
	
	
	/**
	 * Action to take on each method
	 */
	public interface MethodCallback {
		void doWith(Method m) throws IllegalArgumentException, IllegalAccessException;
	}
	
	/**
	 * Callback to exclude methods
	 */
	public interface MethodFilter {
		boolean matches(Method m);
	}
	
	/**
	 * Callback optionally used to filter fields to be
	 * operated on by field callback.
	 */
	public interface FieldFilter {
		boolean matches(Field f);
	}
	
	public static FieldFilter COPYABLE_FIELDS = new FieldFilter() {
		public boolean matches(Field f) {
			return !(Modifier.isStatic(f.getModifiers()) ||
	 				Modifier.isFinal(f.getModifiers())	);
		}
	};
	
	/**
	 * MethodFilter implementation that will return all declared methods
	 * on entire class hierarchy above this object.
	 */
	public static MethodFilter DECLARED_METHODS = new MethodFilter() {
		public boolean matches(Method m) {
			return true;
		}
	};
	
	
	/**
	 * Given the source object and the destination, which must be the same class or a subclass,
	 * copy all fields, including inherited fields. Designed to work on objects with public
	 * no-arg constructors.
	 * @param src
	 * @param dest
	 * @throws java.lang.IllegalArgumentException if arguments are incompatible or either is null
	 */
	public static void shallowCopyFieldState(final Object src, final Object dest) throws IllegalArgumentException {
		if (src == null)
			throw new IllegalArgumentException("Source for field copy cannot be null");
		if (dest == null)
			throw new IllegalArgumentException("Destination for field copy cannot be null");
		if (!src.getClass().isAssignableFrom(dest.getClass()))
			throw new IllegalArgumentException("Destination class '" + dest.getClass().getName() +
						"' must be same or subclass as source class '" + src.getClass().getName() + "'");
		
		log.debug("Copying fields from instance of " + src.getClass() + " to instance of " + dest.getClass());
		ReflectionUtils.doWithFields(src.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field f) throws IllegalArgumentException ,IllegalAccessException {
				Object srcValue = f.get(src);
				f.set(dest, srcValue);
			}
		}, ReflectionUtils.COPYABLE_FIELDS);
	}
}
