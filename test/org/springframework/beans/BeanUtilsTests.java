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

package org.springframework.beans;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.propertyeditors.CustomDateEditor;

/**
 * @author Juergen Hoeller
 * @since 19.05.2003
 */
public class BeanUtilsTests extends TestCase {

	public void testInstantiateClass() {
		// give proper class
		BeanUtils.instantiateClass(ArrayList.class);

		try {
			// give interface
			BeanUtils.instantiateClass(List.class);
			fail("Should have thrown FatalBeanException");
		}
		catch (FatalBeanException ex) {
			// expected
		}

		try {
			// give class without default constructor
			BeanUtils.instantiateClass(CustomDateEditor.class);
			fail("Should have thrown FatalBeanException");
		}
		catch (FatalBeanException ex) {
			// expected
		}
	}

	public void testCopyProperties() throws Exception {
		TestBean tb = new TestBean();
		tb.setName("rod");
		tb.setAge(32);
		tb.setTouchy("touchy");
		TestBean tb2 = new TestBean();
		assertTrue("Name empty", tb2.getName() == null);
		assertTrue("Age empty", tb2.getAge() == 0);
		assertTrue("Touchy empty", tb2.getTouchy() == null);

		try {
			BeanUtils.copyProperties(tb, "");
			fail("Should have thrown IllegalArgumentException");
		}
		catch (InvalidPropertyException ex) {
			// expected
		}

		BeanUtils.copyProperties(tb, tb2);
		assertTrue("Name copied", tb2.getName().equals(tb.getName()));
		assertTrue("Age copied", tb2.getAge() == tb.getAge());
		assertTrue("Touchy copied", tb2.getTouchy().equals(tb.getTouchy()));
	}

	public void testCopyPropertiesWithIgnore() {
		TestBean tb = new TestBean();
		assertTrue("Name empty", tb.getName() == null);
		tb.setAge(32);
		TestBean tb2 = new TestBean();
		tb2.setName("rod");
		assertTrue("Age empty", tb2.getAge() == 0);
		assertTrue("Touchy empty", tb2.getTouchy() == null);

		BeanUtils.copyProperties(tb, tb2, new String[]{"spouse", "touchy", "age"});
		assertTrue("Name copied", tb2.getName() == null);
		assertTrue("Age still empty", tb2.getAge() == 0);
		assertTrue("Touchy still empty", tb2.getTouchy() == null);
	}

	public void testCopyPropertiesWithIgnoredNonExistingProperty() {
		NameAndSpecialProperty source = new NameAndSpecialProperty();
		source.setName("name");
		TestBean target = new TestBean();
		BeanUtils.copyProperties(source, target, new String[] {"specialProperty"});
		assertEquals(target.getName(), "name");
	}

	public void testGetPropertyDescriptors() throws Exception {
		PropertyDescriptor[] actual = Introspector.getBeanInfo(TestBean.class).getPropertyDescriptors();
		PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(TestBean.class);
		assertNotNull("Descriptors should not be null", descriptors);
		assertEquals("Invalid number of descriptors returned", actual.length, descriptors.length);
	}


	private static class NameAndSpecialProperty {

		private String name;

		private int specialProperty;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setSpecialProperty(int specialProperty) {
			this.specialProperty = specialProperty;
		}

		public int getSpecialProperty() {
			return specialProperty;
		}
	}

}
