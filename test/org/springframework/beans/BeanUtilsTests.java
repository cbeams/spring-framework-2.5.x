package org.springframework.beans;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(BeanUtils.getAllInterfaces(testBean));
		assertTrue("Correct number of interfaces", ifcs.size() == 3);
		assertTrue("Contains Serializable", ifcs.contains(Serializable.class));
		assertTrue("Contains ITestBean", ifcs.contains(ITestBean.class));
		assertTrue("Contains IOther", ifcs.contains(IOther.class));
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
		catch (IllegalArgumentException ex) {
			// expected
		}

		BeanUtils.copyProperties(tb, tb2);
		assertTrue("Name copied", tb2.getName().equals(tb.getName()));
		assertTrue("Age copied", tb2.getAge() == tb.getAge());
		assertTrue("Touchy copied", tb2.getTouchy().equals(tb.getTouchy()));
	}

	public void testCopyPropertiesWithIgnore() throws IllegalAccessException, PropertyVetoException {
		TestBean tb = new TestBean();
		tb.setName("rod");
		tb.setAge(32);
		TestBean tb2 = new TestBean();
		assertTrue("Name empty", tb2.getName() == null);
		assertTrue("Age empty", tb2.getAge() == 0);
		assertTrue("Touchy empty", tb2.getTouchy() == null);

		BeanUtils.copyProperties(tb, tb2, new String[] {"spouse", "touchy", "age"});
		assertTrue("Name copied", tb2.getName().equals(tb.getName()));
		assertTrue("Age still empty", tb2.getAge() == 0);
		assertTrue("Touchy still empty", tb2.getTouchy() == null);
	}

}
