/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.qdox;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;

import junit.framework.TestCase;

import org.springframework.metadata.Attributes;
import org.springframework.metadata.MetadataCompiler;
import org.springframework.metadata.bcel.AttributeWriterTests;
import org.springframework.metadata.bcel.BcelAttributeWriter;
import org.springframework.metadata.bcel.BcelAttributes;

/**
 * Tests for the QDox based implementation of the MetadataCompiler
 *
 * @author Mark Pollack
 * @since Oct 13, 2003
 */
public class CompilerTests extends TestCase {

	/**
	 * Constructor for CompilerTests.
	 * @param arg0
	 */
	public CompilerTests(String arg0) {
		super(arg0);
	}

	/**
	 * Test attributes on SimpleService.java
	 * @throws Exception
	 */
	public void testSingleFile() throws Exception {
		MetadataCompiler compiler = new QDoxMetadataCompiler();

		compiler.setSourceDirectory(getTestFileDir()
							+ "/org/springframework/metadata/qdox");

		String[] attribPackages = { "org.springframework.metadata.support" };
		compiler.setAttributePackages(attribPackages);
		
		String outputDir = AttributeWriterTests.getClassfileDir();
		compiler.setDestinationDirectory(outputDir);						

		BcelAttributeWriter attributeWriter = null; //new BcelAttributeWriter();
		compiler.setAttributeWriter(attributeWriter);
		
		compiler.compile();

		//Are the class attributes there?
		Attributes attributes = null; //new BcelAttributes();

		// TODO put this back
	//	attributes.setAttributePackages(attribPackages);
		Class targetClass =
			org.springframework.metadata.qdox.SimpleService.class;
		Collection attribs = attributes.getAttributes(targetClass);
		assertEquals("Expected one custom class attribute", 1, attribs.size());

		//Is is what we expect?
		Object o = attribs.iterator().next();
		AttributeWriterTests.doAssertOnPerson(o);

		Class[] parameterTypes = new Class[] { int.class, float.class };

		//Tell the writer to add the method attribute
		Method targetMethod = targetClass.getMethod("doWork", parameterTypes);
		
		//Are the method attributes there?
		Collection methodAttribs = attributes.getAttributes(targetMethod);
		assertEquals(
			"Expected one custom method attribute",
			1,
			methodAttribs.size());

		//Is is what we expect?
		o = methodAttribs.iterator().next();
		AttributeWriterTests.doAssertOnPerson(o);
	}

	/**
	 * A helper method to get the location of the test .java files
	 * @return
	 */
	private static String getTestFileDir() {
		//Set up some directory locations.
		char sep = File.separatorChar;
		String currentWorkingDir = System.getProperty("user.dir");
		//TODO need to take into account when executing tests outside eclipse
		return currentWorkingDir + sep + "test";
	}
}
