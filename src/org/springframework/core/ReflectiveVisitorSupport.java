/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Helper implementation of a reflective visitor.
 * <p>
 * To use, call <code>invokeVisit</code>, passing a Visitor object and the
 * data argument to accept (double-dispatch.) For example:
 * 
 * <pre>
 *    public String ToStringStyler.styleValue(Object value) {
 *       reflectiveVistorSupport.invokeVisit(this, value)
 *    }
 *  
 *    // visit call back will be invoked via reflection
 *    public String visit(&lt;valueType&gt; arg) {
 *       // process argument of type &lt;valueType&gt; 
 *    }
 * </pre>
 * 
 * @author Keith Donald
 */
public final class ReflectiveVisitorSupport {
	private static final Log logger = LogFactory.getLog(ReflectiveVisitorSupport.class);

	private static final String VISIT_METHOD = "visit";

	private static final String VISIT_NULL = "visitNull";

	private CachingMapDecorator visitorClassVisitMethods = new CachingMapDecorator() {
		public Object create(Object key) {
			return new ClassVisitMethods((Class)key);
		}
	};

	/**
	 * Internal class caching visitor methods by argument class.
	 */
	private static final class ClassVisitMethods {
		private Class visitorClass;

		private Method defaultVisitMethod;

		private CachingMapDecorator visitMethodCache = new CachingMapDecorator() {
			public Object create(Object argumentClazz) {
				if (argumentClazz == null) {
					return findNullVisitorMethod();
				}
				Method method = findVisitMethod((Class)argumentClazz);
				if (method == null) {
					method = findDefaultVisitMethod();
				}
				return method;
			}
		};

		private ClassVisitMethods(Class visitorClass) {
			this.visitorClass = visitorClass;
		}

		private Method findNullVisitorMethod() {
			for (Class clazz = visitorClass; clazz != null; clazz = clazz.getSuperclass()) {
				try {
					return clazz.getDeclaredMethod(VISIT_NULL, (Class[])null);
				}
				catch (NoSuchMethodException e) {
				}
			}
			return findDefaultVisitMethod();
		}

		private Method findDefaultVisitMethod() {
			if (defaultVisitMethod != null) {
				return defaultVisitMethod;
			}
			final Class[] args = { Object.class };
			for (Class clazz = visitorClass; clazz != null; clazz = clazz.getSuperclass()) {
				try {
					return clazz.getDeclaredMethod(VISIT_METHOD, args);
				}
				catch (NoSuchMethodException e) {
				}
			}
			logger.warn("No default '" + VISIT_METHOD + "' method found.  Returning <null>");
			return null;
		}

		/**
		 * Gets a cached visitor method for the specified argument type.
		 * 
		 * @param clazz Method parameter type.
		 * @return Visitor method with parameter type.
		 */
		public Method getVisitMethod(Class argumentClass) {
			return (Method)visitMethodCache.get(argumentClass);
		}

		/**
		 * Traverses class hierarchy looking for applicable visit() method.
		 */
		private Method findVisitMethod(Class rootArgumentType) {
			if (rootArgumentType == Object.class) {
				return null;
			}
			LinkedList classQueue = new LinkedList();
			classQueue.addFirst(rootArgumentType);

			while (!classQueue.isEmpty()) {
				Class argumentType = (Class)classQueue.removeLast();
				// check for a visit method on the visitor class matching this
				// argument type
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Looking for method " + VISIT_METHOD + "(" + argumentType + ")");
					}
					return findVisitMethod(visitorClass, argumentType);
				}
				catch (NoSuchMethodException e) {
					// queue up the argument super class if it's not of type
					// Object.
					if (!argumentType.isInterface() && (argumentType.getSuperclass() != Object.class)) {
						classQueue.addFirst(argumentType.getSuperclass());
					}

					// queue up argument's implemented interfaces.
					Class[] interfaces = argumentType.getInterfaces();
					for (int i = 0; i < interfaces.length; i++) {
						classQueue.addFirst(interfaces[i]);
					}
				}
			}
			// none found, return the default.
			return findDefaultVisitMethod();
		}

		private Method findVisitMethod(Class visitorClass, Class argumentType) throws NoSuchMethodException {
			try {
				return visitorClass.getDeclaredMethod(VISIT_METHOD, new Class[] { argumentType });
			}
			catch (NoSuchMethodException e) {
				// try visitorClass superclasses
				if (visitorClass.getSuperclass() != Object.class) {
					return findVisitMethod(visitorClass.getSuperclass(), argumentType);
				}
				else {
					throw e;
				}
			}
		}
	}

	/**
	 * Use reflection to call the appropriate visit method on the provided
	 * visitor, passing in the specified argument.
	 * 
	 * If the arg is Visitable, accept(Visitor) will be called instead, and the
	 * it will be expected to perform the double-dispatch. Otherwise the visit
	 * method will be called directly on the visitor based on the class of the
	 * argument.
	 * 
	 * @param visitor The visitor encapsulating the logic to process the
	 *        argument
	 * @param argument The argument to dispatch, or a instanceof Vistable
	 * @throws IllegalArgumentException if the visitor parameter is null
	 */
	public final Object invokeVisit(Object visitor, Object argument) {
		Assert.notNull(visitor, "The visitor to visit is required");
		// perform call back on the visitor by reflection
		Method method = getMethod(visitor.getClass(), argument);
		if (method == null) {
			logger.warn("No method found by reflection for visitor class '" + visitor.getClass()
					+ "' and argument of type '" + (argument != null ? argument.getClass() : null) + "'");
			return null;
		}
		try {
			Object[] args = null;
			if (argument != null) {
				args = new Object[] { argument };
			}
			if (!Modifier.isPublic(method.getModifiers()) && method.isAccessible() == false) {
				method.setAccessible(true);
			}
			return method.invoke(visitor, args);

		}
		catch (InvocationTargetException e) {
			logger.error("Invocation target exception invoking method '" + method.getName() + "(" + argument + ")@"
					+ visitor.getClass() + "'" + e.getTargetException());
			throw new ReflectiveDispatchException("Exception occured invoking method '" + method.getName()
					+ "' on visitor", e);
		}
		catch (Exception e) {
			throw new ReflectiveDispatchException("Unable to invoke visit method on visitor", e);
		}
	}

	public static class ReflectiveDispatchException extends NestedRuntimeException {
		public ReflectiveDispatchException(String msg, Throwable ex) {
			super(msg, ex);
		}
	}

	private Method getMethod(Class visitorClass, Object argument) {
		ClassVisitMethods visitMethods = (ClassVisitMethods)visitorClassVisitMethods.get(visitorClass);
		return visitMethods.getVisitMethod((argument != null ? argument.getClass() : null));
	}
}