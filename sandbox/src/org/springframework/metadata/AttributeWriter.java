/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The interface for adding metadata information to a target class.
 * @author Mark Pollack
 * @since Sep 28, 2003
 */
public interface AttributeWriter {

	/**
	 * Set the list of package names to prepend to javadoc comments
	 * in order to create a fully qualified name from the javadoc
	 * tag name.
	 * @param packages
	 */
	void setAttributePackages(String[] packages);
	
	/**
	 * Set the class path to use when trying to locate resolve classes.
	 * Not all implementations will need to call this method.
	 * @param classPath A File.pathSeparator separated list of .jar
	 * and directories.  
	 */
	void setClassPath(String classPath);

	/**
	 * 
	 * Set the name of the target class that will be enchanced
	 * with metadata.
	 * @param targetClass name of the target class that will be 
	 * enhanced with metadata.
	 */
	void initializeClass(String targetClass);

	/**
	 * Add the given attribute metadata information to the target
	 * class at the class level.
	 * @param attributeText The textual description of the attribute
	 * that will be associated with the target class.
	 */
	void addClassAttribute(String attributeText);
	
	/**
	 * Add the given attribute metadata information to the target class
	 * at the method level
	 * @param method The method to add an attribute to.
	 * @param attributeText The textual description of the attribute
	 * that will be associated with the target method.
	 */
	void addMethodAttribute(Method method, String attributeText);
	
	/**
	 * Add the given attribute metadata information to the target class
	 * at the field level.
	 * @param targetField The field to add an attribute to.
	 * @param attributeText The textual descriptino of the attribute that will
	 * be associate with the target field.
	 */
	void addFieldAttribute(Field targetField, String attributeText);
	
	/**
	 * Write the attribute metadata information to this directory.
	 * @param destinationDirectory where the metadata information is
	 * to be stored.
	 */
	void finishClass(String destinationDirectory);




		
}
