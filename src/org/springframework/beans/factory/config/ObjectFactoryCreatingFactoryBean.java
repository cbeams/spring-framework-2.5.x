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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectFactory;

/**
 * <p>FactoryBean which returns a value which is an ObjectFactory that returns
 * a bean from the BeanFactory. As such, this may be used to avoid having a client
 * bean directly calling getBean() the BeanFactory to get a prototype bean out
 * of the BeanFactory, a violation of inversion of control. Instead, with the use
 * of this class, the client bean can be fed an ObjectFactory as a property which
 * directly returns only the one target (usually prototype) bean.</p>
 * 
 * <p>A Sample config in an XML BeanFactory might look as follows:</p>
 *
 * <pre> 
 * <beans>
 *
 *   &lt;!-- Singleton bean since we have state -->
 *   &lt;bean id="myService" class="a.b.c.MyService" singleton="false">
 *   &lt;/bean> 	
 *   &lt;bean id="myServiceFactory" class="org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean">
 *     &lt;property name="targetName">&lt;idref local="myService"/>&lt;/property>
 *   &lt;/bean> 
 *  
 *   &lt;bean id="clientBean" class="a.b.c.MyClientBean">
 *     &lt;property name="myServiceFactory">&lt;ref local="myServiceFactory"/>&lt;/property>
 *   &lt;/bean>
 *
 * &lt;/beans>
 * </pre>
 *  
 * 
 * @see org.springframework.beans.factory.ObjectFactory
 * 
 * @author Colin Sampaleanu
 * @since 2004-05-11
 * @version $Id: ObjectFactoryCreatingFactoryBean.java,v 1.1 2004-05-12 15:22:21 colins Exp $
 */
public class ObjectFactoryCreatingFactoryBean extends AbstractFactoryBean implements BeanFactoryAware {
	
	private String targetName;
	private BeanFactory beanFactory;
	
	/**
	 * @param targetName The targetName to set.
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
	 */
	protected Object createInstance() throws Exception {
		return new ObjectFactory() {
			public Object getObject() throws BeansException {
				return beanFactory.getBean(targetName);
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return ObjectFactory.class;
	}
}
