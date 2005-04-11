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
import java.util.ArrayList;
import java.util.List;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanUtils;
import org.springframework.jmx.support.JmxUtils;

/**
 * Extends the <code>AbstractMBeanInfoAssembler</code> to add a basic
 * algorithm for building metadata based on the reflective metadata of
 * the MBean class.
 * <p/>
 * <p>The logic for creating MBean metadata from the reflective metadata is contained
 * in this class, but this class makes no desicions as to which methods and
 * properties are to be exposed. Instead it gives subclasses a chance to 'vote'
 * on each property or method through the <code>includeXXX</code> methods.
 * <p/>
 * <p>Subclasses are also given the opportunity to populate attribute and operation
 * metadata with additional descriptors once the metadata is assembled through the
 * <code>populateXXXDescriptor</code> methods.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see #includeOperation
 * @see #includeReadAttribute
 * @see #includeWriteAttribute
 * @see #populateAttributeDescriptor
 * @see #populateOperationDescriptor
 * @since 1.2
 */
public abstract class AbstractReflectiveMBeanInfoAssembler extends AbstractMBeanInfoAssembler {

	/**
	 * Key for the visibility descriptor field.
	 */
	private static final String VISIBILITY = "visibility";

	/**
	 * Lowest visibility. Used for operations that correspond to accessors or mutators
	 * for attributes.
	 */
	private static final Integer ATTRIBUTE_OPERATION_VISIBILITY = new Integer(4);

	/**
	 * Key for the role descriptor field.
	 */
	private static final String ROLE = "role";

	/**
	 * Role for attribute accessors.
	 */
	private static final String GETTER = "getter";

	/**
	 * Role for attribute mutators.
	 */
	private static final String SETTER = "setter";

	/**
	 * Role for operations.
	 */
	private static final String OPERATION = "operation";

	/**
	 * Indicates whether or not strict casing is being used for attributes.
	 */
  private boolean useStrictCasing = true;

	/**
	 * Enables and disables strict casing for attributes. When using strict casing a JavaBean property
	 * with a getter such as <code>getFoo()</code> translates to an attribute called <code>Foo</code>.
	 * With strict casing disable <code>getFoo()</code> would translate to just <code>foo</code>.
	 */
	public void setUseStrictCasing(boolean useStrictCasing) {
		this.useStrictCasing = useStrictCasing;
	}

	/**
	 * Iterate through all properties on the MBean class and gives subclasses the chance to vote on the
	 * inclusion of both the accessor and mutator. If a particular accessor or mutator is voted for
	 * inclusion the appropriate metadata is assembled and passed to the subclass for descriptor population.
	 *
	 * @param beanKey   the key associated with the MBean in the <code>beans</code> <code>Map</code>
	 *                  of the <code>MBeanExporter</code>.
	 * @param beanClass the <code>Class</code> of the MBean.
	 * @return the attribute metadata.
	 * @see #populateAttributeDescriptor(javax.management.Descriptor, java.lang.reflect.Method, java.lang.reflect.Method)
	 */
	protected ModelMBeanAttributeInfo[] getAttributeInfo(String beanKey, Class beanClass) throws JMException {
		PropertyDescriptor[] props = BeanUtils.getPropertyDescriptors(beanClass);
		List infos = new ArrayList();

		for (int i = 0; i < props.length; i++) {
			Method getter = props[i].getReadMethod();
			if (getter != null && getter.getDeclaringClass() == Object.class) {
				continue;
			}
			if (getter != null && !includeReadAttribute(getter, beanKey)) {
				getter = null;
			}

			Method setter = props[i].getWriteMethod();
			if (setter != null && !includeWriteAttribute(setter, beanKey)) {
				setter = null;
			}

			if (getter != null || setter != null) {
				// If both getter and setter are null, then this does not need exposing.
				String attributeName = JmxUtils.getAttributeName(props[i], this.useStrictCasing);
				ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo(attributeName, getAttributeDescription(props[i]), getter, setter);

				Descriptor desc = info.getDescriptor();
				if (getter != null) {
					desc.setField("getMethod", getter.getName());
				}
				if (setter != null) {
					desc.setField("setMethod", setter.getName());
				}

				populateAttributeDescriptor(desc, getter, setter);
				info.setDescriptor(desc);
				infos.add(info);
			}
		}

		return (ModelMBeanAttributeInfo[]) infos.toArray(new ModelMBeanAttributeInfo[infos.size()]);
	}

	/**
	 * Iterate through all methods on the MBean class and gives subclasses the chance
	 * to vote on their inclusion. If a particular method corresponds to the accessor
	 * or mutator of an attribute that is inclued in the managment interface, then
	 * the corresponding operation is exposed with the &quot;role&quot; descriptor
	 * field set to the appropriate value.
	 *
	 * @param beanKey   the key associated with the MBean in the beans map
	 *                  of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the operation metadata
	 * @see #populateOperationDescriptor
	 */
	protected ModelMBeanOperationInfo[] getOperationInfo(String beanKey, Class beanClass) {
		Method[] methods = beanClass.getMethods();
		List infos = new ArrayList();

		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			ModelMBeanOperationInfo info = null;

			if (method.getDeclaringClass() == Object.class) {
				continue;
			}

			PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
			if (pd != null) {
				if ((method.equals(pd.getReadMethod()) && includeReadAttribute(method, beanKey)) ||
						(method.equals(pd.getWriteMethod()) && includeWriteAttribute(method, beanKey))) {
					// Attributes need to have their methods exposed as
					// operations to the JMX server as well.
					info = createModelMBeanOperationInfo(method, pd.getName(), true);
					Descriptor desc = info.getDescriptor();
					desc.setField(VISIBILITY, ATTRIBUTE_OPERATION_VISIBILITY);
					if (method.equals(pd.getReadMethod())) {
						desc.setField(ROLE, GETTER);
					}
					else {
						desc.setField(ROLE, SETTER);
					}
					info.setDescriptor(desc);
				}
			}
			else if (includeOperation(method, beanKey)) {
				info = createModelMBeanOperationInfo(method, method.getName(), false);
				Descriptor desc = info.getDescriptor();
				desc.setField(ROLE, OPERATION);
				populateOperationDescriptor(desc, method);
				info.setDescriptor(desc);
			}

			if (info != null) {
				infos.add(info);
			}
		}

		return (ModelMBeanOperationInfo[]) infos.toArray(new ModelMBeanOperationInfo[infos.size()]);
	}

	/**
	 * Create constructor info for the given bean key and class.
	 * <p>Default implementation returns an empty array of
	 * <code>ModelMBeanConstructorInfo</code>.
	 *
	 * @param beanKey   the key associated with the MBean in the beans map
	 *                  of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the ModelMBeanConstructorInfo array
	 */
	protected ModelMBeanConstructorInfo[] getConstructorInfo(String beanKey, Class beanClass) {
		return new ModelMBeanConstructorInfo[0];
	}

	/**
	 * Create notification info for the given bean key and class.
	 * <p>Default implementation returns an empty array of
	 * <code>ModelMBeanNotificationInfo</code>.
	 *
	 * @param beanKey   the key associated with the MBean in the beans map
	 *                  of the <code>MBeanExporter</code>
	 * @param beanClass the class of the MBean
	 * @return the ModelMBeanNotificationInfo array
	 */
	protected ModelMBeanNotificationInfo[] getNotificationInfo(String beanKey, Class beanClass) {
		return new ModelMBeanNotificationInfo[0];
	}

	/**
	 * Create parameter info for the given method. Default implementation
	 * returns an empty arry of <code>MBeanParameterInfo</code>.
	 *
	 * @param method the <code>Method</code> to get the parameter information for.
	 * @return the <code>MBeanParameterInfo</code> array.
	 */
	protected MBeanParameterInfo[] getOperationParameters(Method method) {
		return new MBeanParameterInfo[0];
	}

	/**
	 * Allows subclasses to vote on the inclusion of a particular attribute accessor.
	 *
	 * @param method the accessor <code>Method</code>.
	 * @return <code>true</code> if the accessor should be included in the management interface,
	 *         otherwise <code>false<code>.
	 */
	protected abstract boolean includeReadAttribute(Method method, String beanKey);

	/**
	 * Allows subclasses to vote on the inclusion of a particular attribute mutator.
	 *
	 * @param method the mutator <code>Method</code>.
	 * @return <code>true</code> if the mutator should be included in the management interface,
	 *         otherwise <code>false<code>.
	 */
	protected abstract boolean includeWriteAttribute(Method method, String beanKey);

	/**
	 * Allows subclasses to vote on the inclusion of a particular operation.
	 *
	 * @param method the operation method
	 * @return whether the operation should be included in the management interface
	 */
	protected abstract boolean includeOperation(Method method, String beanKey);

	/**
	 * Get the description for a particular attribute.
	 * <p>Default implementation returns a description for the operation
	 * that is the name of corresponding <code>Method</code>.
	 *
	 * @param propertyDescriptor the PropertyDescriptor for the attribute
	 * @return the description for the attribute
	 */
	protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
		return propertyDescriptor.getDisplayName();
	}

	/**
	 * Get the description for a particular operation.
	 * <p>Default implementation returns a description for the operation
	 * that is the name of corresponding <code>Method</code>.
	 *
	 * @param method the operation method
	 * @return the description for the operation.
	 */
	protected String getOperationDescription(Method method) {
		return method.getName();
	}

	/**
	 * Allows subclasses to add extra fields to the <code>Descriptor</code> for a particular
	 * attribute. Default implementation is empty.
	 *
	 * @param descriptor the attribute descriptor
	 * @param getter     the accessor method for the attribute
	 * @param setter     the mutator method for the attribute
	 */
	protected void populateAttributeDescriptor(Descriptor descriptor, Method getter, Method setter) {
	}

	/**
	 * Allows subclasses to add extra fields to the <code>Descriptor</code> for a particular
	 * operation. Default implementation is empty.
	 *
	 * @param descriptor the operation descriptor
	 * @param method     the method corresponding to the operation
	 */
	protected void populateOperationDescriptor(Descriptor descriptor, Method method) {
	}

	/**
	 * Creates an instance of <code>ModelMBeanOperationInfo</code> for the
	 * given method. Populates the parameter info for the operation.
	 *
	 * @param method the <code>Method</code> to create a <code>ModelMBeanOperationInfo</code> for.
	 * @return the <code>ModelMBeanOperationInfo</code>.
	 */
	private ModelMBeanOperationInfo createModelMBeanOperationInfo(Method method, String name, boolean isAttributeOperation) {
		MBeanParameterInfo[] params = getOperationParameters(method);

		if (params.length == 0) {
			return new ModelMBeanOperationInfo(getOperationDescription(method), method);
		}
		else {
			return new ModelMBeanOperationInfo(name,
				getOperationDescription(method),
				getOperationParameters(method),
				method.getReturnType().getName(),
				MBeanOperationInfo.UNKNOWN);
		}
	}
}
