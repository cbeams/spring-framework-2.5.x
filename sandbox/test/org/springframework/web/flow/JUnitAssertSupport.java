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
package org.springframework.web.flow;

import java.util.Collection;

import junit.framework.Assert;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.binding.AttributeSource;

/**
 * Support class for generally useful JUnit assertions.
 * TODO - move to spring-mock.jar, test.support package
 * 
 * @author Keith Donald
 */
public class JUnitAssertSupport extends Assert {

	/**
	 * Assert that an attribute with specified name is present in given model.
	 */
	public void assertAttributePresent(AttributeSource model, String attributeName) {
		assertTrue("The model attribute '" + attributeName + "' is not present in model but should be", model
				.containsAttribute(attributeName));
	}

	/**
	 * Assert that an attribute with specified name is not present in given
	 * model.
	 */
	public void assertAttributeNotPresent(AttributeSource model, String attributeName) {
		assertTrue("The model attribute '" + attributeName + "' is present in model but shouldn't be", !model
				.containsAttribute(attributeName));
	}

	/**
	 * Assert that an attribute exists in the model map of the specified type.
	 * @param model the model map
	 * @param attributeName the attribute name
	 * @param clazz the required type
	 */
	public void assertAttributeInstanceOf(AttributeSource model, String attributeName, Class clazz) {
		assertAttributePresent(model, attributeName);
		org.springframework.util.Assert.isInstanceOf(clazz, model.getAttribute(attributeName));
	}

	/**
	 * Assert that an attribute exists in the model map of the specified value.
	 * @param model the model map
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void assertAttributeEquals(AttributeSource model, String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			assertAttributeInstanceOf(model, attributeName, attributeValue.getClass());
		}
		assertEquals("The model attribute '" + attributeName + "' must equal '" + attributeValue + "'", attributeValue,
				model.getAttribute(attributeName));
	}

	/**
	 * Assert that a collection exists in the model map under the provided
	 * attribute name, with the specified size.
	 * @param model the model map
	 * @param attributeName the attribute name
	 * @param size the expected collection size
	 */
	public void assertCollectionAttributeSize(AttributeSource model, String attributeName, int size) {
		assertAttributeInstanceOf(model, attributeName, Collection.class);
		assertEquals("The model collection attribute '" + attributeName + "' must have " + size + " elements", size,
				((Collection)model.getAttribute(attributeName)).size());
	}

	/**
	 * Assert that a bean property attribute in the model map has a property
	 * with the provided property value.
	 * @param model the model map
	 * @param attributeName the attribute name (of a javabean)
	 * @param propertyName the bean property name
	 * @param propertyValue the expected property value
	 */
	public void assertAttributePropertyEquals(AttributeSource model, String attributeName, String propertyName,
			Object propertyValue) {
		assertAttributePresent(model, attributeName);
		Object value = model.getAttribute(attributeName);
		org.springframework.util.Assert.isTrue(!BeanUtils.isSimpleProperty(value.getClass()),
				"Attribute value must be a bean");
		BeanWrapper wrapper = new BeanWrapperImpl(value);
		assertEquals(propertyValue, wrapper.getPropertyValue(propertyName));
	}
}