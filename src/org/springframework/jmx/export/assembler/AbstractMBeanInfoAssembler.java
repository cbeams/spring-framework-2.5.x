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

package org.springframework.jmx.export.assembler;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

/**
 * Abstract implementation of the <code>MBeanInfoAssembler</code> interface
 * that encapsulates the creation of a <code>ModelMBeanInfo</code> instance
 * but delegates the creation of metadata to sub-classes.
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.jmx.export.assembler.MBeanInfoAssembler
 */
public abstract class AbstractMBeanInfoAssembler implements MBeanInfoAssembler {

	/**
	 * Create an instance of the <code>ModelMBeanInfoSupport</code> class supplied with all
	 * JMX implementations and populates the metadata through calls to the subclass.
	 * @param beanKey the key associated with the managed bean
	 * @param beanClass the Class of the managed resource
	 * @return the populated ModelMBeanInfo instance
	 * @throws JMException in case of errors
	 * @see #getDescription(String, Class)
	 * @see #getAttributeInfo(String, Class)
	 * @see #getConstructorInfo(String, Class)
	 * @see #getOperationInfo(String, Class)
	 * @see #getNotificationInfo(String, Class)
	 * @see #populateMBeanDescriptor(javax.management.Descriptor, String, Class)
	 */
	public ModelMBeanInfo getMBeanInfo(String beanKey, Class beanClass) throws JMException {
		ModelMBeanInfo info = new ModelMBeanInfoSupport(beanClass.getName(), getDescription(beanKey, beanClass),
				getAttributeInfo(beanKey, beanClass), getConstructorInfo(beanKey, beanClass),
				getOperationInfo(beanKey, beanClass), getNotificationInfo(beanKey, beanClass));
		Descriptor desc = info.getMBeanDescriptor();
		populateMBeanDescriptor(desc, beanKey, beanClass);
		info.setMBeanDescriptor(desc);
		return info;
	}

	/**
	 * Get the description of the MBean resource.
	 * <p>Default implementation returns a simple description for the MBean
	 * based on the class name.
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the MBean description
	 * @throws JMException in case of errors
	 */
	protected String getDescription(String beanKey, Class beanClass) throws JMException {
		return beanClass.getName() + " instance";
	}

	/**
	 * Get the attribute metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all the attributes that should
	 * be exposed in the management interface for the managed resource.
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the attribute metadata
	 * @throws JMException in case of errors
	 */
	protected abstract ModelMBeanAttributeInfo[] getAttributeInfo(String beanKey, Class beanClass)
			throws JMException;

	/**
	 * Get the constructor metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all constructors that should
	 * be exposed in the management interface for the managed resource.
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the constructor metadata
	 * @throws JMException in case of errors
	 */
	protected abstract ModelMBeanConstructorInfo[] getConstructorInfo(String beanKey, Class beanClass)
			throws JMException;

	/**
	 * Get the operation metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all operations that should
	 * be exposed in the management interface for the managed resource.
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the operation metadata
	 * @throws JMException in case of errors
	 */
	protected abstract ModelMBeanOperationInfo[] getOperationInfo(String beanKey, Class beanClass)
			throws JMException;

	/**
	 * Get the notification metadata for the MBean resource. Subclasses should implement
	 * this method to return the appropriate metadata for all notifications that should
	 * be exposed in the management interface for the managed resource.
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the notification metadata
	 * @throws JMException in case of errors
	 */
	protected abstract ModelMBeanNotificationInfo[] getNotificationInfo(String beanKey, Class beanClass)
			throws JMException;

	/**
	 * Called after the <code>ModelMBeanInfo</code> instance has been constructed but
	 * before it is passed to the <code>MBeanExporter</code>.
	 * <p>Subclasses can implement this method to add additional descriptors to the
	 * MBean metadata. Default implementation is empty.
	 * @param mbeanDescriptor the <code>Descriptor</code> for the MBean resource.
	 * @param beanKey the key associated with the MBean in the beans map
	 * of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @throws JMException in case of errors
	 */
	protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass)
			throws JMException {
	}

}
