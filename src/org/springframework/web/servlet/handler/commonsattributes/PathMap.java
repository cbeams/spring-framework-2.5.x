/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.handler.commonsattributes;

/**
 * Attribute to be used on Controller classes to allow for automatic
 * URL mapping without web controllers being defined as beans in an 
 * XML bean definition file. The path map should be the path in the current
 * application, such as /foo.cgi. If there is no leading /, one will be
 * prepended.
 * Application code must use the Commons Attributes indexer
 * tool to use this option.
 * @author Rod Johnson
 * @version $Id: PathMap.java,v 1.1 2003-12-24 17:16:55 johnsonr Exp $
 * 
 * @org.apache.commons.attributes.Indexed()
 */
public class PathMap {
	
	/**
	 * NB: The Indexed attribute on this class is required. Thus the Spring
	 * Jar must be built including a Commons Attributes attribute compilation step
	 * for this class.
	 */
	
	private final String url;
	
	public PathMap(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

}
