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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.StringUtils;

/**
 * FactoryBean that takes an interface which must have one or more methods with
 * the signatures <code>MyType xxx()<code> or <code>MyType xxx(MyIdType id)<code>
 * (typically, <code>MyService getService()<code> or <code>MyService getService(String id)<code>)
 * and creates a dynamic proxy which implements that interface, delegating to the
 * Spring BeanFactory underneath.
 *
 * <p>Such service locator allow to decouple the caller from Spring BeanFactory API, using an
 * appropriate custom locator interface. They will typically be used for <b>prototype beans</b>,
 * i.e. for factory methods that are supposed to return a new instance for each call.
 * The client receives a reference to the service locator via setter or constructor injection,
 * being able to invoke the locator's factory methods on demand. <b>For singleton beans,
 * direct setter or constructor injection of the target bean is preferable.</b>
 *
 * <p>On invocation of the no-arg factory method, or the single-arg factory method with an id
 * of null or empty String, if exactly one bean in the factory matches the return type of the
 * factory method, that is returned, otherwise a NoSuchBeanDefinitionException is thrown.
 *
 * <p>On invocation of the single-arg factory method with a non-null (and non-empty) argument,
 * the proxy returns the result of a <code>BeanFactory.getBean(name)</code> call, using a
 * stringified version of the passed-in id as bean name.
 *
 * <p>A factory method argument will usually be a String, but can also be an int or a custom
 * enumeration type, for example, stringified via <code>toString</code>. The resulting String
 * can be used as bean name as-is, provided that corresponding beans are defined in the bean
 * factory. Alternatively, mappings between service ids and bean names can be defined.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see #setServiceLocatorInterface
 * @see #setServiceMappings
 */
public class ServiceLocatorFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

	private Class serviceLocatorInterface;

	private Constructor serviceLocatorExceptionConstructor;

	private Properties serviceMappings;

	private ListableBeanFactory beanFactory;

	private Object proxy;


	/**
	 * Set the service locator interface to use, which must have one or more methods with
	 * the signatures <code>MyType xxx()<code> or <code>MyType xxx(MyIdType id)<code>
	 * (typically, <code>MyService getService()<code> or <code>MyService getService(String id)<code>).
	 * See class-level javadoc for information on the semantics of such methods.
	 */
	public void setServiceLocatorInterface(Class interfaceName) {
		this.serviceLocatorInterface = interfaceName;
	}

	/**
	 * Set the exception class that the service locator should throw if service
	 * lookup failed. The specified exception class should have a constructor
	 * with one of the following parameter types: <code>(String, Throwable)</code>
	 * or <code>(Throwable)</code> or <code>(String)</code>.
	 * <p>If not specified, subclasses of Spring's BeansException will be thrown,
	 * for example NoSuchBeanDefinitionException. As those are unchecked, the
	 * caller does not need to handle them, so it might be acceptable that
	 * Spring exceptions get thrown as long as they are just handled generically.
	 * @see #determineServiceLocatorExceptionConstructor(Class)
	 * @see #createServiceLocatorException(java.lang.reflect.Constructor, org.springframework.beans.BeansException)
	 * @see org.springframework.beans.BeansException
	 * @see org.springframework.beans.factory.NoSuchBeanDefinitionException
	 */
	public void setServiceLocatorExceptionClass(Class serviceLocatorExceptionClass) {
		if (serviceLocatorExceptionClass != null && !Exception.class.isAssignableFrom(serviceLocatorExceptionClass)) {
			throw new IllegalArgumentException(
					"serviceLocatorException [" + serviceLocatorExceptionClass.getName() + "] is not a subclass of Exception");
		}
		this.serviceLocatorExceptionConstructor =
				determineServiceLocatorExceptionConstructor(serviceLocatorExceptionClass);
	}

	/**
	 * Set mappings between service ids (passed into the service locator)
	 * and bean names (in the bean factory). Service ids that are not defined
	 * here will be treated as bean names as-is.
	 * <p>The empty string as service id key defines the mapping for <code>null</code> and
	 * empty string, and for factory methods without parameter. If not defined,
	 * a single matching bean will be retrieved from the bean factory.
	 * @param serviceMappings mappings between service ids and bean names,
	 * with service ids as keys as bean names as values
	 */
	public void setServiceMappings(Properties serviceMappings) {
		this.serviceMappings = serviceMappings;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new FatalBeanException(
					"ServiceLocatorFactoryBean needs to run in a BeanFactory that is a ListableBeanFactory");
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	public void afterPropertiesSet() {
		if (this.serviceLocatorInterface == null) {
			throw new IllegalArgumentException("serviceLocatorInterface is required");
		}

		// Create service locator proxy.
		this.proxy = Proxy.newProxyInstance(
				this.serviceLocatorInterface.getClassLoader(),
				new Class[] {this.serviceLocatorInterface},
				new ServiceLocatorInvocationHandler());
	}


	/**
	 * Determine the constructor to use for the given service locator exception
	 * class. Only called in case of a custom service locator exception.
	 * <p>The default implementation looks for a constructor with one of the
	 * following parameter types: <code>(String, Throwable)</code>
	 * or <code>(Throwable)</code> or <code>(String)</code>.
	 * @param exceptionClass the exception class
	 * @return the constructor to use
	 * @see #setServiceLocatorExceptionClass
	 */
	protected Constructor determineServiceLocatorExceptionConstructor(Class exceptionClass) {
		try {
			return exceptionClass.getConstructor(new Class[] {String.class, Throwable.class});
		}
		catch (NoSuchMethodException ex) {
			try {
				return exceptionClass.getConstructor(new Class[] {Throwable.class});
			}
			catch (NoSuchMethodException ex2) {
				try {
					return exceptionClass.getConstructor(new Class[] {String.class});
				}
				catch (NoSuchMethodException ex3) {
					throw new IllegalArgumentException(
							"serviceLocatorException [" + exceptionClass.getName() +
							"] neither has a (String, Throwable) constructor nor a (String) constructor");
				}
			}
		}
	}

	/**
	 * Create a service locator exception for the given cause.
	 * Only called in case of a custom service locator exception.
	 * <p>The default implementation can handle all variations of
	 * message and exception arguments.
	 * @param exceptionConstructor the constructor to use
	 * @param cause the cause of the service lookup failure
	 * @return the service locator exception to throw
	 * @see #setServiceLocatorExceptionClass
	 */
	protected Exception createServiceLocatorException(Constructor exceptionConstructor, BeansException cause) {
		Class[] paramTypes = exceptionConstructor.getParameterTypes();
		Object[] args = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			if (paramTypes[i].equals(String.class)) {
				args[i] = cause.getMessage();
			}
			else if (paramTypes[i].isInstance(cause)) {
				args[i] = cause;
			}
		}
		return (Exception) BeanUtils.instantiateClass(exceptionConstructor, args);
	}


	public Object getObject() {
		return this.proxy;
	}

	public Class getObjectType() {
		return this.serviceLocatorInterface;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Invocation handler that delegates service locator calls to the bean factory.
	 */
	private class ServiceLocatorInvocationHandler implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (Object.class.equals(method.getDeclaringClass())) {
				// It's normal to get here for non service locator interface method calls
				// (toString, equals, etc). Simply apply call to invocation handler object.
				try {
					return method.invoke(this, args);
				}
				catch (InvocationTargetException invEx) {
					throw invEx.getTargetException();
				}
			}

			Class[] paramTypes = method.getParameterTypes();
			Method interfaceMethod = serviceLocatorInterface.getMethod(method.getName(), paramTypes);
			Class serviceLocatorReturnType = interfaceMethod.getReturnType();

			// Check whether the method is a valid service locator.
			if (paramTypes.length > 1 || void.class.equals(serviceLocatorReturnType)) {
				throw new UnsupportedOperationException(
						"May only call methods with signature '<type> xxx()' or '<type> xxx(<idtype> id)' " +
						"on factory interface, but tried to call: " + interfaceMethod);
			}

			// Check whether a service id was passed in.
			String beanName = "";
			if (args != null && args.length == 1 && args[0] != null) {
				beanName = args[0].toString();
			}

			// Look for explicit serviceId-to-beanName mappings.
			if (serviceMappings != null) {
				String mappedName = serviceMappings.getProperty(beanName);
				if (mappedName != null) {
					beanName = mappedName;
				}
			}

			try {
				if (StringUtils.hasLength(beanName)) {
					// Service locator for a specific bean name.
					return beanFactory.getBean(beanName, serviceLocatorReturnType);
				}
				else {
					// Service locator for a bean type.
					return BeanFactoryUtils.beanOfTypeIncludingAncestors(beanFactory, serviceLocatorReturnType);
				}
			}
			catch (BeansException ex) {
				if (serviceLocatorExceptionConstructor != null) {
					throw createServiceLocatorException(serviceLocatorExceptionConstructor, ex);
				}
				throw ex;
			}
		}

		public String toString() {
			return "Service locator: " + serviceLocatorInterface.getName();
		}
	}

}
