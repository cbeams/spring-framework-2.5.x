/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.scripting.bsh;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.Primitive;
import bsh.XThis;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Utility methods for handling BeanShell-scripted objects.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class BshScriptUtils {

	/**
	 * Create a new BeanShell-scripted object from the given script source.
	 * <p>With this <code>createBshObject</code> variant, the script needs to
	 * declare a full class or return an actual instance of the scripted object.
	 * @param scriptSource the script source text
	 * @return the scripted Java object
	 * @throws EvalError in case of BeanShell parsing failure
	 */
	public static Object createBshObject(String scriptSource) throws EvalError {
		return createBshObject(scriptSource, null, null);
	}

	/**
	 * Create a new BeanShell-scripted object from the given script source,
	 * using the default ClassLoader.
	 * <p>The script may either be a simple script that needs a corresponding proxy
	 * generated (implementing the specified interfaces), or declare a full class
	 * or return an actual instance of the scripted object (in which case the
	 * specified interfaces, if any, need to be implemented by that class/instance).
	 * @param scriptSource the script source text
	 * @param scriptInterfaces the interfaces that the scripted Java object is
	 * supposed to implement (may be <code>null</code> or empty if the script itself
	 * declares a full class or returns an actual instance of the scripted object)
	 * @return the scripted Java object
	 * @throws EvalError in case of BeanShell parsing failure
	 * @see #createBshObject(String, Class[], ClassLoader)
	 */
	public static Object createBshObject(String scriptSource, Class[] scriptInterfaces) throws EvalError {
		return createBshObject(scriptSource, scriptInterfaces, ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Create a new BeanShell-scripted object from the given script source.
	 * <p>The script may either be a simple script that needs a corresponding proxy
	 * generated (implementing the specified interfaces), or declare a full class
	 * or return an actual instance of the scripted object (in which case the
	 * specified interfaces, if any, need to be implemented by that class/instance).
	 * @param scriptSource the script source text
	 * @param scriptInterfaces the interfaces that the scripted Java object is
	 * supposed to implement (may be <code>null</code> or empty if the script itself
	 * declares a full class or returns an actual instance of the scripted object)
	 * @param classLoader the ClassLoader to create the script proxy with
	 * @return the scripted Java object
	 * @throws EvalError in case of BeanShell parsing failure
	 */
	public static Object createBshObject(String scriptSource, Class[] scriptInterfaces, ClassLoader classLoader)
			throws EvalError {

		Object result = evaluateBshScript(scriptSource, scriptInterfaces, classLoader);
		if (result instanceof Class) {
			Class clazz = (Class) result;
			try {
				return clazz.newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Could not instantiate script class [" +
						clazz.getName() + "]. Root cause is " + ex);
			}
		}
		else {
			return result;
		}
	}

	/**
	 * Evaluate the specified BeanShell script based on the given script source,
	 * keeping a returned script Class or script Object as-is.
	 * <p>The script may either be a simple script that needs a corresponding proxy
	 * generated (implementing the specified interfaces), or declare a full class
	 * or return an actual instance of the scripted object (in which case the
	 * specified interfaces, if any, need to be implemented by that class/instance).
	 * @param scriptSource the script source text
	 * @param scriptInterfaces the interfaces that the scripted Java object is
	 * supposed to implement (may be <code>null</code> or empty if the script itself
	 * declares a full class or returns an actual instance of the scripted object)
	 * @param classLoader the ClassLoader to create the script proxy with
	 * @return the scripted Java class or Java object
	 * @throws EvalError in case of BeanShell parsing failure
	 */
	static Object evaluateBshScript(String scriptSource, Class[] scriptInterfaces, ClassLoader classLoader)
			throws EvalError {

		Assert.hasText(scriptSource, "Script source must not be empty");
		Interpreter interpreter = new Interpreter();
		Object result = interpreter.eval(scriptSource);
		if (result != null) {
			// Script returned result: Let's assume it's a full script class or an instance of the script.
			Class resultClass = (result instanceof Class ? (Class) result : result.getClass());
			if (scriptInterfaces != null) {
				for (int i = 0; i < scriptInterfaces.length; i++) {
					Class scriptInterface = scriptInterfaces[i];
					if (!isCompatibleWithInterface(resultClass, scriptInterface)) {
						throw new IllegalStateException("BeanShell script returned result object [" + result +
								"] which does not implement script interface [" + scriptInterface.getName() + "]");
					}
				}
			}
			return result;
		}
		else {
			// Simple BeanShell script: Let's create a proxy for it, implementing the given interfaces.
			Assert.notEmpty(scriptInterfaces,
					"Given script requires a script proxy: At least one script interface is required.");
			XThis xt = (XThis) interpreter.eval("return this");
			return Proxy.newProxyInstance(classLoader, scriptInterfaces, new BshObjectInvocationHandler(xt));
		}
	}

	/**
	 * Check whether the given script object is compatible with the specified interface.
	 * <p>In case of a CGLIB-generated config interface, we'll accept the object if it
	 * has at least a method of the same name for each config method in the interface.
	 * @param clazz the class of the script object, as returned by the BeanShell interpreter
	 * @param ifc the interface to check (may be a CGLIB-generated config interface)
	 * @return <code>true</code> if the object is considered as compatible
	 */
	private static boolean isCompatibleWithInterface(Class clazz, Class ifc) {
		if (ifc.isAssignableFrom(clazz)) {
			// A direct implementation...
			return true;
		}
		if (ifc.getName().indexOf("$$") != -1) {
			// A CGLIB-generated interface...
			Method[] ifcMethods = ifc.getMethods();
			for (int i = 0; i < ifcMethods.length; i++) {
				Method ifcMethod = ifcMethods[i];
				if (!ClassUtils.hasAtLeastOneMethodWithName(clazz, ifcMethod.getName())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}


	/**
	 * InvocationHandler that invokes a BeanShell script method.
	 */
	private static class BshObjectInvocationHandler implements InvocationHandler {

		private final XThis xt;

		public BshObjectInvocationHandler(XThis xt) {
			this.xt = xt;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (AopUtils.isEqualsMethod(method)) {
				return (isProxyForSameBshObject(args[0]) ? Boolean.TRUE : Boolean.FALSE);
			}
			if (AopUtils.isHashCodeMethod(method)) {
				return new Integer(this.xt.hashCode());
			}
			if (AopUtils.isToStringMethod(method)) {
				return "BeanShell object [" + this.xt + "]";
			}
			try {
				Object result = this.xt.invokeMethod(method.getName(), args);
				if (result == Primitive.NULL || result == Primitive.VOID) {
					return null;
				}
				if (result instanceof Primitive) {
					return ((Primitive) result).getValue();
				}
				return result;
			}
			catch (EvalError ex) {
				throw new BshExecutionException(ex);
			}
		}

		private boolean isProxyForSameBshObject(Object other) {
			if (!Proxy.isProxyClass(other.getClass())) {
				return false;
			}
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			return (ih instanceof BshObjectInvocationHandler &&
					this.xt.equals(((BshObjectInvocationHandler) ih).xt));
		}
	}


	/**
	 * Exception to be thrown on script execution failure.
	 */
	public static class BshExecutionException extends NestedRuntimeException {

		private BshExecutionException(EvalError ex) {
			super("BeanShell script execution failed", ex);
		}
	}

}
