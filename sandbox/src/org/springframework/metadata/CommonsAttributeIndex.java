/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.attributes.AttributeIndex;
import org.springframework.metadata.AttributeException;

/**
 * 
 * @author Rod Johnson
 */
public class CommonsAttributeIndex implements org.springframework.metadata.AttributeIndex {
	
	private AttributeIndex commonsIndex;
	
	public CommonsAttributeIndex() throws AttributeException {
		try {
			commonsIndex = new AttributeIndex(getClass().getClassLoader());
		}
		catch (Exception ex) {
			throw new AttributeException("Cannot index attributes", ex);
		}
	}

	/**
	 * @see org.springframework.metadata.AttributeIndex#getClassesWithAttribute(java.lang.Class)
	 */
	public Class[] getClassesWithAttribute(Class attributeClass) {
		Collection classNames = commonsIndex.getClassesWithAttribute(attributeClass); 
		Class[] classes = new Class[classNames.size()];
		int i = 0;
		// Name of the current class
		String className = null;
		try {
			for (Iterator itr = classNames.iterator(); itr.hasNext();) {
				className = (String) itr.next();
				Class clazz = Class.forName(className);
				classes[i++] = clazz;
			}
		}
		catch (ClassNotFoundException ex) {
			throw new AttributeException("Cannot find class '" + className + "' looking in attribute index", ex);
		}
		return classes;
	}

}
