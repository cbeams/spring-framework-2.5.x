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
package org.springframework.jmx.assembler;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

/**
 * @author Rob Harrop
 */
public abstract class AbstractModelMBeanInfoAssembler implements ModelMBeanInfoAssembler {

	public ModelMBeanInfo getMBeanInfo(String beanKey, Class beanClass) throws JMException {
		ModelMBeanInfo info = new ModelMBeanInfoSupport(beanClass.getName(), getDescription(beanKey, beanClass),
				getAttributeInfo(beanKey, beanClass), getConstructorInfo(beanKey, beanClass),
				getOperationInfo(beanKey, beanClass), getNotificationInfo(beanKey, beanClass));
		Descriptor desc = info.getMBeanDescriptor();
		populateMBeanDescriptor(desc, beanKey, beanClass);
		info.setMBeanDescriptor(desc);
		return info;
	}

	protected abstract String getDescription(String beanKey, Class beanClass) throws JMException;

	protected abstract ModelMBeanAttributeInfo[] getAttributeInfo(String beanKey, Class beanClass)
			throws JMException;

	protected abstract ModelMBeanConstructorInfo[] getConstructorInfo(String beanKey, Class beanClass)
			throws JMException;

	protected abstract ModelMBeanOperationInfo[] getOperationInfo(String beanKey, Class beanClass)
			throws JMException;

	protected abstract ModelMBeanNotificationInfo[] getNotificationInfo(String beanKey, Class beanClass)
			throws JMException;

	protected abstract void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass)
			throws JMException;

}
