/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata;


/**
 * 
 * @author Rod Johnson
 * @version $Id: AttributeIndex.java,v 1.1 2003-12-24 17:17:26 johnsonr Exp $
 */
public interface AttributeIndex {
	
	/**
	 * 
	 * @param attributeClass
	 * @return classes with this attribute. Never returns null.
	 */
	public Class[] getClassesWithAttribute (Class attributeClass);

}
