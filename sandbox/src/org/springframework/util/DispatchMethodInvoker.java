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
package org.springframework.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.core.NestedRuntimeException;

/**
 * Invoker and cache for dispatch methods that all share the same target object.
 * The dispatch methods typically share the same form, but multiple exist per target
 * object, and they only differ in name.
 * @author Keith Donald
 */
public class DispatchMethodInvoker {

	/**
	 * The target object to cache methods on.
	 */
	private Object target;

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
	 * The description of the signature.
	 */
	private String signatureCaption = "[not specified]";

	/**
	 * The resolved method cache.
	 */
	private Map methodCache = new CachingMapTemplate() {
		public Object create(Object key) {
			String methodName = (String)key;
			try {
				return getTarget().getClass().getMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException e) {
				throw new MethodLookupException("Unable to resolve " + getTypeCaption() + " method with name '"
						+ methodName + "' and signature '" + getSignatureCaption() + "'; make sure the method name is correct "
						+ "and such a public method is defined on targetClass " + getTarget().getClass().getName(), e);
			}
		}
	};

	/**
	 * Create a method cache on the target object.
	 * @param the parameter types defining the form of the dispatch method
	 */
	public DispatchMethodInvoker(Class[] parameterTypes, Class returnType, String typeCaption,
			String signatureCaption) {
		setParameterTypes(parameterTypes);
		setReturnType(returnType);
		setTypeCaption(typeCaption);
		setSignatureCaption(signatureCaption);
	}

	/**
	 * Create a method cache on the target object.
	 * @param target the object
	 * @param the parameter types defining the form of the dispatch method
	 */
	public DispatchMethodInvoker(Object target, Class[] parameterTypes, Class returnType, String typeCaption,
			String signatureCaption) {
		setTarget(target);
		setParameterTypes(parameterTypes);
		setReturnType(returnType);
		setTypeCaption(typeCaption);
		setSignatureCaption(signatureCaption);
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
	 * @param parameterTypes
	 */
	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
		this.methodCache.clear();
	}

	/**
	 * Sets the expected return type for the dispatch methods.
	 * @param returnType The expected return type
	 */
	public void setReturnType(Class returnType) {
		this.returnType = returnType;
	}

	/**
	 * @param methodTypeCaption The methodTypeCaption to set.
	 */
	public void setTypeCaption(String typeCaption) {
		this.typeCaption = typeCaption;
	}

	/**
	 * @param signatureCaption The signatureCaption to set.
	 */
	public void setSignatureCaption(String signatureCaption) {
		this.signatureCaption = signatureCaption;
	}

	/**
	 * Returns a optional description of the type of method resolved by this cache.
	 * @return the method type description
	 */
	public String getTypeCaption() {
		return typeCaption;
	}

	/**
	 * Returns a optional description of the expected signature of methods resolved
	 * by this cache.
	 * @return the method type description
	 */
	public String getSignatureCaption() {
		return signatureCaption;
	}

	/**
	 * Returns the target object holding the methods.
	 * Defaults to this object.
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Returns the parameter types of describing the form of the dispatch method.
	 * @return the parameter types
	 */
	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Returns the expected return type of the dispatch methods.
	 * @return the expected return type, or null if void
	 */
	public Class getReturnType() {
		return returnType;
	}

	/**
	 * Get a handle to the method of the specified name, with the signature defined by the
	 * configured parameterTypes.
	 * @param methodName the method name
	 * @return the method
	 */
	public Method getMethod(String methodName) {
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

	public Object dispatch(String methodName, Object[] arguments) throws Exception {
		try {
			Method dispatchMethod = getMethod(methodName);
			Object result = dispatchMethod.invoke(getTarget(), arguments);
			if (result != null && returnType != null) {
				Assert.isInstanceOf(returnType, result, "Dispatched " + getTypeCaption()
						+ " methods must return result objects of type '" + getReturnType() + "' or null; "
						+ "however, this method '" + dispatchMethod.getName() + "' returned an object of type "
						+ result.getClass());
			}
			return result;
		} catch (InvocationTargetException e) {
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