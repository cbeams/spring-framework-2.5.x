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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedAttribute;
import org.springframework.jmx.export.metadata.ManagedOperation;
import org.springframework.jmx.export.metadata.ManagedOperationParameter;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * Implementation of <tt>MBeanInfoAssembler</tt> that reads the
 * management interface information from source level metadata.
 *
 * <p>Uses Spring's metadata abstraction layer so that metadata can
 * be read using any supported implementation.
 *
 * @author Rob Harrop
 * @since 1.2
 * @see #setAttributeSource
 */
public class MetadataMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler
		implements AutodetectCapableMBeanInfoAssembler, InitializingBean {

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
	private JmxAttributeSource attributeSource;


	/**
	 * Set the <code>JmxAttributeSource</code> used to read the
	 * metadata from the bean class.
	 * @param attributeSource the <code>JmxAttributeSource</code>.
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		this.attributeSource = attributeSource;
	}

	public void afterPropertiesSet() {
		if (this.attributeSource == null) {
			throw new IllegalArgumentException("'attributeSource' is required");
		}
	}


	/**
	 * Vote on the inclusion of an attribute accessor.
	 * @param method the accessor method
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return whether the method has the appropriate metadata
	 */
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * Votes on the inclusion of an attribute mutator.
	 * @param method the mutator method
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return whether the method has the appropriate metadata
	 */
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * Votes on the inclusion of an operation.
	 * @param method the operation method
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return whether the method has the appropriate metadata
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
	 * Retrieve the description for the supplied <code>Method</code> from the
	 * metadata. Uses the method name is no description is present in the metadata.
	 * @param method the operation method
	 * @return the description of the operation
	 */
	protected String getOperationDescription(Method method) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			ManagedAttribute ma = this.attributeSource.getManagedAttribute(method);
			if (ma != null && StringUtils.hasText(ma.getDescription())) {
				return ma.getDescription();
			}
			return method.getName();
		}
		else {
			ManagedOperation mo = this.attributeSource.getManagedOperation(method);
			if (mo != null && StringUtils.hasText(mo.getDescription())) {
				return mo.getDescription();
			}
			return method.getName();
		}
	}

	/**
	 * Create a description for the attribute corresponding to this property
	 * descriptor. Attempts to create the description using metadata from either
	 * the getter or setter attributes, otherwise uses the property name.
	 * @param propertyDescriptor the PropertyDescriptor for the attribute
	 * @return the attribute description
	 */
	protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
		Method readMethod = propertyDescriptor.getReadMethod();
		Method writeMethod = propertyDescriptor.getWriteMethod();

		ManagedAttribute getter =
				(readMethod != null) ? this.attributeSource.getManagedAttribute(readMethod) : null;
		ManagedAttribute setter =
				(writeMethod != null) ? this.attributeSource.getManagedAttribute(writeMethod) : null;

		if (getter != null && StringUtils.hasText(getter.getDescription())) {
			return getter.getDescription();
		}
		else if (setter != null && StringUtils.hasText(setter.getDescription())) {
			return setter.getDescription();
		}
		return propertyDescriptor.getDisplayName();
	}

	/**
	 * Read managed resource description from the source level metadata.
	 * Returns an empty <code>String</code> if no description can be found.
	 * @param beanKey the key associated with the MBean in the beans map
	 * @param beanClass the class of the managed resource
	 * @return the description of the managed resource
	 */
	protected String getDescription(String beanKey, Class beanClass) {
		ManagedResource mr = this.attributeSource.getManagedResource(beanClass);
		return (mr != null ? mr.getDescription() : "");
	}

	/**
	 * Adds descriptor fields from the <code>ManagedResource</code> attribute
	 * to the MBean descriptor. Specifically, adds the <code>currencyTimeLimit</code>,
	 * <code>persistPolicy</code>, <code>persistPeriod</code>, <code>persistLocation</code>
	 * and <code>persistName</code> descriptor fields if they are present in the metdata.
	 * @param mbeanDescriptor the <code>Descriptor</code> for the MBean
	 * @param beanKey the key associated with the MBean in the beans map
	 * @param beanClass the <code>Class</code> of the managed resource
	 */
	protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass) {
		ManagedResource mr = this.attributeSource.getManagedResource(beanClass);
		if (mr == null) {
			throw new InvalidMetadataException(
					"No ManagedResource attribute found for class: " + beanClass.getName());
		}
		if (mr.getCurrencyTimeLimit() > 0) {
			mbeanDescriptor.setField(CURRENCY_TIME_LIMIT, Integer.toString(mr.getCurrencyTimeLimit()));
		}
		mbeanDescriptor.setField(LOG, mr.isLog() ? "true" : "false");
		if (mr.getLogFile() != null) {
			mbeanDescriptor.setField(LOG_FILE, mr.getLogFile());
		}
		mbeanDescriptor.setField(PERSIST_POLICY, mr.getPersistPolicy());
		mbeanDescriptor.setField(PERSIST_PERIOD, Integer.toString(mr.getPersistPeriod()));
		mbeanDescriptor.setField(PERSIST_NAME, mr.getPersistName());
		mbeanDescriptor.setField(PERSIST_LOCATION, mr.getPersistLocation());
	}

	/**
	 * Add descriptor fields from the <code>ManagedAttribute</code> attribute
	 * to the attribute descriptor. Specifically, adds the <code>currencyTimeLimit</code>,
	 * <code>default</code>, <code>persistPolicy</code> and <code>persistPeriod</code>
	 * descriptor fields if they are present in the metdata.
	 * @param descriptor the descriptor for the MBean
	 * @param getter the accessor method for the attribute
	 * @param setter the mutator method for the attribute
	 */
	protected void populateAttributeDescriptor(Descriptor descriptor, Method getter, Method setter) {
		ManagedAttribute gma =
				(getter == null) ? ManagedAttribute.EMPTY : this.attributeSource.getManagedAttribute(getter);
		ManagedAttribute sma =
				(setter == null) ? ManagedAttribute.EMPTY : this.attributeSource.getManagedAttribute(setter);

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
	 * Add descriptor fields from the <code>ManagedAttribute</code> attribute
	 * to the attribute descriptor. Specifically, adds the <code>currencyTimeLimit</code>
	 * descriptor field if it is present in the metdata.
	 * @param descriptor the descriptor for the MBean
	 * @param method the corresponding method for the operation
	 */
	protected void populateOperationDescriptor(Descriptor descriptor, Method method) {
		ManagedOperation mo = this.attributeSource.getManagedOperation(method);
		if (mo != null) {
			descriptor.setField(CURRENCY_TIME_LIMIT, Integer.toString(mo.getCurrencyTimeLimit()));
		}
	}

	/**
	 * Reads <code>MBeanParameterInfo</code> from the <code>ManagedOperationParameter</code>
	 * attributes attached to a method. Returns an empty array of <code>MBeanParameterInfo</code> if no
	 * attributes are found.
	 * @param method the <code>Method</code> to get the <code>ManagedOperationParameter</code> for.
	 * @return the <code>MBeanParameterInfo</code> array.
	 */
	protected MBeanParameterInfo[] getOperationParameters(Method method) {
		ManagedOperationParameter[] params = this.attributeSource.getManagedOperationParameters(method);
		if (params == null || params.length == 0) {
			return new MBeanParameterInfo[0];
		}

		MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[params.length];
		Class[] methodParameters = method.getParameterTypes();

		for (int i = 0; i < params.length; i++) {
			ManagedOperationParameter param = params[i];
			parameterInfo[i] =
					new MBeanParameterInfo(param.getName(), methodParameters[i].getName(), param.getDescription());
		}

		return parameterInfo;
	}

	/**
	 * Determines which of two <code>int</code> values should be used as the value
	 * for an attribute descriptor. In general, only the getter or the setter will
	 * be have a non-zero value so we use that value. In the event that both values
	 * are non-zero we use the greater of the two. This method can be used to resolve
	 * any <code>int</code> valued descriptor where there are two possible values.
	 * @param getter the int value associated with the getter for this attribute.
	 * @param setter the int associated with the setter for this attribute.
	 */
	private int resolveIntDescriptor(int getter, int setter) {
		if (getter == 0 && setter != 0) {
			return setter;
		}
		else if (setter == 0 && getter != 0) {
			return getter;
		}
		return (getter >= setter) ? getter : setter;
	}

	/**
	 * Locates the value of a descriptor based on values attached
	 * to both the getter and setter methods. If both have values
	 * supplied then the value attached to the getter is preferred.
	 * @param getter the Object value associated with the get method.
	 * @param setter the Object value associated with the set method.
	 * @return the appropriate Object to use as the value for the descriptor
	 */
	private Object resolveObjectDescriptor(Object getter, Object setter) {
		if (getter != null) {
			return getter;
		}
		else if (setter != null) {
			return setter;
		}
		return null;
	}

	/**
	 * Locates the value of a descriptor based on values attached
	 * to both the getter and setter methods. If both have values
	 * supplied then the value attached to the getter is preferred.
	 * The supplied default value is used to check to see if the value
	 * associated with the getter has changed from the default.
	 * @param getter the String value associated with the get method
	 * @param setter the String value associated with the set method
	 * @param defaultValue the String value default associated with this descriptor
	 * @return the appropriate String to use as the value for the descriptor
	 */
	private String resolveStringDescriptor(String getter, String setter, String defaultValue) {
		if (getter != null && !defaultValue.equals(getter)) {
			return getter;
		}
		else if (setter != null) {
			return setter;
		}
		return null;
	}

	/**
	 * Checks to see if a <code>Method</code> has the <code>ManagedAttribute</code> attribute.
	 * @param method the method to check
	 */
	private boolean hasManagedAttribute(Method method) {
		ManagedAttribute ma = this.attributeSource.getManagedAttribute(method);
		return (ma != null);
	}

	/**
	 * Checks to see if a <code>Method</code> has the <code>ManagedOperation</code> attribute.
	 * @param method the method to check
	 */
	private boolean hasManagedOperation(Method method) {
		ManagedOperation mo = this.attributeSource.getManagedOperation(method);
		return (mo != null);
	}

	/**
	 * Used for autodetection of beans. Checks to see if the bean's class has a
	 * <code>ManagedResource</code> attribute. If so it will add it list of included beans.
	 * @param beanName the name of the bean in the bean factory
	 * @param beanClass the class of the bean
	 */
	public boolean includeBean(String beanName, Class beanClass) {
		ManagedResource mr = this.attributeSource.getManagedResource(beanClass);
		return (mr != null);
	}

}
