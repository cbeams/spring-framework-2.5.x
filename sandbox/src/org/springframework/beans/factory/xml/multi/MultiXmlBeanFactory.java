/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml.multi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryLocation;
import org.springframework.beans.factory.support.ClasspathBeanDefinitionRegistryLocation;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * Convenience extension of DefaultListableBeanFactory that reads bean definitions from
 * an XML document. Delegates to DefaultXmlBeanDefinitionReader underneath; effectively
 * equivalent to using a DefaultXmlBeanDefinitionReader for a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the DefaultListableBeanFactory
 * superclass, and relies on the latter's implementation of the BeanFactory
 * interface. It supports singletons, prototypes and references to either of
 * these kinds of bean.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 * @version $Id: MultiXmlBeanFactory.java,v 1.1 2003-12-24 17:17:26 johnsonr Exp $
 */
public class MultiXmlBeanFactory extends DefaultListableBeanFactory {
	
	public static final String DEFAULT_FILE_NAME = "spring.xml";

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
	
	/**
	 * Read in files from classpath
	 *
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
				InputStream is = getClass().getResourceAsStream(resource);
				if (resource == null)
					throw new FatalBeanException("Can't read from resource '" + resource + "'");
				this.reader.loadBeanDefinitions(is, new ClasspathBeanDefinitionRegistryLocation(resource));
				++filesRead;
			}
			System.out.println("Read " + filesRead + " bean definition files");
		}
		catch (IOException ex) {
			throw new FatalBeanException("Cannot load bean factory", ex);
		}
	}

	/**
	 * Create new XmlBeanFactory using java.io to read the XML document
	 * with the given file name.
	 * @param fileName name of the file containing the XML document
	 */
	public MultiXmlBeanFactory(String[] fileNames) throws BeansException {
		for (int i = 0; i < fileNames.length; i++) {
			this.reader.loadBeanDefinitions(fileNames[i]);
		}
	}


}
