/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.ant;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.metadata.Attributes;
import org.springframework.metadata.bcel.BcelAttributes;

/**
 * A simple program that looks for metadata on the AnnotatedClass
 * 
 * 
 * @author Mark Pollack
 * @since Oct 16, 2003
 */
public class PersonProgram {



	/**
	 * Create a PersonProgram and extract the attributes.
	 * @param args user args, not used.
	 */
	public static void main(String[] args) throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		PersonProgram p = new PersonProgram();
		p.extractAttributes();
	}

	/**
	 * 
	 */
	private void extractAttributes() throws SecurityException, NoSuchMethodException, ClassNotFoundException {
		Class targetClass = Class.forName("org.springframework.metadata.ant.AnnotatedClass");
		Attributes attributes = null; //new BcelAttributes();
		String[] packages = { "org.springframework.metadata.ant" };
		
		// TODO put this back one day
//		attributes.setAttributePackages(packages);
		
		Collection attribs = attributes.getAttributes(targetClass);
		System.out.println("Found " + attribs.size() + " class attributes");
		System.out.println("Attribute: " + attribs.iterator().next().toString());
		
		Method targetMethod = targetClass.getMethod("doWork", null);
		attribs = attributes.getAttributes(targetMethod);
		System.out.println("Found " + attribs.size() + " attributes on method doWork()");
		System.out.println("Attribute: " + attribs.iterator().next().toString());		
		
	}
}
