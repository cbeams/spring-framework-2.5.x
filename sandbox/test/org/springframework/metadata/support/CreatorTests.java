/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.support;

import org.springframework.metadata.AttributeException;
import org.springframework.metadata.AttributeParser;

import junit.framework.TestCase;

/**
 * Test the creation of attributes given their description.
 * 
 * @author Mark Pollack
 * @since Oct 6, 2003
 */
public class CreatorTests extends TestCase {

	/**
	 * Constructor for CreatorTests.
	 * @param name Name of the test
	 */
	public CreatorTests(final String name) {
		super(name);
	}

	/**
	 * Call the attribute creator with bad values.
	 *
	 */
	public void testBadArgs() {
		AttributeCreator creator = new AttributeCreator();
		try {
			Object o = creator.createAttribute(null);
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
		}
	}

	/**
	 * Test that we check for the attribute class to have 
	 * at least one public constructor.
	 *
	 */
	public void testNoPublicCtor() {
		AttributeParser parser = new DotNetAttributeParser();
		AttributeCreator creator = new AttributeCreator();
		String attributeText =
			"org.springframework.metadata.support.PrivateCtorAttribute";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		try {
			Object o = creator.createAttribute(pa);
			fail("Should have thrown AttributeException for not having no arg public ctor");
		} catch (AttributeException e) {
		}
	}

	/**
	 * Test that a no argument constructor in the attribute text will
	 * create an instance of the class
	 *
	 */
	public void testNoArgFqn() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);

		AttributeCreator creator = new AttributeCreator();
		Object o = creator.createAttribute(pa);
		assertNotNull("Created attribute should not be null", o);
		assertTrue(
			"Attribute should be of type org.springframework.metadata.support.PersonAttribute",
			(o instanceof PersonAttribute));
	}

	/**
	 * The PersonAttribute does not contain a constructor containing the
	 * name and age only.  This should produce a AttributeException.
	 *
	 */
	public void testBadCtorArgFqn() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute ( \"Albert\", 33)";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);

		AttributeCreator creator = new AttributeCreator();
		try {
			Object o = creator.createAttribute(pa);
			fail();
		} catch (AttributeException e) {
			String errorString =
				e.getMessage().substring(0, e.getMessage().indexOf('['));
			assertEquals(
				"Error message not as expected",
				"No constructor in org.springframework.metadata.support.PersonAttribute matched ",
				errorString);
		}
	}


	/**
	 * Test creating an attribute using the correct constructor arguments.
	 *
	 */
	public void testCtorArgFqn() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute ( \"Albert\", 33, 150.4)";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);

		AttributeCreator creator = new AttributeCreator();
		Object o = creator.createAttribute(pa);
		assertNotNull("Created attribute should not be null", o);
		assertTrue(
			"Attribute should be of type org.springframework.metadata.support.PersonAttribute",
			(o instanceof PersonAttribute));
		PersonAttribute person = (PersonAttribute) o;
		assertEquals("Person name not as expected", "Albert", person.getName());
		assertEquals("Person age not as expected", 33, person.getAge());
		assertEquals("Person height not as expected", 150.4, person.getHeight(), 0.1);

	}

	/**
	 * Test creating the attribute using javabean properties
	 *
	 */
	public void testPropertyFqn() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute ( name=Albert Einstein, age=33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		
		AttributeCreator creator = new AttributeCreator();
		Object o = creator.createAttribute(pa);
		doAssertOnPerson(o);

	}

	/**
	 * Test the creation of an attribute given a simple
	 * no constructor, no property description in
	 * a AttributeDefinition class.
	 *
	 */
	public void testBadClassname() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText = "PersonAttributeBadName (name=Albert Einstein, age=33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);

		AttributeCreator creator = new AttributeCreator();
		
		try {
			Object o = creator.createAttribute(pa);
			fail("Should have thrown AttributeException.");
		} catch (AttributeException e) {
			String errorString =
				e.getMessage().substring(0, e.getMessage().indexOf('['));
			assertEquals("Should not be able to create attribute class",
						 errorString,
						 "Could not create class using classnames ");
		}
	}
	
	/**
	 * These assert methods keeps getting done over and over, put them in
	 * one place.
	 * @param o The object created from the AttributeCreator
	 */
	private void doAssertOnPerson(Object o) {

		assertNotNull("Created attribute should not be null", o);
		assertTrue(
			"Attribute should be of type org.springframework.metadata.support.PersonAttribute",
			(o instanceof PersonAttribute));
		PersonAttribute person = (PersonAttribute) o;
		assertEquals("Person name not as expected", "Albert Einstein", person.getName());
		assertEquals("Person age not as expected", 33, person.getAge());
		assertEquals("Person height not as expected", 150.4, person.getHeight(), 0.1);

	}
	/**
	 * Test the logic for appending the keyword 'Attribute' to the base classname
	 * The rest of the test is 
	 *
	 */
	public void testAbbreviatedClassname() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"org.springframework.metadata.support.Person ( name=Albert Einstein, age=33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		
		AttributeCreator creator = new AttributeCreator();
		Object o = creator.createAttribute(pa);
		doAssertOnPerson(o);
	}
	
	/**
	 * Test using the package search algorithm.
	 *
	 */
	public void testUsingPackageSearch() {
		AttributeParser parser = new DotNetAttributeParser();
		String[] attributePackages = { "org.springframework.metadata.support" };
		AttributeCreator.setAttributePackages(attributePackages);
		AttributeCreator creator = new AttributeCreator();	
		
		String attributeText = "PersonAttribute (name=Albert Einstein, age=33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		Object o = creator.createAttribute(pa);
		doAssertOnPerson(o);
		
		attributeText = "Person (name=Albert Einstein, age=33, height=150.4 )";
		pa = parser.getAttributeDefinition(attributeText);
		o = creator.createAttribute(pa);
		doAssertOnPerson(o);
		
	}
	
}
