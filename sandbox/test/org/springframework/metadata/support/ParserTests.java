/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.support;

import org.springframework.metadata.AttributeParser;

import junit.framework.TestCase;

/**
 * Test the parsing of javadoc attributes to extract constructor
 * and Java bean properties information.
 * @author Mark Pollack
 * 
 */
public class ParserTests extends TestCase {

	/**
	 * Constructor for ParserTests.
	 * @param name The name of the test
	 */
	public ParserTests(String name) {
		super(name);
	}

	/**
	 * Test for the case when a null value for the attribute text
	 * is passed to the DefaultAttributeParser.
	 *
	 */
	public void testNullAttribute() {
		AttributeParser parser = new DotNetAttributeParser();

		String attributeText = null;
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertFalse("Null text should not be parsable", pa.isValid());
		assertEquals(
			"Error message not as expected",
			"Attribute Text was null",
			pa.getErrorText());
		assertEquals(
			"Expected zero constructor arguments",
			0,
			pa.getConstructorArgs().size());
		assertEquals("Expected zero properties", 0, pa.getProperties().size());

	}
	/**
	 * Test the case when there the attribute parenthesis is not closed
	 *
	 */
	public void testUnclosedParen() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText = "PersonAttribute (";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertFalse(
			"Did not generate parse error in case of missing paren",
			pa.isValid());
		assertTrue(
			"Incorrect error message",
			pa.getErrorText().startsWith(
				"Could not find closing parenthesis in attribute text"));

		//attributeText = "PersonAttribute ())";
		//pa = parser.getParsedAttribute(attributeText);
		//assertFalse(
		//	"Did not generate parse error in case of extra paren",
		//	pa.isValid());
	}

	/**
	 * Test for when only the classname is given, with or without parenthesis,
	 * to the DefaultAttributeParser.  Also makes sure that attribute
	 * classname has trailing space trimmed.
	 *
	 */
	public void testClassnameOnly() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText = "PersonAttribute ";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertTrue(
			"Did not parse case of no parenthesis attribute usage",
			pa.isValid());
		assertEquals(
			"No Paren - Did not parse attribute classname correctly",
			"PersonAttribute",
			pa.getClassname());

		attributeText = "PersonAttribute()";
		assertTrue(
			"Did not parse case of empty parenthesis attribute usage",
			pa.isValid());
		assertEquals(
			"Empty Paren - Did not parse attribute classname correctly",
			"PersonAttribute",
			pa.getClassname());

		attributeText = "PersonAttribute ( ) ";
		assertTrue(
			"Did not parse case of empty parenthesis with spaces attribute usage",
			pa.isValid());
		assertEquals(
			"Empty Paren with Spaces - Did not parse attribute classname correctly",
			"PersonAttribute",
			pa.getClassname());

	}

	/**
	 * Test for when the attribute only contains constructor arguments.
	 *
	 */
	public void testOnlyCtorArgs() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText = "PersonAttribute ( \"Albert\", 33, 150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertTrue(
			"Did not parse case of ctor only args correctly",
			pa.isValid());
		assertEquals(
			"Incorrect number of constructor arguments.",
			3,
			pa.getConstructorArgs().size());
		assertEquals(
			"Incorrect number of properties",
			0,
			pa.getProperties().size());
		assertEquals(
			"Ctor arguement not as expected",
			"\"Albert\"",
			pa.getConstructorArgs().get(0));
		assertEquals(
			"Ctor arguement not as expected",
			"33",
			pa.getConstructorArgs().get(1));
		assertEquals(
			"Ctor arguement not as expected",
			"150.4",
			pa.getConstructorArgs().get(2));
	}

	/**
	 * Test for the case when there are only property name value
	 * pairs in the attribute text.
	 *
	 */
	public void testOnlyPropArgs() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"PersonAttribute ( name=\"Albert\", age=33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertTrue(
			"Did not parse case of properties only attribute correctly",
			pa.isValid());
		assertEquals(
			"Incorrect number of bean properties.",
			3,
			pa.getProperties().size());
	}
	
	
	/**
	 * Test when there are spaces in string properties.
	 *
	 */
	public void testStringProperties() {
		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"PersonAttribute ( name =Albert Einstein, age =33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertTrue(
			"Did not parse case of properties only attribute correctly",
			pa.isValid());
		assertEquals(
			"Incorrect number of bean properties.",
			3,
			pa.getProperties().size());
		System.out.println("property map =" + pa.getProperties());
		assertEquals("Name not as expected",
					 (String)pa.getProperties().get("name"),
					 "Albert Einstein" );
	}
	
	/**
	 * Test for a combination of constructor and properties attribute
	 * parameters.
	 *
	 */
	public void testAllArgs() {
		AttributeParser parser = new DotNetAttributeParser();
		
		String attributeText =
			"PersonAttribute ( \"Albert\", 33, 150.4, name=\"Albert\", age=33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertTrue(
			"Did not parse attribute correctly",
			pa.isValid());
		assertEquals(
			"Incorrect number of constructor arguments.",
			3,
			pa.getConstructorArgs().size());			
		assertEquals(
			"Incorrect number of bean properties.",
			3,
			pa.getProperties().size());
	}
	
	/**
	 * Test for a simple case when there is a mistake/typo in
	 * specifying properties.
	 *
	 */
	public void testBadProperties() {

		AttributeParser parser = new DotNetAttributeParser();
		String attributeText =
			"PersonAttribute ( name=\"Albert\", age*33, height=150.4 )";
		AttributeDefinition pa = parser.getAttributeDefinition(attributeText);
		assertFalse(
			"Should have not parsed attribute.",
			pa.isValid());
		//System.out.println("error messgae = " + pa.getErrorText());				
		assertTrue(
			"Incorrect error message",
			pa.getErrorText().startsWith(
			"Could not parse name/value property "));
		
	}
}
