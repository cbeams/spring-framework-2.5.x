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
 * Programmatic means of constructor BeanDefinitions using the builder
 * pattern. Intended primarily for use when implementing Spring 2.0
 * NamespaceHandlers.
 * <p>
 * Note that BeanDefinitionBuilder objects support references. A property
 * or constructor argument that is passed another BeanDefinitionBuilder object
 * <i>that has already been registered with a BeanDefinitionRegistryBuilder</i>
 * will result in a reference to the bean with that name.
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanDefinitionBuilder  {

	/**
	 * BeanDefinition we are creating
	 */
	private AbstractBeanDefinition beanDefinition;
	
	/**
	 * OIur current position with respect to constructor args
	 */
	private int constructorArgIndex;
	
	private String assignedBeanName;


	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
		return rootBeanDefinition(beanClass, null);
	}
	
//	public static BeanDefinitionBuilder rootBeanDefinition(String className) {
//		return rootBeanDefinition(className, null);
//	}
	
	//public static BeanDefinitionBuilder rootBeanDefinition(String className, String factoryMethod) {
		
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethod) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethod);
		return builder;
	}
	
	public static BeanDefinitionBuilder childBeanDefinition(String parentBeanName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new ChildBeanDefinition(parentBeanName);
		return builder;
	}

	
	/**
	 * Protect from public use
	 *
	 */
	protected BeanDefinitionBuilder() {
	}

	/**
	 * Return the created bean definition
	 */
	public AbstractBeanDefinition getBeanDefinition() {
		beanDefinition.validate();
		return beanDefinition;
	}
	

	public BeanDefinitionBuilder factoryMethod(String factoryMethod) {
		beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	public BeanDefinitionBuilder factoryBean(String factoryBean, String factoryMethod) {
		beanDefinition.setFactoryBeanName(factoryBean);
		beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}

	
	public BeanDefinitionBuilder singleton(boolean singleton) {
		beanDefinition.setSingleton(singleton);
		return this;
	}
	
	public BeanDefinitionBuilder setAbstract(boolean flag) {
		beanDefinition.setAbstract(flag);
		return this;
	}
	
	public BeanDefinitionBuilder lazyInit(boolean lazy) {
		beanDefinition.setLazyInit(lazy);
		return this;
	}

	/**
	 * Add a property value
	 * @param name property name
	 * @param value property value. May be a simple type, or may be a
	 * BeanDefinitionBuilder object, in which case if the name has been assigned,
	 * a runtime bean reference is created. If you want to make a reference
	 * to a named bean, use addPropertyValueToNamedBean
	 * @return this object
	 */
	public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
		value = resolveReferenceValue(value);
		if(this.beanDefinition.getPropertyValues() == null) {
			this.beanDefinition.setPropertyValues(new MutablePropertyValues());
		}
		this.beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
		return this;
	}
	
	public BeanDefinitionBuilder addPropertyReferenceToNamedBean(String name, String bean) {
		return this.addPropertyValue(name, new RuntimeBeanReference(bean));
	}
	
	protected Object resolveReferenceValue(Object value) {
		if (value instanceof BeanDefinitionBuilder) {
			BeanDefinitionBuilder target = (BeanDefinitionBuilder) value;
			if (target.assignedBeanName == null) {
				throw new IllegalStateException("Cannot support references: not yet assigned an identity");
			}
			return new RuntimeBeanReference(target.assignedBeanName);
		}
		else {
			return value;
		}
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
	 * Constructor args are indexed. All additions are at the present point
	 * @param name
	 * @param value
	 * @return
	 */
	public BeanDefinitionBuilder addConstructorArg(Object value) {
		value = resolveReferenceValue(value);
		if(this.beanDefinition.getConstructorArgumentValues() == null) {
			this.beanDefinition.setConstructorArgumentValues(new ConstructorArgumentValues());
		}

		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(constructorArgIndex++, value);
		return this;
	}
	
	public BeanDefinitionBuilder addConstructorReferenceToNamedBean(String beanName) {
		return addConstructorArg(new RuntimeBeanReference(beanName));
	}


	public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
		// TODO validate, or in AstractBeanDefinition
		beanDefinition.setAutowireMode(autowireMode);
		return this;
	}
	
	public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
		// TODO validate, or in AstractBeanDefinition
		beanDefinition.setDependencyCheck(dependencyCheck);
		return this;
	}
	
	public BeanDefinitionBuilder destroyMethodName(String methodName) {
		beanDefinition.setDestroyMethodName(methodName);
		return this;
	}
	
	public BeanDefinitionBuilder initMethodName(String methodName) {
		beanDefinition.setInitMethodName(methodName);
		return this;
	}
	
	
	public BeanDefinitionBuilder resourceDescription(String resourceDescription) {
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

}
