/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

/**
 * BeanDefinitionRegistryLocation for resoures loaded from the file system.
 * @author Rod Johnson
 * @version $Id: FileBeanDefinitionRegistryLocation.java,v 1.1 2003-12-19 15:49:58 johnsonr Exp $
 */
public class FileBeanDefinitionRegistryLocation implements BeanDefinitionRegistryLocation {

	private final String path; 
	
	public FileBeanDefinitionRegistryLocation(String path) {
		this.path = path;
	}
	
	public String toString() {
		return "File path='" + path + "'";
	}
}
