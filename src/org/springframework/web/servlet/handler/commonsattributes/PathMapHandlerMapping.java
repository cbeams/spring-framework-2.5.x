/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.handler.commonsattributes;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.attributes.AttributeIndex;
import org.apache.commons.attributes.Attributes;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * Implementation of the HandlerMapping interface that recognizes Commons Attributes
 * metadata attributes of type PathMap on application Controllers and automatically
 * wires them into the current servlet's WebApplicationContext.
 * <p>
 * Controllers must have class attributes of the form:
 * <code>
 * &64;org.springframework.web.servlet.handler.commonsattributes.PathMap("/path.cgi")
 * </code>
 * <br>The path must be mapped to the relevant Spring DispatcherServlet in /WEB-INF/web.xml.
 * It's possible to have multiple PathMap attributes on the one controller class.
 * <p>To use this feature, you must compile application classes with Commons Attributes,
 * and run the Commons Attributes indexer tool on your application classes, which must
 * be in a Jar rather than in WEB-INF/classes.
 * <p>Controllers instantiated by this class may have dependencies on middle tier
 * objects, expressed via JavaBean properties or constructor arguments. These will
 * be resolved automatically.
 * <p>You will normally use this HandlerMapping with at most one DispatcherServlet in your web 
 * application. Otherwise you'll end with one instance of the mapped controller for
 * each DispatcherServlet's context. You <i>might</i> want this--for example, if
 * one's using a .pdf mapping and a PDF view, and another a JSP view, or if
 * using different middle tier objects, but should understand the implications. All
 * Controllers with attributes will be picked up by each DispatcherServlet's context.
 * @author Rod Johnson
 * @version $Id: PathMapHandlerMapping.java,v 1.1 2003-12-24 17:16:55 johnsonr Exp $
 */
public class PathMapHandlerMapping extends AbstractUrlHandlerMapping {
	
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
				
				String beanName = handlerClass.getName();
				Object handler = getApplicationContext().registerBeanOfClass(beanName, handlerClass, true);
				
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
			// Shouldn't happen: Commons Attributes gave us the classname
			throw new ApplicationContextException("Failed to load a class returned in an attribute index: internal error in Commons Attributes indexing?", ex);
		}
	}

	/**
	 * Use Commons Attributes AttributeIndex to get a Collection of FQNs of
	 * classes with the required PathMap attribute. Protected so that it can
	 * be overridden during testing.
	 */
	protected Collection getClassNamesWithPathMapAttributes() {
		try {
			AttributeIndex ai = new AttributeIndex(getClass().getClassLoader());
			return ai.getClassesWithAttribute(PathMap.class);
		}
		catch (Exception ex) {
			throw new ApplicationContextException("Failed to load Commons Attributes attribute index", ex);
		}
	}
	
	/**
	 * Use Commons Attributes to find PathMap attributes for the given class.
	 * We know there's at least one, as the getClassNamesWithPathMapAttributes
	 * method return this class name.
	 */
	protected PathMap[] getPathMapAttributes(Class handlerClass) {
		Collection atts =  Attributes.getAttributes(handlerClass, PathMap.class);
		return (PathMap[]) atts.toArray(new PathMap[atts.size()]);
	}

}
