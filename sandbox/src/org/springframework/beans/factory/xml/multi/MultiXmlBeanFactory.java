/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml.multi;

import java.io.IOException;
import java.util.Enumeration;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rod Johnson
 */
public class MultiXmlBeanFactory extends DefaultListableBeanFactory {
	
	public static final String DEFAULT_FILE_NAME = "spring.xml";

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
	
	/**
	 * Read in files from classpath.
	 */
	public MultiXmlBeanFactory() {
		this("multi.xml");
		//this(DEFAULT_FILE_NAME);
	}
	
	public MultiXmlBeanFactory(String name) {
		try {
			int filesRead = 0;
			Enumeration enum = getClass().getClassLoader().getResources(name);
			while (enum.hasMoreElements()) {
				String resource = (String) enum.nextElement();
				System.out.println("Reading resource " + resource);
				if (resource == null) {
					throw new FatalBeanException("Can't read from resource '" + resource + "'");
				}
				this.reader.loadBeanDefinitions(new ClassPathResource(resource, getClass()));
				++filesRead;
			}
			System.out.println("Read " + filesRead + " bean definition files");
		}
		catch (IOException ex) {
			throw new FatalBeanException("Cannot load bean factory", ex);
		}
	}

}
