/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.bcel;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.metadata.AttributeException;
import org.springframework.metadata.AttributeWriter;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.support.PersonAttribute;

/**
 * 
 * @author Mark Pollack
 * @since Sep 28, 2003
 * 
 */
public class AttributeWriterTests extends TestCase {

	/**
	 * Constructor for AttributeWriterTests.
	 * @param arg0
	 */
	public AttributeWriterTests(String arg0) {
		super(arg0);
	}

	/**
	 * Test to bootstrap development of the meat of the attribute package.
	 *
	 */
	public void testAttributes() throws NoSuchMethodException {
		AttributeWriter writer = null; //new BcelAttributeWriter();

		//The class that we will "add an attribute" to programatically.
		Class targetClass = org.springframework.metadata.bcel.TargetBean.class;
		String targetClassname = targetClass.getName();

		//The attribute text to add.
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute ( name=Albert Einstein, age=33, height=150.4 )";

		//Tell the writer what class we are going to modify
		writer.initializeClass(targetClassname);
		//Tell the writer to add the class attribute.
		writer.addClassAttribute(attributeText);

		//Tell the writer to add the method attribute
		Method targetMethod = targetClass.getMethod("doReport", null);
		writer.addMethodAttribute(targetMethod, attributeText);

		String eclipseOutputDir = getClassfileDir();

		//Tell the writer to finish its job and write out the new bytecode.
		writer.finishClass(eclipseOutputDir);

		File clazzFile = getClassfile(eclipseOutputDir, targetClass);
		//Does it exist?
		assertTrue(
			"Modified TargetBean class " + clazzFile + " should exist",
			clazzFile.exists());

		//Are the class attributes there?
		Attributes attributes = null; //new BcelAttributes();
		Collection attribs = attributes.getAttributes(targetClass);
		assertEquals("Expected one custom class attribute", 1, attribs.size());

		//Is is what we expect?
		Object o = attribs.iterator().next();
		doAssertOnPerson(o);

		//Are the method attributes there?
		Collection methodAttribs = attributes.getAttributes(targetMethod);
		assertEquals(
			"Expected one custom method attribute",
			1,
			methodAttribs.size());

		//Is is what we expect?
		o = methodAttribs.iterator().next();
		doAssertOnPerson(o);

	}

	/**
	 * Try to add an attribute using a classname that does not
	 * exist.  
	 *
	 */
	public void testBadAbbreviatedAttribute() {
		AttributeWriter writer = null; //new BcelAttributeWriter();
		String[] attribPackages = { "org.springframework.metadata.xxsupport" };
		writer.setAttributePackages(attribPackages);
		//The class that we will "add an attribute" to programatically.
		Class targetClass = org.springframework.metadata.bcel.TargetBean.class;
		String targetClassname = targetClass.getName();

		//The attribute text to add.
		String attributeText =
			"PersonAttribute ( name=Albert Einstein, age=33, height=150.4 )";

		//Tell the writer what class we are going to modify
		writer.initializeClass(targetClassname);
		//Tell the writer to add the class attribute.
		try {
			writer.addClassAttribute(attributeText);
			fail(
				"Should not have been able to add a class attribute "
					+ "with a bad classname.  AttributeText = "
					+ attributeText);
		} catch (AttributeException e) {
			//all ok.
		}

	}

	/**
	 * Set the attribute packages property on the AttributeWriter so
	 * that we can use abbreviated names in the javadoc tag.
	 *
	 */
	public void testAbbreviatedAttribute() {
		AttributeWriter writer = null; //new BcelAttributeWriter();
		String[] attribPackages = { "org.springframework.metadata.support" };
		writer.setAttributePackages(attribPackages);
		//The class that we will "add an attribute" to programatically.
		Class targetClass = org.springframework.metadata.bcel.TargetBean.class;
		String targetClassname = targetClass.getName();

		//The attribute text to add.
		String attributeText =
			"PersonAttribute ( name=Albert Einstein, age=33, height=150.4 )";

		//Tell the writer what class we are going to modify
		writer.initializeClass(targetClassname);
		//Tell the writer to add the class attribute.
		writer.addClassAttribute(attributeText);

		String eclipseOutputDir = getClassfileDir();

		//Tell the writer to finish its job and write out the new bytecode.
		writer.finishClass(eclipseOutputDir);

		File clazzFile = getClassfile(eclipseOutputDir, targetClass);
		//Does it exist?
		assertTrue(
			"Modified TargetBean class " + clazzFile + " should exist",
			clazzFile.exists());

		//Are the class attributes there?
		Attributes attributes = null; //new BcelAttributes();
		
		// TODO restore this
//		attributes.setAttributePackages(attribPackages);
		Collection attribs = attributes.getAttributes(targetClass);
		assertEquals("Expected one custom class attribute", 1, attribs.size());

		//Is is what we expect?
		Object o = attribs.iterator().next();
		doAssertOnPerson(o);
	}

	/**
	 * Test attributes on some more complex method signatures
	 *
	 */
	public void testMethods() throws SecurityException, NoSuchMethodException {
		AttributeWriter writer = null; //new BcelAttributeWriter();

		//The class that we will "add an attribute" to programatically.
		Class targetClass = org.springframework.metadata.bcel.TargetBean.class;
		String targetClassname = targetClass.getName();

		//The attribute text to add.
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute ( name=Albert Einstein, age=33, height=150.4 )";

		//Tell the writer what class we are going to modify
		writer.initializeClass(targetClassname);

		Class[] parameterTypes =
			new Class[] {
				String.class,
				int.class,
				float.class,
				Date.class,
				Object.class };

		//Tell the writer to add the method attribute
		Method targetMethod = targetClass.getMethod("doWork", parameterTypes);
		writer.addMethodAttribute(targetMethod, attributeText);

		//Tell the writer to finish its job and write out the new bytecode.
		String eclipseOutputDir = getClassfileDir();
		writer.finishClass(eclipseOutputDir);

		File clazzFile = getClassfile(eclipseOutputDir, targetClass);
		//Does it exist?
		assertTrue(
			"Modified TargetBean class " + clazzFile + " should exist",
			clazzFile.exists());

		Attributes attributes = null; //new BcelAttributes();

		//There should be no class attributes.
		Collection attribs = attributes.getAttributes(targetClass);
		assertEquals(
			"Expected zero custom class attributes",
			0,
			attribs.size());

		//Are the method attributes there?
		Collection methodAttribs = attributes.getAttributes(targetMethod);
		assertEquals(
			"Expected one custom method attribute",
			1,
			methodAttribs.size());

		//Is is what we expect?
		Object o = methodAttribs.iterator().next();
		doAssertOnPerson(o);
	}

	/**
	 * Test attributes on fields.
	 *
	 */
	public void testFields()
		throws NoSuchFieldException, SecurityException, NoSuchMethodException {
		AttributeWriter writer = null; //new BcelAttributeWriter();

		//The class that we will "add an attribute" to programatically.
		Class targetClass = org.springframework.metadata.bcel.TargetBean.class;
		String targetClassname = targetClass.getName();

		//The attribute text to add.
		String attributeText =
			"org.springframework.metadata.support.PersonAttribute ( name=Albert Einstein, age=33, height=150.4 )";

		//Tell the writer what class we are going to modify
		writer.initializeClass(targetClassname);

		//Tell the writer to add the method attribute
		Field targetField = targetClass.getField("height");
		writer.addFieldAttribute(targetField, attributeText);

		//Tell the writer to finish its job and write out the new bytecode.
		String eclipseOutputDir = getClassfileDir();
		writer.finishClass(eclipseOutputDir);

		Attributes attributes = null; //new BcelAttributes();

		//There should be no class attributes.
		Collection attribs = attributes.getAttributes(targetClass);
		assertEquals(
			"Expected zero custom class attributes",
			0,
			attribs.size());

		//There should be no method attributes.
		Method targetMethod = targetClass.getMethod("doReport", null);
		Collection methodAttribs = attributes.getAttributes(targetMethod);
		assertEquals(
			"Expected zero custom method attributes",
			0,
			methodAttribs.size());

		//Are the field attributes there?
		Collection fieldAttribs = attributes.getAttributes(targetField);
		assertEquals(
			"Expected one custom field attribute",
			1,
			fieldAttribs.size());
		Object o = fieldAttribs.iterator().next();
		doAssertOnPerson(o);

	}

	/**
	 * A helper method to get a reference to the classfile.
	 * @param eclipseOutputDir where the .class file is located
	 * @param targetClass the specific class we want a file reference to
	 * @return a file reference specified by the input parameters
	 */
	private File getClassfile(String eclipseOutputDir, Class targetClass) {
		char sep = File.separatorChar;
		//Get a reference to the new modified .class file
		String targetClassName = targetClass.getName().replace('.', sep);
		return new File(eclipseOutputDir + sep + targetClassName + ".class");
	}

	/**
	 * A helper method to get the location of the classfiles
	 * @return
	 */
	public static String getClassfileDir() {
		//Set up some directory locations.
		char sep = File.separatorChar;
		String currentWorkingDir = System.getProperty("user.dir");
		//TODO need to take into account when executing tests outside eclipse
		String eclipseOutputDir = currentWorkingDir + sep + ".testclasses";
		return eclipseOutputDir;
	}

	/**
	 * These assert methods keeps getting done over and over, put them in
	 * one place.
	 * @param o The object created from the AttributeCreator
	 */
	public static void doAssertOnPerson(Object o) {

		assertNotNull("Created attribute should not be null", o);
		assertTrue(
			"Attribute should be of type org.springframework.metadata.support.PersonAttribute",
			(o instanceof PersonAttribute));
		PersonAttribute person = (PersonAttribute) o;
		assertEquals(
			"Person name not as expected",
			"Albert Einstein",
			person.getName());
		assertEquals("Person age not as expected", 33, person.getAge());
		assertEquals(
			"Person height not as expected",
			150.4,
			person.getHeight(),
			0.1);

	}

}
