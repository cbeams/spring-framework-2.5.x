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

package org.springframework.web.servlet.view.tiles;

import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.TilesUtil;
import org.apache.struts.tiles.TilesUtilImpl;
import org.apache.struts.tiles.xmlDefinition.I18nFactorySet;

import org.springframework.context.ApplicationContextException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;

/**
 * Helper class to configure Tiles for the Spring Framework (see
 * <a href="http://jakarta.apache.org/struts">http://jakarta.apache.org/struts</a>
 * for more information about Tiles, which basically is a templating mechanism
 * for JSP-based web applications).
 *
 * <p>The TilesConfigurer simply configures tiles using a set of files containing
 * definitions. The rest is done by an appropriate Resolver which could for instance
 * be the {@link org.springframework.web.servlet.view.InternalResourceViewResolver
 * InternalResourceViewResolver} or the
 * {@link org.springframework.web.servlet.view.ResourceBundleViewResolver
 * ResourceBundleViewResolver}. Usage of the TilesConfigurer is done as follows:
 *
 * <p>
 * <pre>
 *  &lt;bean id="tilesConfigurer" class="org.springframework.web.servlet.view.tiles.TilesConfigurer"&gt;
 *       &lt;property name="definitions"&gt;
 *           &lt;list&gt;
 *               &lt;value&gt;/WEB-INF/defs/general.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/widgets.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/administrator.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/customer.xml&lt;/value&gt;
 *               &lt;value&gt;/WEB-INF/defs/templates.xml&lt;/value&gt;
 *           &lt;/list&gt;
 *       &lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 * </p>
 *
 * <p>The values in the list are the actual files containing the definitions.
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @see TilesView
 */
public class TilesConfigurer extends WebApplicationObjectSupport {

	/** factory class for Tiles */
	private Class factoryClass = I18nFactorySet.class;

	/** validate the Tiles definitions? */
	private boolean validateDefinitions = true;

	/** definition URLs mapped to descriptions */
	private String[] definitions;

	/**
	 * Set the factory class for Tiles. Default is I18nFactorySet.
	 * @see org.apache.struts.tiles.xmlDefinition.I18nFactorySet
	 */
	public void setFactoryClass(Class factoryClass) {
		this.factoryClass = factoryClass;
	}

	/**
	 * Validate the Tiles definitions? Default is false.
	 * @param validateDefinitions <code>true</code> to validate,
	 * <code>false</code> otherwise
	 */
	public void setValidateDefinitions(boolean validateDefinitions) {
		this.validateDefinitions = validateDefinitions;
	}

	/**
	 * Set the Tiles definitions, i.e. the list of files.
	 * @param definitions the files containing the definitions
	 */
	public void setDefinitions(String[] definitions) {
		this.definitions = definitions;
	}

	/**
	 * Initialization of the Tiles definition factory.
	 * @throws ApplicationContextException if an error occurs
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		try {
			logger.info("Tiles: initializion started");

			// initialize the definitions factory configuration
			DefinitionsFactoryConfig factoryConfig = new DefinitionsFactoryConfig();
			factoryConfig.setFactoryClassname(this.factoryClass.getName());
			factoryConfig.setParserValidate(this.validateDefinitions);

			if (this.definitions != null) {
				String defs = StringUtils.arrayToCommaDelimitedString(this.definitions);
				logger.info("Tiles: adding definitions [" + defs + "]");
				factoryConfig.setDefinitionConfigFiles(defs);
			}

			// initialize the definitions factory
			DefinitionsFactory factory = TilesUtil.createDefinitionsFactory(getServletContext(), factoryConfig);
			getWebApplicationContext().getServletContext().setAttribute(
				TilesUtilImpl.DEFINITIONS_FACTORY, factory);
			

			logger.info("Tiles: initialization done");
		}
		catch (DefinitionsFactoryException ex) {
			throw new ApplicationContextException("Failed to initialize Tiles definitions factory", ex);
		}
	}

}
