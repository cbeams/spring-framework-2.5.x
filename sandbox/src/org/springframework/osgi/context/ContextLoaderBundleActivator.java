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
 *
 * Created on 23-Jan-2006 by Adrian Colyer
 */
package org.springframework.osgi.context;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.context.support.DefaultOsgiBundleXmlApplicationContextFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContextFactory;
import org.springframework.osgi.service.OsgiServiceUtils;
import org.springframework.util.StringUtils;

/**
 * OSGi bundle activator class to be used as the bundle activator 
 * for OSGi bundles using Spring services. When the bundle is activated
 * it creates an application context and destroys it when the bundled
 * is stopped. 
 *
 * By default the ContextLoaderBundleActivator will look for an application
 * context file in the location /META-INF/&lt;bundle-symbolic-name&gt;-context.xml.
 * You can override this default behaviour by adding a bundle manifest header
 * entry of the form
 * 
 * Spring-Context: &lt;comma-delimited list of context file locations&gt; 
 * 
 * The manifest entry may contain any number of resource paths, separated by any
 * number of commas and spaces.
 * 
 * TODO: support parent application context via additional header giving name
 *       of parent application context service (by default this will be 
 *       <bundle-symbolic-name>-springApplicationContext.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class ContextLoaderBundleActivator implements BundleActivator {

	private static final String CONTEXT_LOCATION_HEADER = "Spring-Context";
	private static final String PARENT_CONTEXT_SERVICE_NAME_HEADER = "Spring-Parent-Context";
	private static final String CONTEXT_LOCATION_DELIMITERS = ", ";
	private static final String DEFAULT_CONTEXT_PREFIX = "/META-INF/";
	private static final String DEFAULT_CONTEXT_POSTFIX = "-context.xml";
	
	private OsgiBundleXmlApplicationContextFactory contextFactory = new DefaultOsgiBundleXmlApplicationContextFactory();
	private ConfigurableApplicationContext applicationContext;
	private ServiceReference parentServiceReference;
	
	/**
	 * BundleActivator.start
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Bundle myBundle = bundleContext.getBundle();
		String[] applicationContextLocations = getApplicationContextLocations(myBundle);
		ApplicationContext parent = getParentApplicationContext(bundleContext);
		this.applicationContext = this.contextFactory.createApplicationContext(parent, bundleContext,applicationContextLocations);		
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (this.applicationContext != null) {
			this.applicationContext.close();
		}
		if (this.parentServiceReference != null) {
			bundleContext.ungetService(this.parentServiceReference);
		}
	}
	
	
	protected ApplicationContext getParentApplicationContext(BundleContext context) {
		String parentContextServiceName = (String) context.getBundle().getHeaders().get(PARENT_CONTEXT_SERVICE_NAME_HEADER);
		if (parentContextServiceName == null) {
			return null;
		} 
		else {
			// try to find the service
			String filter = "(" + OsgiBundleXmlApplicationContext.APPLICATION_CONTEXT_SERVICE_NAME_HEADER +
            					"=" + parentContextServiceName + ")";
			ServiceReference ref = OsgiServiceUtils.getService(context,ApplicationContext.class, filter);
			ApplicationContext parent = (ApplicationContext) context.getService(ref);
			// TODO: register as service listener..., probably in a proxy to the app context
			// that we create here and return instead.
			
			return parent;
		}
	}

	/**
	 * Retrieves the org.springframework.context manifest header attribute
	 * and parses it to create a String[] of resource names for creating
	 * the application context.
	 * 
	 * If the org.springframework.context header is not present, the
	 * default <bundle-symbolic-name>-context.xml file will be returned.
	 */
	protected String[] getApplicationContextLocations(Bundle bundle) {
		Dictionary manifestHeaders = bundle.getHeaders();
		String contextLocationsHeader = (String) manifestHeaders.get(CONTEXT_LOCATION_HEADER);
		if (contextLocationsHeader != null) {
			// (Dictionary does not offer a "containsKey" operation)
			return addBundlePrefixTo(
					StringUtils.tokenizeToStringArray(contextLocationsHeader, CONTEXT_LOCATION_DELIMITERS)); 
		}
		else {
			String defaultName = DEFAULT_CONTEXT_PREFIX + bundle.getSymbolicName() + DEFAULT_CONTEXT_POSTFIX;
			return addBundlePrefixTo(new String[] {defaultName});
		}
	}

	/**
	 * add the "bundle:" prefix to the resource location paths in the given argument.
	 * This ensures that only this bundle will be searched for matching resources.
	 * 
	 * Modifies the argument in place and returns it.
	 */
	private String[] addBundlePrefixTo(String[] resourcePaths) {
		for (int i = 0; i < resourcePaths.length; i++) {
			resourcePaths[i] = OsgiBundleXmlApplicationContext.BUNDLE_URL_PREFIX + resourcePaths[i];
		}
		return resourcePaths;
	}
	
	// for testing...
	protected void setApplicationContext(ConfigurableApplicationContext context) {
		this.applicationContext = context;
	}
	
	// for testing...
	protected void setParentServiceReference(ServiceReference ref) {
		this.parentServiceReference = ref;
	}
	
	// for testing...
	protected void setApplicationContextFactory(OsgiBundleXmlApplicationContextFactory factory) {
		this.contextFactory = factory;
	}
}
