/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.support;

import java.util.HashMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;

/**
 * Bean definition reader that populates a bean factory from JNDI environment
 * variables available to an object running in a J2EE application server.
 * Such a bean factory might be used to parameterize EJBs.
 *
 * <p>Only environment entries with names beginning with "beans." are included.
 *
 * @author Rod Johnson
 */
public class JndiEnvironmentBeanDefinitionReader {
	
	/** Syntax is beans.name.class=Y */
	public static final String BEANS_PREFIX = "beans.";
	
	/** Delimiter for properties */
	public static final String DELIMITER = ".";

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Creates new JNDIBeanFactory
	 * @param root likely to be "java:comp/env"
	 */
	public JndiEnvironmentBeanDefinitionReader(BeanDefinitionRegistry beanFactory, String root) throws BeansException {
		// We'll take everything from the NamingContext and dump it in a
		// Properties object, so that the superclass can efficiently manipulate it
		// after we've closed the context.
		HashMap m = new HashMap();
		
		Context initCtx = null;		
		try {
			initCtx = new InitialContext();
			// Parameterize
			NamingEnumeration bindings = initCtx.listBindings(root);
			
			// Orion 1.5.2 doesn't seem to regard anything under a /
			// as a true subcontext, so we need to search all bindings
			// Not all that fast, but it doesn't matter				
			while (bindings.hasMore()) {
				Binding binding = (Binding) bindings.next();
				logger.debug("Name: " + binding.getName( ));
				logger.debug("Type: " + binding.getClassName( ));
				logger.debug("Value: " + binding.getObject());								
				m.put(binding.getName(), binding.getObject());
			}
			bindings.close();

			PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(beanFactory);
			propReader.registerBeanDefinitions(m, BEANS_PREFIX);
		}
		catch (NamingException ex) {
			logger.debug("----- NO PROPERTIES FOUND " + ex);
		}
		finally {
			try {
				if (initCtx != null) {
					initCtx.close();
				}
			}
			catch (NamingException ex) {
				// IGNORE OR THROW RTE?
			}
		}
	}
	
}
