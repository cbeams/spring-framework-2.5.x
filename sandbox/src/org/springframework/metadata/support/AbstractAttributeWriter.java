/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.metadata.support;

import org.springframework.metadata.AttributeException;
import org.springframework.metadata.AttributeWriter;
import org.springframework.util.StringUtils;

/**
 * Convenient superclass of AttributeWriter handling
 * attribute packages and offering a utility method to
 * work out the class name given the attribute packages
 * @author Rod Johnson
 */
public abstract class AbstractAttributeWriter implements AttributeWriter {
	
	private String[] attributePackages;

	/**
	 * @see org.springframework.metadata.AttributeWriter#setAttributePackages
	 */
	public void setAttributePackages(String[] packages) {
		this.attributePackages = packages;
	}
	
	protected String[] getAttributePackages() {
		return this.attributePackages;
	}

	/**
	 * @see org.springframework.metadata.AttributeWriter#setClassPath
	 */
	public void setClassPath(String classPath) {
		//throw new UnsupportedOperationException();
	}

	/**
	 * Given a class name probably without a package prefix,
	 * add one if necessary based on the attribute packages of this writer.
	 * @param className the original class name
	 * @return the fully qualified class name
	 */
	protected String fqn(String className) {
		return fqn(className, getAttributePackages());
	}

	/**
	 * Given a class name probably without a package prefix,
	 * add one if necessary based on the given attribute packages.
	 * @param className the original class name
	 * @return the fully qualified class name
	 */
	public static String fqn(String className, String[] attributePackages) {
		// TODO really a hack. where should this be done?
		if (!className.endsWith("Attribute"))
			className += "Attribute";
			
		if (className.indexOf(".") != -1)
			return className;
		for (int i = 0; i < attributePackages.length; i++) {
			String spackage = attributePackages[i];
			String fqn = spackage + "." + className;
			try {
				//System.out.println("Trying attribute package [" + spackage + "] for fqn [" + fqn + "]");
				return Class.forName(fqn, true, Thread.currentThread().getContextClassLoader()).getName();
			}
			catch (ClassNotFoundException ex) {
				// Keep trying
			}
		}
		throw new AttributeException("Can't expand class '" + className + "' using attribute packages " +
		                             "{" + StringUtils.arrayToCommaDelimitedString(attributePackages) + "" +"}");
	}
	
}
