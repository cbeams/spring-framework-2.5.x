/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Concrete implementation of the {@link ContextLoader} strategy which loads a
 * {@link GenericApplicationContext} from the <em>locations</em> in the
 * supplied {@link ContextConfigurationAttributes configuration attributes}.
 * The locations must refer to XML based configuration files.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.2
 */
public class GenericXmlContextLoader implements ContextLoader {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	protected static final Log LOG = LogFactory.getLog(GenericXmlContextLoader.class);

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CLASS VARIABLES ----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final ContextConfigurationAttributes configAttributes;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public GenericXmlContextLoader(final ContextConfigurationAttributes configAttributes) {

		this.configAttributes = configAttributes;
	}

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Loads a {@link GenericApplicationContext} from the <em>locations</em>
	 * provided by the
	 * {@link ContextConfigurationAttributes configuration attributes}.
	 *
	 * @see org.springframework.test.ContextLoader#loadContext()
	 * @see GenericApplicationContext
	 * @see XmlBeanDefinitionReader
	 */
	@Override
	public ConfigurableApplicationContext loadContext() throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading ApplicationContext for ContextConfigurationAttributes [" + this.configAttributes + "].");
		}

		final GenericApplicationContext context = new GenericApplicationContext();
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(this.configAttributes.getLocations());
		context.refresh();

		return context;
	}

	// ------------------------------------------------------------------------|
}
