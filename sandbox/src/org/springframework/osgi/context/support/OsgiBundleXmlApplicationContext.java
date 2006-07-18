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
package org.springframework.osgi.context.support;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.util.Assert;

/**
 * Application context backed by an OSGi bundle. Will use the
 * bundle classpath for resource loading for any unqualified resource string.
 * 
 * Also understands the "bundle:" resource prefix for explicit loading
 * of resources from the bundle. When the bundle prefix is used the 
 * target resource must be contained within the bundle (or attached 
 * fragments), the classpath is not searched.
 * 
 * This application context will publish itself as a service using the
 * service name "&lt;bundle-symbolic-name&gt-springApplicationContext".
 * To specify an alternate service name, use the 
 * org.springframework.context.service.name manifest header in the bundle
 * manifest. For example:
 * 
 * <code>
 * org.springframework.context.service.name=myApplicationContextService
 * </code>
 * 
 * TODO: provide means to access OSGi services etc. through this 
 * application context?
 * TODO: think about whether restricting config files to bundle: is the right
 * thing to do
 * TODO: listen to parent application context service, automatically rebind and
 * refresh if it goes away and comes back (the logic can be placed inside the lookupParentAppContext proxy).
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class OsgiBundleXmlApplicationContext extends
		AbstractXmlApplicationContext {
	
	public  static final String BUNDLE_URL_PREFIX = "bundle:";
	public  static final String APPLICATION_CONTEXT_SERVICE_NAME_HEADER = "org.springframework.context.service.name";
	private static final String APPLICATION_CONTEXT_SERVICE_POSTFIX = "-springApplicationContext";
	private static final char   PREFIX_SEPARATOR = ':';
	private static final String ABSOLUTE_PATH_PREFIX = "/";
	private static final String OSGI_SPRING_HANDLERS_LOCATION = "org/springframework/osgi/handlers/spring.handlers";
	
	private Bundle osgiBundle;
	private BundleContext osgiBundleContext;
	private String[] configLocations;
	private ServiceRegistration serviceRegistration;
	
	public OsgiBundleXmlApplicationContext(BundleContext aBundleContext, String[] configLocations) {
		this(null, aBundleContext, configLocations);
	}

	public OsgiBundleXmlApplicationContext(ApplicationContext parent, BundleContext aBundleContext, String[] configLocations) {
		super(parent);

		this.configLocations = configLocations;
		this.osgiBundleContext = aBundleContext;
		this.osgiBundle = this.osgiBundleContext.getBundle();
		this.setClassLoader(createBundleClassLoader(this.osgiBundle));
		refresh();

		publishContextAsOsgiService();
	}

	protected String[] getConfigLocations() {
		return this.configLocations;
	}
	
	/**
	 * Get the OSGi BundleContext for this application context
	 */
	public BundleContext getBundleContext() {
		return this.osgiBundleContext;		
	}

	/**
	 * We can't look in META-INF across bundles when using osgi, so we need to
	 * change the default namespace handler (spring.handlers) location with a 
	 * custom resolver.
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		beanDefinitionReader.setNamespaceHandlerResolver(
				new DefaultNamespaceHandlerResolver(getClassLoader(),OSGI_SPRING_HANDLERS_LOCATION));
	}

	protected void publishContextAsOsgiService() {
		Dictionary serviceProperties = new Properties();
		serviceProperties.put(APPLICATION_CONTEXT_SERVICE_NAME_HEADER, getServiceName());
		this.serviceRegistration = 
			this.osgiBundleContext.registerService(
				new String[] {ApplicationContext.class.getName()},
				this,
				serviceProperties);
	}
	
	public void close() {
		if (this.serviceRegistration != null) {
			this.serviceRegistration.unregister();
		}
		super.close();
	}
	
	/**
	 * The name we will use when publishing this application context as an OSGi service.
	 * If the APPLICATION_CONTEXT_SERVICE_NAME_HEADER manifest header is present, we
	 * use the user given name, otherwise we derive a name from the bundle symbolic name.
	 */
	protected String getServiceName() {
		String userSpecifiedName = (String) this.osgiBundle.getHeaders().get(APPLICATION_CONTEXT_SERVICE_NAME_HEADER);
		if (userSpecifiedName != null) {
			return userSpecifiedName;
		}
		else {
			return this.osgiBundle.getSymbolicName() + APPLICATION_CONTEXT_SERVICE_POSTFIX;
		}		
	}
	
	/**
	 * Implementation of getResource that delegates to the bundle for
	 * any unqualified resource reference or a reference starting with
	 * "bundle:"
	 */
	public Resource getResource(String location) {
		Assert.notNull(location, "location is required");
		if (location.startsWith(BUNDLE_URL_PREFIX)) {
			return getResourceFromBundle(location.substring(BUNDLE_URL_PREFIX.length()));
		} 
		else if (location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
			return getResourceFromBundleClasspath(location.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length()));
		}
		else if (isRelativePath(location)){ 
			return getResourceFromBundleClasspath(location);
		}
		else {
			return super.getResource(location);			
		}
	}

	/**
	 * Resolves a resource from *this bundle only*. Only the bundle and its
	 * attached fragments are searched for the given resource.
	 * 
	 * @param bundleRelativePath
	 * @return
	 */
	protected Resource getResourceFromBundle(String bundleRelativePath) {
		return new UrlResource(this.osgiBundle.getEntry(bundleRelativePath));
	}

	/**
	 * Resolves a resource from the bundle's classpath. This will find resources
	 * in this bundle and also in imported packages from other bundles.
	 * 
	 * @param bundleRelativePath
	 * @return
	 */
	protected Resource getResourceFromBundleClasspath(String bundleRelativePath) {
		return new UrlResource(this.osgiBundle.getResource(bundleRelativePath));
	}

	protected boolean isRelativePath(String locationPath) {
		return ((locationPath.indexOf(PREFIX_SEPARATOR) == -1) &&
				 !locationPath.startsWith(ABSOLUTE_PATH_PREFIX));
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.context.support.AbstractApplicationContext#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		super.postProcessBeanFactory(beanFactory);
		beanFactory.addBeanPostProcessor(new BundleContextAwareProcessor(this.osgiBundleContext));
		beanFactory.ignoreDependencyInterface(BundleContextAware.class);
	}

	private ClassLoader createBundleClassLoader(Bundle bundle) {
		return new BundleClassLoader(bundle);
	}

	private static class BundleClassLoader extends ClassLoader {
		
		private Bundle backingBundle;
		
		public BundleClassLoader(Bundle aBundle) {
			this.backingBundle = aBundle;
		}
		
		protected Class findClass(String name) throws ClassNotFoundException {
			return this.backingBundle.loadClass(name);
		}
		
		protected URL findResource(String name) {
			return this.backingBundle.getResource(name);
		}
		
		protected Enumeration findResources(String name) throws IOException {
			return this.backingBundle.getResources(name);
		}
	}
}
