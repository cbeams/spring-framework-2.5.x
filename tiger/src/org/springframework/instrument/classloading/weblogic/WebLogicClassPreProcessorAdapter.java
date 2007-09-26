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
package org.springframework.instrument.classloading.weblogic;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;

/**
 * Adapter that implements WebLogic ClassPreProcessor interface, delegating to a
 * standard JDK {@link ClassFileTransformer} underneath.
 * 
 * <p/> To avoid compile time checks again the vendor API, a dynamic proxy is
 * being used.
 * 
 * @author Costin Leau
 * 
 */
public class WebLogicClassPreProcessorAdapter implements InvocationHandler {

	private final ClassFileTransformer transformer;

	private final ClassLoader loader;

	/**
	 * Creates a new instance of the {@link WebLogicClassPreProcessorAdapter}
	 * class.
	 * @param transformer the {@link ClassFileTransformer} to be adapted (must
	 * not be <code>null</code>)
	 * @throws IllegalArgumentException if the supplied <code>transformer</code>
	 * is <code>null</code>
	 */
	public WebLogicClassPreProcessorAdapter(ClassFileTransformer transformer, ClassLoader loader) {
		Assert.notNull(transformer, "Transformer must not be null");
		Assert.notNull(loader);
		this.transformer = transformer;
		this.loader = loader;
	}

	public void initialize(Hashtable params) {
	}

	public byte[] preProcess(String className, byte[] classBytes) {
		try {
			byte[] result = transformer.transform(loader, className, null, null, classBytes);
			return (result == null ? classBytes : result);
		}
		catch (IllegalClassFormatException ex) {
			throw new IllegalStateException("Cannot transform due to illegal class format", ex);
		}
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		if (AopUtils.isEqualsMethod(method)) {
			return ((proxy == args[0]) ? Boolean.TRUE : Boolean.FALSE);
		}
		if (AopUtils.isHashCodeMethod(method)) {
			return new Integer(hashCode());
		}
		// Invoke method on target Session.
		try {
			return method.invoke(this, args);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getName());
		builder.append(" for transformer: ");
		builder.append(this.transformer);
		return builder.toString();
	}
}
