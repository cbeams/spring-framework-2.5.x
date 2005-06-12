/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.core.CachingMapDecorator;
import org.springframework.core.NestedRuntimeException;
import org.springframework.util.Assert;

/**
 * Invoker and cache for dispatch methods that all share the same target object.
 * The dispatch methods typically share the same form, but multiple exist per target
 * object, and they only differ in name.
 * 
 * @author Keith Donald
 */
public class DispatchMethodInvoker {

	/**
	 * The target object to cache methods on.
	 */
	private Object target = this;

	/**
	 * The method parameter types describing the form of the dispatcher methods.
	 */
	private Class[] parameterTypes;

	/**
	 * The dispatch method return type
	 */
	private Class returnType;

	/**
	 * A description of the method type.
	 */
	private String typeCaption = "dispatcher";

	/**
	 * The resolved method cache.
	 */
	private Map methodCache = new CachingMapDecorator() {
		public Object create(Object key) {
			String methodName = (String)key;
			try {
				return getTarget().getClass().getMethod(methodName, parameterTypes);
			}
			catch (NoSuchMethodException e) {
				throw new MethodLookupException("Unable to resolve " + getTypeCaption() + " method with name '"
						+ methodName + "' and signature '" + getSignature(methodName)
						+ "'; make sure the method name is correct "
						+ "and such a public method is defined on targetClass " + getTarget().getClass().getName(), e);
			}
		}
	};

	/**
	 * Creates a still-to-be-configured dispatch method invoker.
	 * 
	 * @see #setParameterTypes(Class[])
	 * @see #setReturnType(Class)
	 * @see #setTypeCaption(String)
	 * @see #setTarget(Object)
	 */
	public DispatchMethodInvoker() {
	}

	/**
	 * Creates a dispatch method invoker.
	 * @param parameterTypes the parameter types defining the form of the
	 *        dispatch methods
	 * @param returnType the return type of the dispatch methods, use null for void
	 * @param typeCaption a description of the method type
	 * @see #setTarget(Object)
	 */
	public DispatchMethodInvoker(Class[] parameterTypes, Class returnType, String typeCaption) {
		setParameterTypes(parameterTypes);
		setReturnType(returnType);
		setTypeCaption(typeCaption);
	}

	/**
	 * Creates a dispatch method invoker.
	 * @param target the object
	 * @param parameterTypes the parameter types defining the form of the
	 *        dispatch methods
	 * @param returnType the return type of the dispatch methods, use null for void
	 * @param typeCaption a description of the method type
	 */
	public DispatchMethodInvoker(Object target, Class[] parameterTypes, Class returnType, String typeCaption) {
		setTarget(target);
		setParameterTypes(parameterTypes);
		setReturnType(returnType);
		setTypeCaption(typeCaption);
	}

	/**
	 * Set the target object holding the methods.
	 * @param target the delegate to set
	 */
	public void setTarget(Object target) {
		Assert.notNull(target, "The target object is required");
		this.target = target;
		this.methodCache.clear();
	}

	/**
	 * Set the parameter types defining the form of the dispatch methods.
	 */
	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
		this.methodCache.clear();
	}

	/**
	 * Sets the expected return type for the dispatch methods.
	 * @param returnType the expected return type, or null for void
	 */
	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}

	/**
	 * Set the method type description (e.g. "action").
	 */
	public void setTypeCaption(String typeCaption) {
		this.typeCaption = typeCaption;
	}

	/**
	 * Returns the target object holding the methods.
	 * Defaults to this object.
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Returns the parameter types describing the form of the dispatch method.
	 * @return the parameter types
	 */
	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Convenience method that returns the parameter types describing the form of
	 * the dispatch method as a string.
	 */
	protected String getParameterTypesString() {
		StringBuffer parameterTypesString = new StringBuffer();
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypesString.append(parameterTypes[i]);
			if (i < parameterTypes.length - 1) {
				parameterTypesString.append(',');
			}
		}
		return parameterTypesString.toString();
	}
	
	/**
	 * Returns the expected return type of the dispatch methods.
	 * @return the expected return type, or null if void
	 */
	public Class getReturnType() {
		return returnType;
	}

	/**
	 * Convenience method that returns the return type of the dispatch methods
	 * as a string.
	 */
	protected String getReturnTypeString() {
		return (returnType != null ? returnType.getName() : "void");
	}
	
	/**
	 * Returns a optional description of the type of method resolved by this cache.
	 * @return the method type description
	 */
	public String getTypeCaption() {
		return typeCaption;
	}

	/**
	 * Returns the signature of the dispatch methods invoked by this class.
	 * @param methodName name of the dispatch method
	 */
	protected String getSignature(String methodName) {
		return "public " + getReturnTypeString() + " " + methodName + "(" + getParameterTypesString() + ");";
	}
	
	/**
	 * Get a handle to the method of the specified name, with the signature defined by the
	 * configured parameter types and return type.
	 * @param methodName the method name
	 * @return the method
	 */
	public Method getDispatchMethod(String methodName) {
		return (Method)this.methodCache.get(methodName);
	}

	/**
	 * Thrown when a method could not be resolved.
	 */
	public static class MethodLookupException extends NestedRuntimeException {
		public MethodLookupException(String msg, Throwable ex) {
			super(msg, ex);
		}
	}

	/**
	 * Dispatch a call with given arguments to named dispatcher method.
	 * @param methodName the name of the method to invoke
	 * @param arguments the arguments to pass to the method
	 * @return the result of the method invokation
	 * @throws Exception when the invoked method throws an exception
	 */
	public Object dispatch(String methodName, Object[] arguments) throws Exception {
		try {
			Method dispatchMethod = getDispatchMethod(methodName);
			Object result = dispatchMethod.invoke(getTarget(), arguments);
			if (result != null && returnType != null) {
				Assert.isInstanceOf(returnType, result, "Dispatched " + getTypeCaption()
						+ " methods must return result objects of type '" + getReturnType() + "' or null; "
						+ "however, this method '" + dispatchMethod.getName() + "' returned an object of type "
						+ result.getClass());
			}
			return result;
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof Exception) {
				throw (Exception)e.getTargetException();
			}
			else {
				throw (Error)e.getTargetException();
			}
		}
	}
}