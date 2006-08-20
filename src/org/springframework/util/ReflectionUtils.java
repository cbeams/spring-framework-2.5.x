/*
 * Copyright 2002-2006 the original author or authors.
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

/**
 * Simple utility class for handling reflection exceptions.
 * Only intended for internal use.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Costin Leau
 * @since 1.2.2
 */
public abstract class ReflectionUtils {

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
	 * The target object can be <code>null</code> when invoking a static {@link Method}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, null);
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object with the supplied arguments
	 * The target object can be null when invoking a static {@link Method}.
	 * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target, Object[] args) {
		try {
			return method.invoke(target, args);
		}
		catch (IllegalAccessException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException(
					"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
		catch (InvocationTargetException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException(
					"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	/**
	 * Determine whether the given field is a "public static final" constant.
	 * @param field the field to check
	 */
	public static boolean isPublicStaticFinal(Field field) {
		int modifiers = field.getModifiers();
		return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
	}

	/**
	 * Make the given field accessible, explicitly setting it accessible if necessary.
	 * The <code>setAccessible(true)</code> method is only called when actually necessary,
	 * to avoid unnecessary conflicts with a JVM SecurityManager (if active).
	 * @param field the field to make accessible
	 * @see java.lang.reflect.Field#setAccessible
	 */
	public static void makeAccessible(Field field) {
		if (!Modifier.isPublic(field.getModifiers()) ||
				!Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}


	/**
	 * Perform the given callback operation on all matching methods of the given class
	 * and superclasses.
	 * <p>The same named method occurring on subclass and superclass will
	 * appear twice, unless excluded by the MethodFilter
	 * @param targetClass class to start looking at
	 * @param mc the callback to invoke for each method
	 */
	public static void doWithMethods(Class targetClass, MethodCallback mc) throws IllegalArgumentException {
		doWithMethods(targetClass, mc, null);
	}

	/**
	 * Perform the given callback operation on all matching methods of the given class
	 * and superclasses.
	 * <p>The same named method occurring on subclass and superclass will
	 * appear twice, unless excluded by the MethodFilter
	 * @param targetClass class to start looking at
	 * @param mc the callback to invoke for each method
	 * @param mf the filter that determines the methods to apply the callback to
	 */
	public static void doWithMethods(Class targetClass, MethodCallback mc, MethodFilter mf)
			throws IllegalArgumentException {

		// Keep backing up the inheritance hierarchy.
		do {
			Method[] methods = targetClass.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (mf != null && !mf.matches(methods[i])) {
					continue;
				}
				try {
					mc.doWith(methods[i]);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException(
							"Shouldn't be illegal to access method '" + methods[i].getName() + "': " + ex);
				}
			}
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null);
	}

	/**
	 * Get all declared methods on the leaf class and all superclasses.
	 * Leaf class methods are included first.
	 */
	public static Method[] getAllDeclaredMethods(Class leafClass) throws IllegalArgumentException {
		final List l = new LinkedList();
		doWithMethods(leafClass, new MethodCallback() {
			public void doWith(Method m) {
				l.add(m);
			}
		});
		return (Method[]) l.toArray(new Method[l.size()]);
	}

	/**
	 * Invoke the given callback on all private fields in the target class,
	 * going up the class hierarchy to get all declared fields.
	 * @param targetClass the target class to analyze
	 * @param fc the callback to invoke for each field
	 */
	public static void doWithFields(Class targetClass, FieldCallback fc) throws IllegalArgumentException {
		doWithFields(targetClass, fc, null);
	}

	/**
	 * Invoke the given callback on all private fields in the target class,
	 * going up the class hierarchy to get all declared fields.
	 * @param targetClass the target class to analyze
	 * @param fc the callback to invoke for each field
	 * @param ff the filter that determines the fields to apply the callback to
	 */
	public static void doWithFields(Class targetClass, FieldCallback fc, FieldFilter ff)
			throws IllegalArgumentException {

		// Keep backing up the inheritance hierarchy.
		do {
			// Copy each field declared on this class unless it's static or file.
			Field[] fields = targetClass.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				// Skip static and final fields.
				if (ff != null && !ff.matches(fields[i])) {
					continue;
				}
				try {
					fc.doWith(fields[i]);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalStateException(
							"Shouldn't be illegal to access field '" + fields[i].getName() + "': " + ex);
				}
			}
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);
	}

	/**
	 * Given the source object and the destination, which must be the same class or a subclass,
	 * copy all fields, including inherited fields. Designed to work on objects with public
	 * no-arg constructors.
	 * @throws IllegalArgumentException if arguments are incompatible or either is <code>null</code>
	 */
	public static void shallowCopyFieldState(final Object src, final Object dest) throws IllegalArgumentException {
		if (src == null) {
			throw new IllegalArgumentException("Source for field copy cannot be null");
		}
		if (dest == null) {
			throw new IllegalArgumentException("Destination for field copy cannot be null");
		}
		if (!src.getClass().isAssignableFrom(dest.getClass())) {
			throw new IllegalArgumentException("Destination class [" + dest.getClass().getName() +
					"] must be same or subclass as source class [" + src.getClass().getName() + "]");
		}
		doWithFields(src.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				makeAccessible(field);
				Object srcValue = field.get(src);
				field.set(dest, srcValue);
			}
		}, ReflectionUtils.COPYABLE_FIELDS);
	}


	/**
	 * Action to take on each method
	 */
	public static interface MethodCallback {

		/**
		 * Perform an operation using the given method.
		 * @param method method which will have been made accessible before this invocation
		 */
		void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
	}


	/**
	 * Callback optionally used to method fields to be operated on by a method callback.
	 */
	public static interface MethodFilter {

		/**
		 * Return whether the given method matches.
		 * @param method the method to check
		 */
		boolean matches(Method method);
	}


	/**
	 * Callback interface invoked on each field in the hierarchy.
	 */
	public static interface FieldCallback {

		/**
		 * Perform an operation using the given field.
		 * @param field field which will have been made accessible before this invocation
		 */
		void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
	}


	/**
	 * Callback optionally used to filter fields to be operated on by a field callback.
	 */
	public static interface FieldFilter {

		/**
		 * Return whether the given field matches.
		 * @param field the field to check
		 */
		boolean matches(Field field);
	}


	/**
	 * FieldFilter that matches all non-static, non-final fields.
	 */
	public static FieldFilter COPYABLE_FIELDS = new FieldFilter() {
		public boolean matches(Field field) {
			return !(Modifier.isStatic(field.getModifiers()) ||
					Modifier.isFinal(field.getModifiers()));
		}
	};

}
