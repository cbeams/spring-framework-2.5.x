/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.samples.petportal.domain;

import java.io.Serializable;
import java.net.URL;

/**
 * A PetSite stores the url of a website that is external to
 * the portlet as well as a user-supplied name for the site.
 * This is used to demonstrate a redirect from a portlet.
 * 
 * The websites to be included upon portlet startup are in a file
 * called "petsites.properties" within the WEB-INF directory.
 * 
 * See the bean definitions in "petsites-portlet.xml" for more detail.
 * 
 * @author Mark Fisher
 */
public class PetSite implements Serializable {

	private static final long serialVersionUID = -5203772531388644142L;

	private String name;
	private URL url;
	
	/**
	 * Get the PetSite's user-supplied name.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the PetSite's name.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the PetSite's URL.
	 * @return url
	 */
	public URL getUrl() {
		return url;
	}
	
	/**
	 * Set the PetSite's URL.
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}
	
}
