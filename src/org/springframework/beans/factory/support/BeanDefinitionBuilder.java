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

package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.util.ObjectUtils;

/**
 * Programmatic means of constructing {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}
 * using the builder pattern. Intended primarily for use when implementing Spring 2.0
 * {@link org.springframework.beans.factory.xml.NamespaceHandler NamespaceHandlers}.
 * <p>
 * Note that <code>BeanDefinitionBuilder</code> objects support references. A property
 * or constructor argument that is passed another <code>BeanDefinitionBuilder</code> object
 * <emphasis>that has already been registered with a <code>BeanDefinitionRegistryBuilder</code></emphasis>
 * will result in a reference to the bean with that name.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanDefinitionBuilder  {

	/**
	 * The <code>BeanDefinition</code> instance we are creating.
	 */
	private AbstractBeanDefinition beanDefinition;
	
	/**
	 * Our current position with respect to constructor args.
	 */
	private int constructorArgIndex;

	/**
	 * The bean name assigned to the current <code>BeanDefinition</code> if any.
	 */
	private String assignedBeanName;

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClass the <code>Class</code> of the bean the definition is being created for.
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
		return rootBeanDefinition(beanClass, null);
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link RootBeanDefinition}.
	 * @param beanClass the <code>Class</code> of the bean the definition is being created for.
	 * @param factoryMethod the name of the method to use to construct the bean instance.
	 */
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethod) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethod);
		return builder;
	}

	/**
	 * Create a new <code>BeanDefinitionBuilder</code> used to construct a {@link ChildBeanDefinition}.
	 * @param parentBeanName the name of the parent bean.
	 */
	public static BeanDefinitionBuilder childBeanDefinition(String parentBeanName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new ChildBeanDefinition(parentBeanName);
		return builder;
	}

	/**
	 * Protect from public use.
	 */
	protected BeanDefinitionBuilder() {
	}

	/**
	 * Validate and return the created {@link org.springframework.beans.factory.config.BeanDefinition}.
	 */
	public AbstractBeanDefinition getBeanDefinition() {
		this.beanDefinition.validate();
		return this.beanDefinition;
	}

	/**
	 * Adds the supplied property value under the given name.
	 */
	public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
		if(this.beanDefinition.getPropertyValues() == null) {
			this.beanDefinition.setPropertyValues(new MutablePropertyValues());
		}
		this.beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
		return this;
	}

	/**
	 * Adds a reference to the specified bean name under the property specified.
	 * @param name the name of the property to add the reference to.
	 * @param bean the name of the bean being referenced.
	 */
	public BeanDefinitionBuilder addPropertyReference(String name, String bean) {
		return this.addPropertyValue(name, new RuntimeBeanReference(bean));
	}

	/**
	 * Adds a reference to the bean identified by the supplied <code>BeanDefinitionBuilder</code> to the
	 * specified property.
	 * @param name the name of tye 
	 * @param target
	 */
	public BeanDefinitionBuilder addPropertyReference(String name, BeanDefinitionBuilder target) {
		return addPropertyReference(name, getTargetBeanName(target));
	}

	/**
	 * Constructor args are indexed. All additions are at the present point
	 */
	public BeanDefinitionBuilder addConstructorArg(Object value) {
		if(this.beanDefinition.getConstructorArgumentValues() == null) {
			this.beanDefinition.setConstructorArgumentValues(new ConstructorArgumentValues());
		}

		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(constructorArgIndex++, value);
		return this;
	}

	public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
		return addConstructorArg(new RuntimeBeanReference(beanName));
	}

	public BeanDefinitionBuilder addConstructorArgReference(BeanDefinitionBuilder target) {
		return addConstructorArgReference(getTargetBeanName(target));
	}


	public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	public BeanDefinitionBuilder setFactoryBean(String factoryBean, String factoryMethod) {
		this.beanDefinition.setFactoryBeanName(factoryBean);
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}


	public BeanDefinitionBuilder setSingleton(boolean singleton) {
		this.beanDefinition.setSingleton(singleton);
		return this;
	}

	public BeanDefinitionBuilder setAbstract(boolean flag) {
		this.beanDefinition.setAbstract(flag);
		return this;
	}

	public BeanDefinitionBuilder setLazyInit(boolean lazy) {
		this.beanDefinition.setLazyInit(lazy);
		return this;
	}

	public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
		beanDefinition.setAutowireMode(autowireMode);
		return this;
	}
	
	public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
		beanDefinition.setDependencyCheck(dependencyCheck);
		return this;
	}
	
	public BeanDefinitionBuilder setDestroyMethodName(String methodName) {
		beanDefinition.setDestroyMethodName(methodName);
		return this;
	}
	
	public BeanDefinitionBuilder setInitMethodName(String methodName) {
		beanDefinition.setInitMethodName(methodName);
		return this;
	}
	
	
	public BeanDefinitionBuilder setResourceDescription(String resourceDescription) {
		beanDefinition.setResourceDescription(resourceDescription);
		return this;
	}
	
	
	public BeanDefinitionBuilder addDependsOn(String beanName) {
		if (beanDefinition.getDependsOn() == null) {
			beanDefinition.setDependsOn(new String[] { beanName });
		}
		else {
			String[] added = (String[]) ObjectUtils.addObjectToArray(beanDefinition.getDependsOn(), beanName);
			beanDefinition.setDependsOn(added);
		}
		return this;
	}


	/**
	 * Called by BeanDefinitionRegistryBuilder to let this object know the name of the
	 * bean definition. Allows reference setting
	 * @param name bean name that has been assigned for this object
	 */
	protected void assignBeanName(String name) {
		if (this.assignedBeanName != null) {
			throw new IllegalStateException("Cannot assign bean name: bean name of '" + assignedBeanName + "' already assigned");
		}
		this.assignedBeanName = name;
	}

	/**
	 * Returns the bean name assigned to the supplied <code>BeanDefinitionBuilder</code>.
	 * @throws IllegalArgumentException if the <code>BeanDefinitionBuilder</code> has no assigned bean name.
	 */
	private String getTargetBeanName(BeanDefinitionBuilder target) throws IllegalArgumentException {
		String assignedBeanName = target.assignedBeanName;
		if(assignedBeanName == null) {
			throw new IllegalArgumentException("Cannot add a reference to the bean identified by the " +
					"supplied BeanDefinitonBuilder. No bean name has been assigned.");
		}
		return assignedBeanName;
	}
}
