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
package org.springframework.test;

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
	 * Assert that an attribute with specified name is present in given attributes.
	 */
	public void assertAttributePresent(AttributeSource attributes, String attributeName) {
		assertTrue("The attributes attribute '" + attributeName + "' is not present in attributes but should be",
				attributes.containsAttribute(attributeName));
	}

	/**
	 * Assert that an attribute with specified name is not present in given
	 * attributes map.
	 */
	public void assertAttributeNotPresent(AttributeSource attributes, String attributeName) {
		assertTrue("The attributes attribute '" + attributeName + "' is present in attributes but shouldn't be",
				!attributes.containsAttribute(attributeName));
	}

	/**
	 * Assert that an attribute exists in the attributes map of the specified type.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name
	 * @param clazz the required type
	 */
	public void assertAttributeInstanceOf(AttributeSource attributes, String attributeName, Class clazz) {
		assertAttributePresent(attributes, attributeName);
		org.springframework.util.Assert.isInstanceOf(clazz, attributes.getAttribute(attributeName));
	}

	/**
	 * Assert that an attribute exists in the attributes map of the specified value.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name
	 * @param attributeValue the attribute value
	 */
	public void assertAttributeEquals(AttributeSource attributes, String attributeName, Object attributeValue) {
		if (attributeValue != null) {
			assertAttributeInstanceOf(attributes, attributeName, attributeValue.getClass());
		}
		assertEquals("The attributes attribute '" + attributeName + "' must equal '" + attributeValue + "'",
				attributeValue, attributes.getAttribute(attributeName));
	}

	/**
	 * Assert that a collection exists in the attributes map under the provided
	 * attribute name, with the specified size.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name
	 * @param size the expected collection size
	 */
	public void assertCollectionAttributeSize(AttributeSource attributes, String attributeName, int size) {
		assertAttributeInstanceOf(attributes, attributeName, Collection.class);
		assertEquals("The attributes collection attribute '" + attributeName + "' must have " + size + " elements",
				size, ((Collection) attributes.getAttribute(attributeName)).size());
	}

	/**
	 * Assert that a bean attribute in the attributes map has a property
	 * with the provided property value.
	 * @param attributes the attributes map
	 * @param attributeName the attribute name (of a javabean)
	 * @param propertyName the bean property name
	 * @param propertyValue the expected property value
	 */
	public void assertAttributePropertyEquals(AttributeSource attributes, String attributeName, String propertyName,
			Object propertyValue) {
		assertAttributePresent(attributes, attributeName);
		Object value = attributes.getAttribute(attributeName);
		org.springframework.util.Assert.isTrue(!BeanUtils.isSimpleProperty(value.getClass()),
				"Attribute value must be a bean");
		BeanWrapper wrapper = new BeanWrapperImpl(value);
		assertEquals(propertyValue, wrapper.getPropertyValue(propertyName));
	}
}