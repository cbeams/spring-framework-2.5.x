package org.springframework.util;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 */
public class PluralizerTests extends TestCase {

	public void testDefaultRule() {
		assertEquals("Incorrect plural of product", "products", pluralize("product"));
	}

	public void testYRule() {
		assertEquals("abilities", pluralize("ability"));
		assertEquals("agencies", pluralize("agency"));
		assertEquals("honeys", pluralize("honey"));
	}

	public void testSXZRule() {
		assertEquals("gases", pluralize("gas"));
		assertEquals("boxes", pluralize("box"));
		assertEquals("whizzes", pluralize("whizz"));
	}

	public void testHRule() {
		assertEquals("bushes", pluralize("bush"));
		assertEquals("rushes", pluralize("rush"));
		assertEquals("churches", pluralize("church"));
		assertEquals("cheetahs", pluralize("cheetah"));
	}

	private String pluralize(String term) {
		return Pluralizer.pluralize(term);
	}
}
