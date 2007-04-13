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

package org.springframework.beans.factory.support;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.util.ObjectUtils;

/**
 * Programmatic means of constructing
 * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}
 * using the builder pattern. Intended primarily for use when implementing Spring 2.0
 * {@link org.springframework.beans.factory.xml.NamespaceHandler NamespaceHandlers}.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanDefinitionBuilder  {

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClass the <code>Class</code> of the bean that the definition is being created for
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
		return rootBeanDefinition(beanClass, null);
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClass the <code>Class</code> of the bean that the definition is being created for
	 * @param factoryMethodName the name of the method to use to construct the bean instance
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClassName the class name for the bean that the definition is being created for
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
		return rootBeanDefinition(beanClassName, null);
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClassName the class name for the bean that the definition is being created for
	 * @param factoryMethodName the name of the method to use to construct the bean instance
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClassName(beanClassName);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link ChildBeanDefinition}.
	 * @param parentBeanName the name of the parent bean
	 */
	public static BeanDefinitionBuilder childBeanDefinition(String parentBeanName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new ChildBeanDefinition(parentBeanName);
		return builder;
	}


	/**
	 * The <code>BeanDefinition</code> instance we are creating.
	 */
	private AbstractBeanDefinition beanDefinition;

	/**
	 * Our current position with respect to constructor args.
	 */
	private int constructorArgIndex;


	/**
	 * Enforce the use of factory methods.
	 */
	private BeanDefinitionBuilder() {
	}

	/**
	 * Return the current BeanDefinition object in its raw (unvalidated) form.
	 * @see #getBeanDefinition()
	 */
	public AbstractBeanDefinition getRawBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * Validate and return the created BeanDefinition object.
	 */
	public AbstractBeanDefinition getBeanDefinition() {
		this.beanDefinition.validate();
		return this.beanDefinition;
	}


	/**
	 * Add the supplied property value under the given name.
	 */
	public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
		this.beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
		return this;
	}

	/**
	 * Add a reference to the specified bean name under the property specified.
	 * @param name the name of the property to add the reference to
	 * @param beanName the name of the bean being referenced
	 */
	public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
		return addPropertyValue(name, new RuntimeBeanReference(beanName));
	}

	/**
	 * Add an indexed constructor arg value. The current index is tracked internally and all
	 * additions are at the present point.
	 */
	public BeanDefinitionBuilder addConstructorArg(Object value) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(constructorArgIndex++, value);
		return this;
	}

	/**
	 * Add a reference to a named bean as a constructor arg.
	 * @see #addConstructorArg(Object)
	 */
	public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
		return addConstructorArg(new RuntimeBeanReference(beanName));
	}

	/**
	 * Set the name of the factory method to use for this definition.
	 */
	public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	/**
	 * Set the name of the factory bean to use for this definition.
	 */
	public BeanDefinitionBuilder setFactoryBean(String factoryBean, String factoryMethod) {
		this.beanDefinition.setFactoryBeanName(factoryBean);
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	/**
	 * Set the scope of this definition.
	 * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_SINGLETON
	 * @see org.springframework.beans.factory.config.BeanDefinition#SCOPE_PROTOTYPE
	 */
	public BeanDefinitionBuilder setScope(String scope) {
		this.beanDefinition.setScope(scope);
		return this;
	}

	/**
	 * Set whether or not this definition describes a singleton bean,
	 * as alternative to <code>setScope</code>.
	 * @see #setScope
	 */
	public BeanDefinitionBuilder setSingleton(boolean singleton) {
		this.beanDefinition.setSingleton(singleton);
		return this;
	}

	/**
	 * Set whether or not this definition is abstract.
	 */
	public BeanDefinitionBuilder setAbstract(boolean flag) {
		this.beanDefinition.setAbstract(flag);
		return this;
	}

	/**
	 * Set whether beans for this definition should be lazily initialized or not.
	 */
	public BeanDefinitionBuilder setLazyInit(boolean lazy) {
		this.beanDefinition.setLazyInit(lazy);
		return this;
	}

	/**
	 * Set the autowire mode for this definition.
	 */
	public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
		beanDefinition.setAutowireMode(autowireMode);
		return this;
	}

	/**
	 * Set the depency check mode for this definition.
	 */
	public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
		beanDefinition.setDependencyCheck(dependencyCheck);
		return this;
	}

	/**
	 * Set the destroy method for this definition.
	 */
	public BeanDefinitionBuilder setDestroyMethodName(String methodName) {
		beanDefinition.setDestroyMethodName(methodName);
		return this;
	}

	/**
	 * Set the init method for this definition.
	 */
	public BeanDefinitionBuilder setInitMethodName(String methodName) {
		beanDefinition.setInitMethodName(methodName);
		return this;
	}

	/**
	 * Set the description associated with this definition.
	 */
	public BeanDefinitionBuilder setResourceDescription(String resourceDescription) {
		this.beanDefinition.setResourceDescription(resourceDescription);
		return this;
	}

	/**
	 * Append the specified bean name to the list of beans that this definition
	 * depends on.
	 */
	public BeanDefinitionBuilder addDependsOn(String beanName) {
		if (this.beanDefinition.getDependsOn() == null) {
			this.beanDefinition.setDependsOn(new String[] { beanName });
		}
		else {
			String[] added = (String[]) ObjectUtils.addObjectToArray(beanDefinition.getDependsOn(), beanName);
			this.beanDefinition.setDependsOn(added);
		}
		return this;
	}

	/**
	 * Set the source of this definition.
	 * @see AbstractBeanDefinition#setSource
	 */
	public BeanDefinitionBuilder setSource(Object source) {
		this.beanDefinition.setSource(source);
		return this;
	}

	/**
	 * Set the role of this definition.
	 * @see AbstractBeanDefinition#setRole
	 */
	public BeanDefinitionBuilder setRole(int role) {
		this.beanDefinition.setRole(role);
		return this;
	}

}
