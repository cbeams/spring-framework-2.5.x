package org.springframework.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;

/**
 * @author Rob Harrop
 */
public class ConventionUtilsTests extends TestCase {

	public void testSimpleObject() {
		TestBean testBean = new TestBean();
		assertEquals("Incorrect singular variable name", "testBean", ConventionUtils.getVariableName(testBean));
	}

	public void testArray() {
		TestBean[] testBeans = new TestBean[0];
		assertEquals("Incorrect plural array form", "testBeans", ConventionUtils.getVariableName(testBeans));
	}

	public void testCollections() {
		List list = new ArrayList();
		list.add(new TestBean());
		assertEquals("Incorrect plural List form", "testBeans", ConventionUtils.getVariableName(list));

		Set set = new HashSet();
		set.add(new TestBean());
		assertEquals("Incorrect plural Set form", "testBeans", ConventionUtils.getVariableName(set));

		List emptyList = new ArrayList();
		assertEquals("List without items should just return class name", "arrayList", ConventionUtils.getVariableName(emptyList));
	}

}
