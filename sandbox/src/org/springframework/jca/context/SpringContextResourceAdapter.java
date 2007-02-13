/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jca.context;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * @author Juergen Hoeller
 * @since 2.1
 */
public class SpringContextResourceAdapter implements ResourceAdapter {

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple context config paths in a single String value.
	 * @see #setContextConfigLocation
	 */
	public static final String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	public static final String DEFAULT_CONTEXT_CONFIG_LOCATION = "classpath:META-INF/applicationContext.xml";


	private String contextConfigLocation = DEFAULT_CONTEXT_CONFIG_LOCATION;

	private ConfigurableApplicationContext applicationContext;


	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	protected String getContextConfigLocation() {
		return this.contextConfigLocation;
	}


	public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
		System.out.println("***** RA STARTED: " + bootstrapContext);
		this.applicationContext = createApplicationContext(bootstrapContext);
	}

	protected ConfigurableApplicationContext createApplicationContext(BootstrapContext bootstrapContext) {
		ResourceAdapterApplicationContext applicationContext =
				new ResourceAdapterApplicationContext(bootstrapContext);
		applicationContext.setClassLoader(getClass().getClassLoader());
		String[] configLocations =
				StringUtils.tokenizeToStringArray(getContextConfigLocation(), CONFIG_LOCATION_DELIMITERS);
		if (configLocations != null) {
			new XmlBeanDefinitionReader(applicationContext).loadBeanDefinitions(configLocations);
		}
		return applicationContext;
	}

	public void stop() {
		System.out.println("***** RA STOPPED");
		this.applicationContext.close();
	}


	public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec)
			throws ResourceException {
	}

	public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
	}

	public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
		return new XAResource[0];
	}

}
