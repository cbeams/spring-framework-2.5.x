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

package org.springframework.jmx.export.annotation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedAttribute;
import org.springframework.jmx.export.metadata.ManagedOperation;
import org.springframework.jmx.export.metadata.ManagedResource;

/**
 * Implementation of the <code>JmxAttributeSource</code> interface that
 * reads JDK 1.5+ annotations and exposes the corresponding attributes.
 *
 * <p>This is a direct alternative to <code>AttributesJmxAttributeSource</code>,
 * which is able to read in source-level attributes via Commons Attributes.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.jmx.export.annotation.ManagedResource
 * @see org.springframework.jmx.export.annotation.ManagedAttribute
 * @see org.springframework.jmx.export.annotation.ManagedOperation
 * @see org.springframework.jmx.export.metadata.AttributesJmxAttributeSource
 * @see org.springframework.metadata.commons.CommonsAttributes
 */
public class AnnotationJmxAttributeSource implements JmxAttributeSource {

	public ManagedResource getManagedResource(Class beanClass) throws InvalidMetadataException {
		Annotation ann = beanClass.getAnnotation(org.springframework.jmx.export.annotation.ManagedResource.class);
		if (ann == null) {
			return null;
		}
		org.springframework.jmx.export.annotation.ManagedResource mr =
				(org.springframework.jmx.export.annotation.ManagedResource) ann;
		ManagedResource attr = new ManagedResource();
		attr.setObjectName(mr.objectName());
		attr.setDescription(mr.description());
		attr.setCurrencyTimeLimit(mr.currencyTimeLimit());
		attr.setLog(mr.log());
		attr.setLogFile(mr.logFile());
		attr.setPersistPolicy(mr.persistPolicy());
		attr.setPersistPeriod(mr.persistPeriod());
		attr.setPersistLocation(mr.persistLocation());
		attr.setPersistName(mr.persistName());
		return attr;
	}

	public ManagedAttribute getManagedAttribute(Method method) throws InvalidMetadataException {
		Annotation ann = method.getAnnotation(org.springframework.jmx.export.annotation.ManagedAttribute.class);
		if (ann == null) {
			return null;
		}
		org.springframework.jmx.export.annotation.ManagedAttribute ma =
				(org.springframework.jmx.export.annotation.ManagedAttribute) ann;
		ManagedAttribute attr = new ManagedAttribute();
		attr.setDescription(ma.description());
		attr.setPersistPolicy(ma.persistPolicy());
		attr.setPersistPeriod(ma.persistPeriod());
		attr.setCurrencyTimeLimit(ma.currencyTimeLimit());
		if (ma.defaultValue().length() > 0) {
			attr.setDefaultValue(ma.defaultValue());
		}
		return attr;
	}

	public ManagedOperation getManagedOperation(Method method) throws InvalidMetadataException {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			throw new InvalidMetadataException(
					"The ManagedOperation attribute is not valid for JavaBean properties. Use ManagedAttribute instead.");
		}

		Annotation ann = method.getAnnotation(org.springframework.jmx.export.annotation.ManagedOperation.class);
		if (ann == null) {
			return null;
		}
		org.springframework.jmx.export.annotation.ManagedOperation mo =
				(org.springframework.jmx.export.annotation.ManagedOperation) ann;
		ManagedOperation op = new ManagedOperation();
		op.setDescription(mo.description());
		op.setCurrencyTimeLimit(mo.currencyTimeLimit());
		return op;
	}

}
