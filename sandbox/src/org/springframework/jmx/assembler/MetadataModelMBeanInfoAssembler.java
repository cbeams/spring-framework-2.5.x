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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;

import org.springframework.beans.BeanUtils;
import org.springframework.jmx.metadata.CommonsJmxAttributeSource;
import org.springframework.jmx.metadata.InvalidMetadataException;
import org.springframework.jmx.metadata.JmxAttributeSource;
import org.springframework.jmx.metadata.ManagedAttribute;
import org.springframework.jmx.metadata.ManagedOperation;
import org.springframework.jmx.metadata.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * Implementation of <tt>ModelMBeanInfoAssembler</tt> that reads the
 * management interface information from source level metadata. Uses Spring's
 * metadata abstraction layer so that metadata can be read using any supported
 * implementation.
 *
 * @author Rob Harrop
 */
public class MetadataModelMBeanInfoAssembler extends
		AbstractReflectionBasedModelMBeanInfoAssembler implements
		AutodetectCapableModelMBeanInfoAssembler {

	/**
	 * Key for <code>log</code> descriptor.
	 */
	private static final String LOG = "log";

	/**
	 * Key for <code>logFile</code> descriptor.
	 */
	private static final String LOG_FILE = "logFile";

	/**
	 * Key for <code>currencyTimeLimit</code> descriptor.
	 */
	private static final String CURRENCY_TIME_LIMIT = "currencyTimeLimit";

	/**
	 * Key for <code>default</code> descriptor.
	 */
	private static final String DEFAULT = "default";

	/**
	 * Key for <code>persistPolicy</code> descriptor.
	 */
	private static final String PERSIST_POLICY = "persistPolicy";

	/**
	 * Key for <code>persistPeriod</code> descriptor.
	 */
	private static final String PERSIST_PERIOD = "persistPeriod";

	/**
	 * Key for <code>persistLocation</code> descriptor.
	 */
	private static final String PERSIST_LOCATION = "persistLocation";

	/**
	 * Key for <code>persistName</code> descriptor.
	 */
	private static final String PERSIST_NAME = "persistName";


	/**
	 * The <code>JmxAttributeSource</code> implementation used to read
	 * the metadata from the bean class.
	 */
	private JmxAttributeSource attributeSource = new CommonsJmxAttributeSource();


	/**
	 * Sets the <code>JmxAttributeSource</code> used to read the
	 * metadata from the bean class.
	 *
	 * @param attributeSource the <code>JmxAttributeSource</code>.
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		this.attributeSource = attributeSource;
	}

	/**
	 * Votes on the inclusion of an attribute accessor.
	 *
	 * @param method the accessor <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> has the appropriate metadata.
	 *         Otherwise <code>false</code>.
	 */
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * Votes on the inclusion of an attribute mutator.
	 *
	 * @param method the mutator <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> has the appropriate metadata.
	 *         Otherwise <code>false</code>.
	 */
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * Votes on the inclusion of an operation.
	 *
	 * @param method the operation <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> has the appropriate metadata.
	 *         Otherwise <code>false</code>.
	 */
	protected boolean includeOperation(Method method, String beanKey) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			return hasManagedAttribute(method);
		}
		else {
			return hasManagedOperation(method);
		}
	}

	/**
	 * Retreives the description for the supplied <code>Method</code> from the
	 * metadata. Uses the method name is no description is present in the
	 * metadata.
	 *
	 * @param method the operation <code>Method</code>.
	 * @return the description of the operation.
	 */
	protected String getOperationDescription(Method method) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			ManagedAttribute ma = attributeSource.getManagedAttribute(method);
			if (ma == null) {
				return method.getName();
			}
			else {
				return ma.getDescription();
			}
		}
		else {
			ManagedOperation mo = attributeSource.getManagedOperation(method);
			if (mo == null) {
				return method.getName();
			}
			else {
				return mo.getDescription();
			}
		}
	}

	/**
	 * Creates a description for the attribute corresponding to this property
	 * descriptor. Attempts to create the description using metadata from either
	 * the getter or setter attributes, otherwise uses the property name.
	 *
	 * @param propertyDescriptor the <code>PropertyDescriptor</code> for the attribute.
	 * @return the attribute description.
	 */
	protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
		Method readMethod = propertyDescriptor.getReadMethod();
		Method writeMethod = propertyDescriptor.getWriteMethod();

		ManagedAttribute getter = (readMethod != null) ? attributeSource.getManagedAttribute(readMethod) : null;
		ManagedAttribute setter = (writeMethod != null) ? attributeSource.getManagedAttribute(writeMethod) : null;

		if (getter != null && StringUtils.hasText(getter.getDescription())) {
			return getter.getDescription();
		}
		else if (setter != null && StringUtils.hasText(setter.getDescription())) {
			return setter.getDescription();
		}
		else {
			return propertyDescriptor.getDisplayName();
		}
	}

	/**
	 * Attempts to read managed resource description from the source level metadata.
	 * Returns an empty <code>String</code> if no description can be found.
	 *
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @param beanClass the <code>Class</code> of the managed resource.
	 * @return the description of the managed resource.
	 */
	protected String getDescription(String beanKey, Class beanClass) {
		ManagedResource mr = attributeSource.getManagedResource(beanClass);
		return (mr != null ? mr.getDescription() : "");
	}

	/**
	 * Adds descriptor fields from the <code>ManagedResource</code> attribute
	 * to the MBean descriptor. Specifically, adds the <code>currencyTimeLimit</code>,
	 * <code>persistPolicy</code>, <code>persistPeriod</code>, <code>persistLocation</code>
	 * and <code>persistName</code> descriptor fields if they are present in the metdata.
	 *
	 * @param mbeanDescriptor the <code>Descriptor</code> for the MBean.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @param beanClass the <code>Class</code> of the managed resource.
	 */
	protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass) {
		ManagedResource mr = attributeSource.getManagedResource(beanClass);
		if (mr == null) {
			throw new InvalidMetadataException("No ManagedResource attribute found for class: " + beanClass.getName());
		}
		mbeanDescriptor.setField(LOG, mr.isLog() ? "true" : "false");
		if (mr.getLogFile() != null) {
			mbeanDescriptor.setField(LOG_FILE, mr.getLogFile());
		}
		mbeanDescriptor.setField(CURRENCY_TIME_LIMIT, Integer.toString(mr.getCurrencyTimeLimit()));
		mbeanDescriptor.setField(PERSIST_POLICY, mr.getPersistPolicy());
		mbeanDescriptor.setField(PERSIST_PERIOD, Integer.toString(mr.getPersistPeriod()));
		mbeanDescriptor.setField(PERSIST_LOCATION, mr.getPersistLocation());
		mbeanDescriptor.setField(PERSIST_NAME, mr.getPersistName());
	}

	/**
	 * Adds descriptor fields from the <code>ManagedAttribute</code> attribute
	 * to the attribute descriptor. Specifically, adds the <code>currencyTimeLimit</code>,
	 * <code>default</code>, <code>persistPolicy</code> and <code>persistPeriod</code>
	 * descriptor fields if they are present in the metdata.
	 *
	 * @param descriptor the <code>Descriptor</code> for the MBean.
	 * @param getter the accessor <code>Method</code> for the attribute.
	 * @param setter the mutator <code>Method</code> for the attribute.
	 */
	protected void populateAttributeDescriptor(Descriptor descriptor, Method getter, Method setter) {
		ManagedAttribute gma = (getter == null) ? ManagedAttribute.EMPTY : attributeSource.getManagedAttribute(getter);
		ManagedAttribute sma = (setter == null) ? ManagedAttribute.EMPTY : attributeSource.getManagedAttribute(setter);

		int ctl = resolveIntDescriptor(gma.getCurrencyTimeLimit(), sma.getCurrencyTimeLimit());
		descriptor.setField(CURRENCY_TIME_LIMIT, Integer.toString(ctl));

		Object defaultValue = resolveObjectDescriptor(gma.getDefaultValue(), sma.getDefaultValue());
		descriptor.setField(DEFAULT, defaultValue);

		String persistPolicy = resolveStringDescriptor(gma.getPersistPolicy(), sma.getPersistPolicy(), "Never");
		descriptor.setField(PERSIST_POLICY, persistPolicy);

		int persistPeriod = resolveIntDescriptor(gma.getPersistPeriod(), sma.getPersistPeriod());
		descriptor.setField(PERSIST_PERIOD, Integer.toString(persistPeriod));
	}

	/**
	 * Adds descriptor fields from the <code>ManagedAttribute</code> attribute
	 * to the attribute descriptor. Specifically, adds the <code>currencyTimeLimit</code>
	 * descriptor field if it is present in the metdata.
	 *
	 * @param descriptor the <code>Descriptor</code> for the MBean.
	 * @param method the corresponding <code>Method</code> for the operation.
	 */
	protected void populateOperationDescriptor(Descriptor descriptor, Method method) {
		ManagedOperation mo = attributeSource.getManagedOperation(method);
		if (mo != null) {
			descriptor.setField(CURRENCY_TIME_LIMIT, Integer.toString(mo.getCurrencyTimeLimit()));
		}
	}


	/**
	 * Determines which of two <code>int</code> values
	 * should be used as the value for an attribute descriptor.
	 * In general only the getter or the setter will be have a non-zero
	 * value so we use that value. In the event that both values
	 * are non-zero we use the greater of the two. This method can be used
	 * to resolve any <code>int</code> valued descriptor where there are two
	 * possible values.
	 *
	 * @param getter the <code>int</code> value associated with the getter for this attribute.
	 * @param setter the <code>int</code> value associated with the setter for this attribute.
	 */
	private int resolveIntDescriptor(int getter, int setter) {
		if (getter == 0 && setter != 0) {
			return setter;
		}
		else if (setter == 0 && getter != 0) {
			return getter;
		}
		else {
			return (getter >= setter) ? getter : setter;
		}
	}

	/**
	 * Locates the value of a descriptor based on values attached
	 * to both the getter and setter methods. If both have values
	 * supplied then the value attached to the getter is preferred.
	 *
	 * @param getter the <code>Object</code> value associated with the get method.
	 * @param setter the <code>Object</code> value associated with the set method.
	 * @return The appropriate <code>Object</code> to use as the value for the descriptor.
	 */
	private Object resolveObjectDescriptor(Object getter, Object setter) {
		if (getter != null) {
			return getter;
		}
		else if (setter != null) {
			return setter;
		}
		else {
			return null;
		}
	}

	/**
	 * Locates the value of a descriptor based on values attached
	 * to both the getter and setter methods. If both have values
	 * supplied then the value attached to the getter is preferred.
	 * The supplied default value is used to check to see if the value
	 * associated with the getter has changed from the default.
	 *
	 * @param getter the <code>String</code> value associated with the get method.
	 * @param setter the <code>String</code> value associated with the set method.
	 * @param defaultValue the <code>String</code> valued default associated with this descriptor.
	 * @return The appropriate <code>String</code> to use as the value for the descriptor.
	 */
	private String resolveStringDescriptor(String getter, String setter, String defaultValue) {
		if (getter != null && !defaultValue.equals(getter)) {
			return getter;
		}
		else if (setter != null) {
			return setter;
		}
		else {
			return null;
		}
	}

	/**
	 * Checks to see if a <code>Method</code> has the <code>ManagedAttribute</code> attribute.
	 * @param method the <code>Method</code> to check.
	 * @return <code>true</code> if the attribute is present, otherwise <code>false</code>.
	 */
	private boolean hasManagedAttribute(Method method) {
		ManagedAttribute ma = attributeSource.getManagedAttribute(method);
		return (ma != null);
	}

	/**
	 * Checks to see if a <code>Method</code> has the <code>ManagedOperation</code> attribute.
	 * @param method the <code>Method</code> to check.
	 * @return <code>true</code> if the attribute is present, otherwise <code>false</code>.
	 */
	private boolean hasManagedOperation(Method method) {
		ManagedOperation mo = attributeSource.getManagedOperation(method);
		return (mo != null);
	}

	/**
	 * Used for auto detection of beans. Checks to see if the bean's class has a
	 * <code>ManagedResource</code> attribute. If so it will add it list of included beans.
	 * @param beanName the name of the bean in the <code>BeanFactory</code>.
	 * @param beanClass the <code>Class</code> of the managed resource.
	 * @return <code>true</code> if the bean class has the <code>ManagedResource</code> attribute.
	 * Otherwise <code>false</code>.
	 */
	public boolean includeBean(String beanName, Class beanClass) {
		return (attributeSource.getManagedResource(beanClass) != null);
	}

}
