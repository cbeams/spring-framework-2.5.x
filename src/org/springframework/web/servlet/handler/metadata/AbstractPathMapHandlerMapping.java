/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.handler.metadata;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.config.ConfigurableApplicationContext;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * Abstract mplementation of the HandlerMapping interface that recognizes 
 * metadata attributes of type PathMap on application Controllers and automatically
 * wires them into the current servlet's WebApplicationContext.
 *
 * <p>The path must be mapped to the relevant Spring DispatcherServlet in /WEB-INF/web.xml.
 * It's possible to have multiple PathMap attributes on the one controller class.
 *
 * <p>Controllers instantiated by this class may have dependencies on middle tier
 * objects, expressed via JavaBean properties or constructor arguments. These will
 * be resolved automatically.
 *
 * <p>You will normally use this HandlerMapping with at most one DispatcherServlet in your
 * web application. Otherwise you'll end with one instance of the mapped controller for
 * each DispatcherServlet's context. You <i>might</i> want this -- for example, if
 * one's using a .pdf mapping and a PDF view, and another a JSP view, or if
 * using different middle tier objects, but should understand the implications. All
 * Controllers with attributes will be picked up by each DispatcherServlet's context.
 *
 * @author Rod Johnson
 * @version $Id: AbstractPathMapHandlerMapping.java,v 1.2 2003-12-30 01:16:35 jhoeller Exp $
 */
public abstract class AbstractPathMapHandlerMapping extends AbstractUrlHandlerMapping {
	
	/**
	 * Look for all classes with a PathMap class attribute, instantiate them in
	 * the owning ApplicationContext and register them as MVC handlers usable
	 * by the current DispatcherServlet.
	 * @see org.springframework.context.support.ApplicationObjectSupport#initApplicationContext()
	 */
	public void initApplicationContext() throws ApplicationContextException {
		try {
			logger.info("Looking for attribute-defined URL mappings in application context: " + getApplicationContext());
			
			Collection names = getClassNamesWithPathMapAttributes();
			logger.info("Found " + names.size() + " attribute-targeted handlers");				
			
			// For each classname returned by the Commons Attribute indexer
			for (Iterator itr = names.iterator(); itr.hasNext();) {
				String handlerClassName = (String) itr.next();
				Class handlerClass = Class.forName(handlerClassName);
				Object handler = getApplicationContext().autowire(handlerClass);
				if (getApplicationContext() instanceof ConfigurableApplicationContext) {
					ConfigurableListableBeanFactory beanFactory =
							((ConfigurableApplicationContext) getApplicationContext()).getBeanFactory();
					beanFactory.registerSingleton(handlerClassName, handler);
				}

				// There may be multiple paths mapped to this class
				PathMap[] pathMaps = getPathMapAttributes(handlerClass);
				for (int i = 0; i < pathMaps.length; i++) {				
					PathMap pathMap = pathMaps[i];
					String path = pathMap.getUrl();
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
					
					logger.info("Mapping path [" + path + "] to class with name '" + handlerClassName + "'");
					registerHandler(path, handler);
				}
			}
		}
		catch (ClassNotFoundException ex) {
			// Shouldn't happen: Attributes API gave us the classname
			throw new ApplicationContextException("Failed to load a class returned in an attribute index: internal error in Commons Attributes indexing?", ex);
		}
	}

	/**
	 * Use an attribute index to get a Collection of FQNs of
	 * classes with the required PathMap attribute.
	 */
	protected abstract Collection getClassNamesWithPathMapAttributes();

	/**
	 * Use Attributes API to find PathMap attributes for the given class.
	 * We know there's at least one, as the getClassNamesWithPathMapAttributes
	 * method return this class name.
	 */
	protected abstract PathMap[] getPathMapAttributes(Class handlerClass);

}
