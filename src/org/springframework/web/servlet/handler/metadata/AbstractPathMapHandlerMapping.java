/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.servlet.handler.metadata;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * Abstract implementation of the HandlerMapping interface that recognizes 
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
 * @version $Id: AbstractPathMapHandlerMapping.java,v 1.7 2004-05-26 10:48:57 jhoeller Exp $
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
				if (!(getApplicationContext() instanceof ConfigurableApplicationContext)) {
					throw new ApplicationContextException("AbstractPathMapHandlerMapping needs to run in a ConfigurableApplicationContext");
				}
				ConfigurableListableBeanFactory beanFactory =
						((ConfigurableApplicationContext) getApplicationContext()).getBeanFactory();

				// Autowire the given handler class via AutowireCapableBeanFactory.
				// Either autowires a constructor or by type, depending on the
				// constructors available in the given class.
				Object handler = beanFactory.autowire(handlerClass, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
				
				// We now have an "autowired" handler, that may reference beans in the
				// application context. We now add the new handler to the factory.
				// This isn't necessary for the handler to work, but is useful if we want
				// to enumerate controllers in the factory etc.
				beanFactory.registerSingleton(handlerClassName, handler);

				// There may be multiple paths mapped to this handler,
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
			// Shouldn't happen: Attributes API gave us the class name.
			throw new ApplicationContextException("Failed to load a class returned in an attribute index: " +
																						"internal error in Commons Attributes indexing?", ex);
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
