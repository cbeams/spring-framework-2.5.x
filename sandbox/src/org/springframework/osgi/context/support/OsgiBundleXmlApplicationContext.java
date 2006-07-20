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
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.AbstractRefreshableOsgiBundleApplicationContext;

/**
 * Application context backed by an OSGi bundle.
 * 
 * This application context will publish itself as a service using the service
 * name "&lt;bundle-symbolic-name&gt-springApplicationContext". To specify an
 * alternate service name, use the org.springframework.context.service.name
 * manifest header in the bundle manifest. For example:
 * 
 * <code>
 * org.springframework.context.service.name=myApplicationContextService
 * </code>
 * 
 * TODO: provide means to access OSGi services etc. through this application
 * context?
 * 
 * TODO: think about whether restricting config files to bundle: is the right
 * thing to do
 * 
 * TODO: listen to parent application context service, automatically rebind and
 * refresh if it goes away and comes back (Costin: the logic can be placed
 * inside the lookupParentAppContext proxy).
 * 
 * TODO: add default locations (take intoa ccount also namespaces)
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * @since 2.0
 */
public class OsgiBundleXmlApplicationContext extends AbstractRefreshableOsgiBundleApplicationContext {

	/**
	 * Default config location for the root context (given as header inside the
	 * OSGi bundle)
	 */
	public static final String APPLICATION_CONTEXT_SERVICE_NAME_HEADER = "org.springframework.context.service.name";

	/** Default application context suffix */
	private static final String APPLICATION_CONTEXT_SERVICE_POSTFIX = "-springApplicationContext";

	/** OSGi namespace handlers * */
	private static final String OSGI_SPRING_HANDLERS_LOCATION = "org/springframework/osgi/handlers/spring.handlers";

	/** Used for publishing the app context * */
	private ServiceRegistration serviceRegistration;

	public OsgiBundleXmlApplicationContext(BundleContext aBundleContext, String[] configLocations) {
		this(null, aBundleContext, configLocations);
	}

	public OsgiBundleXmlApplicationContext(ApplicationContext parent, BundleContext aBundleContext,
			String[] configLocations) {
		super(parent);

		setConfigLocations(configLocations);
		setBundleContext(aBundleContext);
		refresh();

		publishContextAsOsgiService();
	}

	/**
	 * Loads the bean definitions via an XmlBeanDefinitionReader.
	 * 
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * We can't look in META-INF across bundles when using osgi, so we need to
	 * change the default namespace handler (spring.handlers) location with a
	 * custom resolver.
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		beanDefinitionReader.setNamespaceHandlerResolver(new DefaultNamespaceHandlerResolver(getClassLoader(),
				OSGI_SPRING_HANDLERS_LOCATION));
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * <p>
	 * The lifecycle of the bean factory is handled by the refreshBeanFactory
	 * method; therefore this method is just supposed to load and/or register
	 * bean definitions.
	 * <p>
	 * Delegates to a ResourcePatternResolver for resolving location patterns
	 * into Resource instances.
	 * 
	 * @throws org.springframework.beans.BeansException in case of bean
	 *             registration errors
	 * @throws java.io.IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			for (int i = 0; i < configLocations.length; i++) {
				reader.loadBeanDefinitions(configLocations[i]);
			}
		}
	}

	protected void publishContextAsOsgiService() {
		Dictionary serviceProperties = new Properties();
		serviceProperties.put(APPLICATION_CONTEXT_SERVICE_NAME_HEADER, getServiceName());
		this.serviceRegistration = getBundleContext().registerService(
				new String[] { ApplicationContext.class.getName() }, this, serviceProperties);
	}

	/**
	 * The name we will use when publishing this application context as an OSGi
	 * service. If the APPLICATION_CONTEXT_SERVICE_NAME_HEADER manifest header
	 * is present, we use the user given name, otherwise we derive a name from
	 * the bundle symbolic name.
	 */
	protected String getServiceName() {
		String userSpecifiedName = (String) getBundle().getHeaders().get(APPLICATION_CONTEXT_SERVICE_NAME_HEADER);
		if (userSpecifiedName != null) {
			return userSpecifiedName;
		}
		else {
			return getBundle().getSymbolicName() + APPLICATION_CONTEXT_SERVICE_POSTFIX;
		}
	}

	public void close() {
		if (this.serviceRegistration != null) {
			this.serviceRegistration.unregister();
		}
		super.close();
	}
}
