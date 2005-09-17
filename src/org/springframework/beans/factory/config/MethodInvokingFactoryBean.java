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

package org.springframework.beans.factory.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.MethodInvoker;

/**
 * <p>FactoryBean which returns a value which is the result of a static or instance
 * method invocation. For most use cases it is better to just use the container's 
 * built-in factory-method support for the same purpose, since that is smarter at
 * converting arguments. This factory bean is still useful though when you need to
 * call a method which doesn't return any value (for example, a static class method
 * to force some sort of initialization to happen). This use case is not supported
 * by factory-methods, since a return value is needed to become the bean.</p.
 *
 * <p>Note that as it is expected to be used mostly for accessing factory methods,
 * this factory by default operates in a <b>singleton</b> fashion. The first request
 * to {@link #getObject} by the owning bean factory will cause a method invocation,
 * whose return value will be cached for subsequent requests. An internal
 * {@link #setSingleton singleton} property may be set to "false", to cause this
 * factory to invoke the target method each time it is asked for an object.</p>
 *
 * <p>A static target method may be specified by setting the
 * {@link #setTargetMethod targetMethod} property to a String representing the static
 * method name, with {@link #setTargetClass targetClass} specifying the Class that
 * the static method is defined on. Alternatively, a target instance method may be
 * specified, by setting the {@link #setTargetObject targetObject} property as the target
 * object, and the {@link #setTargetMethod targetMethod} property as the name of the
 * method to call on that target object. Arguments for the method invocation may be
 * specified by setting the {@link #setArguments arguments} property.</p>
 *
 * <p>This class depends on {@link #afterPropertiesSet()} being called once
 * all properties have been set, as per the InitializingBean contract.</p>
 * 
 * <p>Note that this factory bean will return the special
 * {@link org.springframework.util.MethodInvoker#VOID} singleton instance when it is
 * used to invoke a method which returns null, or has a void return type. While the
 * user of the factory bean is presumably calling the method to perform some sort of
 * initialization, and doesn't care about any return value, all factory beans must
 * return a value, so this special singleton instance is used for this case.</p>
 *
 * <p>An example (in an XML based bean factory definition) of a bean definition
 * which uses this class to call a static factory method:</p>
 *
 * <pre>
 * &lt;bean id="myObject" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="staticMethod">&lt;value>com.whatever.MyClassFactory.getInstance&lt;/value>&lt;/property>
 * &lt;/bean></pre>
 *
 * <p>An example of calling a static method then an instance method to get at a
 * Java system property. Somewhat verbose, but it works.
 *
 * <pre>
 * &lt;bean id="sysProps" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="targetClass">&lt;value>java.lang.System&lt;/value>&lt;/property>
 *   &lt;property name="targetMethod">&lt;value>getProperties&lt;/value>&lt;/property>
 * &lt;/bean>
 *
 * &lt;bean id="javaVersion" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
 *   &lt;property name="targetObject">&lt;ref local="sysProps"/>&lt;/property>
 *   &lt;property name="targetMethod">&lt;value>getProperty&lt;/value>&lt;/property>
 *   &lt;property name="arguments">
 *     &lt;list>
 *       &lt;value>java.version&lt;/value>
 *     &lt;/list>
 *   &lt;/property>
 * &lt;/bean></pre>
 * 
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 21.11.2003
 */
public class MethodInvokingFactoryBean extends ArgumentConvertingMethodInvoker
		implements FactoryBean, InitializingBean {

	private boolean singleton = true;

	/** Method call result in the singleton case */
	private Object singletonObject;


	/**
	 * Set if a singleton should be created, or a new object on each
	 * request else. Default is "true".
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void afterPropertiesSet() throws Exception {
		prepare();
		if (this.singleton) {
			Object obj = doInvoke();
			this.singletonObject = (obj != null ? obj : MethodInvoker.VOID);
		}
	}

	/**
	 * Perform the invocation and convert InvocationTargetException
	 * into the underlying target exception.
	 */
	private Object doInvoke() throws Exception {
		try {
			return invoke();
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof Exception) {
				throw (Exception) ex.getTargetException();
			}
			if (ex.getTargetException() instanceof Error) {
				throw (Error) ex.getTargetException();
			}
			throw ex;
		}
	}


	/**
	 * Returns the same value each time if the singleton property is set
	 * to true, otherwise returns the value returned from invoking the
	 * specified method. However, returns {@link MethodInvoker#VOID} if the
	 * method returns null or has a void return type, since factory beans
	 * must return a result.
	 */
	public Object getObject() throws Exception {
		if (this.singleton) {
			// Singleton: return shared object.
			return this.singletonObject;
		}
		else {
			// Prototype: new object on each call.
			Object retVal = doInvoke();
			return (retVal != null ? retVal : MethodInvoker.VOID);
		}
	}

	public Class getObjectType() {
		Method preparedMethod = getPreparedMethod();
		if (preparedMethod == null) {
			// Not fully initialized yet ->
			// return null to indicate "not known yet".
			return null;
		}
		Class type = preparedMethod.getReturnType();
		if (type.equals(void.class)) {
			type = VoidType.class;
		}
		return type;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
