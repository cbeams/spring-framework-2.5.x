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
import java.util.ArrayList;
import java.util.List;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.beans.BeanUtils;

/**
 * Extends the <code>AbstractModelMBeanInfoAssembler</code> to add a basic
 * algorithm for building metadata based on the reflective metadata of
 * the MBean class.
 * <p/>
 * The logic for creating MBean metadata from the reflective metadata is contained
 * in this class, but this class makes no desicions as to which methods and
 * properties are to be exposed. Instead it gives subclasses a chance to 'vote'
 * on each property or method through the <code>includeXXX</code> methods.
 * </p>
 * <p/>
 * Subclasses are also given the opportunity to populate attribute and operation
 * metadata with additional descriptors once the metadata is assembled through the
 * <code>populateXXXDescriptor</code> methods.
 * </p>
 *
 * @author Rob Harrop
 * @see #includeOperation(java.lang.reflect.Method)
 * @see #includeReadAttribute(java.lang.reflect.Method)
 * @see #includeWriteAttribute(java.lang.reflect.Method)
 * @see #populateAttributeDescriptor(javax.management.Descriptor, java.lang.reflect.Method, java.lang.reflect.Method)
 * @see #populateOperationDescriptor(javax.management.Descriptor, java.lang.reflect.Method)
 */
public abstract class AbstractReflectionBasedModelMBeanInfoAssembler
		extends AbstractModelMBeanInfoAssembler {

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
	 * Iterates through all properties on the MBean class and gives subclasses the chance to vote on the
	 * inclusion of both the accessor and mutator. If a particular accessor or mutator is voted for
	 * inclusion the appropriate metadata is assembled and passed to the subclass for descriptor population.
	 *
	 * @param beanKey the key associated with the MBean in the <code>beans</code> <code>Map</code>
	 * of the <code>MBeanExporter</code>.
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

			if (getter != null && !includeReadAttribute(getter)) {
				getter = null;
			}

			Method setter = props[i].getWriteMethod();

			if (setter != null && !includeWriteAttribute(setter)) {
				setter = null;
			}

			if ((getter != null) || (setter != null)) {
				// if both getter and setter are null
				// then this does not need exposing
				ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo(props[i].getName(), getAttributeDescription(props[i]), getter, setter);

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
	 * Iterates through all methods on the MBean class and gives subclasses the chance to vote on their inclusion.
	 * If a particular method corresponds to the accessor or mutator of an attribute that is inclued in the
	 * managment interface then the corresponding operation is exposed with the &quot;role&quot; descriptor field
	 * set to the appropriate value.
	 *
	 * @param beanKey the key associated with the MBean in the <code>beans</code> <code>Map</code>
	 * of the <code>MBeanExporter</code>.
	 * @param beanClass the <code>Class</code> of the MBean.
	 * @return the operation metadata.
	 * @see #populateOperationDescriptor(javax.management.Descriptor, java.lang.reflect.Method)
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
				if ((method.equals(pd.getReadMethod()) && includeReadAttribute(method))
						|| (method.equals(pd.getWriteMethod()) && includeWriteAttribute(method))) {
					// Attributes need to have their methods exposed as
					// operations to the JMX server as well.
					info = new ModelMBeanOperationInfo(getOperationDescription(method), method);
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
			else if (includeOperation(method)) {
				info = new ModelMBeanOperationInfo(getOperationDescription(method), method);
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
	 * Returns an empty array of <code>ModelMBeanConstructorInfo</code>.
	 */
	protected ModelMBeanConstructorInfo[] getConstructorInfo(String beanKey, Class beanClass) {
		return new ModelMBeanConstructorInfo[]{};
	}

	/**
	 * Returns an empty array of <code>ModelMBeanNotificationInfo</code>.
	 */
	protected ModelMBeanNotificationInfo[] getNotificationInfo(String beanKey, Class beanClass) {
		return new ModelMBeanNotificationInfo[]{};
	}

	/**
	 * Allows subclasses to vote on the inclusion of a particular attribute accessor.
	 * @param method the accessor <code>Method</code>.
	 * @return <code>true</code> if the accessor should be included in the management interface,
	 * otherwise <code>false<code>.
	 */
	protected abstract boolean includeReadAttribute(Method method);

	/**
	 * Allows subclasses to vote on the inclusion of a particular attribute mutator.
	 * @param method the mutator <code>Method</code>.
	 * @return <code>true</code> if the mutator should be included in the management interface,
	 * otherwise <code>false<code>.
	 */
	protected abstract boolean includeWriteAttribute(Method method);

	/**
	 * Allows subclasses to vote on the inclusion of a particular operation.
	 * @param method the operation <code>Method</code>.
	 * @return <code>true</code> if the operation should be included in the management interface,
	 * otherwise <code>false<code>.
	 */
	protected abstract boolean includeOperation(Method method);

	/**
	 * Gets the description for a particular operation.
	 * @param method the operation <code>Method</code>.
	 * @return the description for the operation.
	 */
	protected abstract String getOperationDescription(Method method);

	/**
	 * Gets the description for a particular attribute.
	 * @param propertyDescriptor the attribute <code>PropertyDescriptor</code>.
	 * @return the description for the attribute.
	 */
	protected abstract String getAttributeDescription(PropertyDescriptor propertyDescriptor);

	/**
	 * Allows subclasses to add extra fields to the <code>Descriptor</code> for a particular
	 * attribute.
	 * @param descriptor the attribute <code>Descriptor</code>.
	 * @param getter the accessor method for the attribute.
	 * @param setter the mutator method for the attribute.
	 */
	protected abstract void populateAttributeDescriptor(Descriptor descriptor,
			Method getter, Method setter);

	/**
	 * Allows subclasses to add extra fields to the <code>Descriptor</code> for a particular
	 * operation.
	 * @param descriptor the operation <code>Descriptor</code>.
	 * @param method the method corresponding to the operation.
	 */
	protected abstract void populateOperationDescriptor(Descriptor descriptor, Method method);

}
