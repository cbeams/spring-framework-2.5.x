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
 * @author Rob Harrop
 */
public abstract class AbstractReflectionBasedModelMBeanInfoAssembler
		extends AbstractModelMBeanInfoAssembler {

	private static final String VISIBILITY = "visibility";
	private static final Integer ATTRIBUTE_OPERATION_VISIBILITY = new Integer(4);

	private static final String ROLE = "role";
	private static final String GETTER = "getter";
	private static final String SETTER = "setter";
	private static final String OPERATION = "operation";

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
				ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo(
						props[i].getName(), getAttributeDescription(props[i]), getter, setter);

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

	protected ModelMBeanConstructorInfo[] getConstructorInfo(String beanKey, Class beanClass) {
		return new ModelMBeanConstructorInfo[]{};
	}

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

	protected ModelMBeanNotificationInfo[] getNotificationInfo(String beanKey, Class beanClass) {
		return new ModelMBeanNotificationInfo[]{};
	}

	protected abstract boolean includeReadAttribute(Method method);

	protected abstract boolean includeWriteAttribute(Method method);

	protected abstract boolean includeOperation(Method method);

	protected abstract String getOperationDescription(Method method);

	protected abstract String getAttributeDescription(
			PropertyDescriptor propertyDescriptor);

	protected abstract void populateAttributeDescriptor(Descriptor descriptor,
			Method getter, Method setter);

	protected abstract void populateOperationDescriptor(Descriptor descriptor, Method method);

}
