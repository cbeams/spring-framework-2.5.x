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

/**
 * Attribute to be used on Controller classes to allow for automatic
 * URL mapping without web controllers being defined as beans in an 
 * XML bean definition file. The path map should be the path in the current
 * application, such as /foo.cgi. If there is no leading /, one will be
 * prepended.
 * Application code must use the Commons Attributes indexer
 * tool to use this option.
 * @author Rod Johnson
 * 
 * @@org.apache.commons.attributes.Indexed()
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
