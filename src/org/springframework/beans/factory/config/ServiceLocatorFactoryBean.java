/*
 * Copyright 2002-2004 the original author or authors.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * FactoryBean which, given an interface which must have one or more methods
 * with the signatures <code>getXXX()<code> or <code>getXXX(String id)<code>
 * creates a proxy which implements that interface.
 *
 * <p>On invocation of the no-arg factory method, or the String-arg factory method with an
 * id of null, if exactly one bean in the factory matches the return type of the 'get' method,
 * that is returned, otherwise a NoSuchBeanDefinitionException is thrown. On invocation of
 * the String-arg factory method with a non-null argument, the proxy returns the result of a
 * <code>BeanFactory.getBean(id)</code> call.
 * 
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.1.4
 */
public class ServiceLocatorFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

	private Class serviceLocatorInterface;

	private ListableBeanFactory beanFactory;

	private Object proxy;


	/**
	 * Set the service locator interface to use.
	 * See class-level javadoc for details on service locator method signatures.
	 */
	public void setServiceLocatorInterface(Class interfaceName) {
		this.serviceLocatorInterface = interfaceName;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new FatalBeanException(
					"ServiceLocatorFactoryBean needs to run in a BeanFactory that is a ListableBeanFactory");
		}
		this.beanFactory = (ListableBeanFactory) beanFactory;
	}

	public void afterPropertiesSet() throws BeansException {
		if (this.serviceLocatorInterface == null) {
			throw new FatalBeanException("serviceLocatorInterface is required");
		}
		this.proxy = Proxy.newProxyInstance(
				this.serviceLocatorInterface.getClassLoader(),
				new Class[] {this.serviceLocatorInterface},
				new ServiceLocatorInvocationHandler());
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
	 * Invocation handler that delegates service locator calls
	 * to the bean factory.
	 */
	private class ServiceLocatorInvocationHandler implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				Class[] paramTypes = method.getParameterTypes();
				Method interfaceMethod = serviceLocatorInterface.getMethod(method.getName(), paramTypes);
				if (!interfaceMethod.getName().startsWith("get") ||
						!((paramTypes.length == 0) || (paramTypes.length == 1 && String.class.equals(paramTypes[0])))) {
					throw new UnsupportedOperationException(
							"May only call methods with signature '<type> getXXX()' or '<type> getXXX(String id)' " +
							"on factory interface, but tried to call: " + interfaceMethod);
				}
				Class serviceLocatorReturnType = interfaceMethod.getReturnType();
				String beanName = null;
				if (args != null) {
					// Service locator method for a specific bean name.
					beanName = (String) args[0];
				}
				if (beanName != null) {
					return beanFactory.getBean(beanName);
				}
				else {
					// Service locator method for a specific bean type.
					return BeanFactoryUtils.beanOfTypeIncludingAncestors(beanFactory, serviceLocatorReturnType);
				}
			}
			catch (NoSuchMethodException ex) {
				// It's normal to get here for non service locator interface method calls
				// (toString, equals, etc).
				try {
					return method.invoke(this, args);
				}
				catch (InvocationTargetException invEx) {
					throw invEx.getTargetException();
				}
			}
		}

		public String toString() {
			return "Service locator: " + serviceLocatorInterface.getName();
		}
	}

}
