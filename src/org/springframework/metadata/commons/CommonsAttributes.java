/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.metadata.Attributes;

/**
 * Implementation of the Spring Attributes facade for Commons Attributes.
 * Please see 
 * <a href="http://jakarta.apache.org/commons/sandbox/attributes/">
 * the Commons Attributes documentation</a> for information on how to use the attribute compiler.
 * As of December 2003, follow the Javadocs to the AttributeCompiler class to see how the
 * Ant task works. Note that you need to put the follow Jars in your $ANT_HOME/lib directory for
 * the Common Attributes compiler to work:
 * <ul>
 * <li>Commons Attributes compiler jar
 * <li>the xjavadoc Jar (from XDoclet)
 * <li>commons-collection.jar (from Jakarta Commons)
 * </ul>
 * You need to perform the attribute compilation
 * step before compiling your source.
 * <br>
 * See build.xml in the tests for package org.springframework.aop.autoproxy.metadata
 * for an example of the required Ant scripting. The header of this build script
 * includes some quick, and hopefully useful, hints on using Commons Attributes.
 * The source files in the same package (TxClass and TxClassWithClassAttribute)
 * illustrate attribute usage in source files.
 * <br>
 * The Spring project does not support usage of specific attributes implementations.
 * Please refer to the appropriate site and mailing list.
 * @author Rod Johnson
 * @version $Id: CommonsAttributes.java,v 1.1 2003-12-13 00:10:21 johnsonr Exp $
 */
public class CommonsAttributes implements Attributes {
	
	/*
	 * Commons Attributes caches attributes, so we don't need to cache here
	 * as well.
	 */

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.Class)
	 */
	public Collection getAttributes(Class targetClass) {
		return org.apache.commons.attributes.Attributes.getAttributes(targetClass);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.Class, java.lang.Class)
	 */
	public Collection getAttributes(Class targetClass, Class filter) {
		return org.apache.commons.attributes.Attributes.getAttributes(targetClass, filter);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Method)
	 */
	public Collection getAttributes(Method targetMethod) {
		return org.apache.commons.attributes.Attributes.getAttributes(targetMethod);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Method, java.lang.Class)
	 */
	public Collection getAttributes(Method targetMethod, Class filter) {
		return org.apache.commons.attributes.Attributes.getAttributes(targetMethod, filter);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Field)
	 */
	public Collection getAttributes(Field targetField) {
		return org.apache.commons.attributes.Attributes.getAttributes(targetField);
	}

	/**
	 * @see org.springframework.metadata.Attributes#getAttributes(java.lang.reflect.Field, java.lang.Class)
	 */
	public Collection getAttributes(Field targetField, Class filter) {
		return org.apache.commons.attributes.Attributes.getAttributes(targetField, filter);
	}

}
