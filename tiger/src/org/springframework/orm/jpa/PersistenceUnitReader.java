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

package org.springframework.orm.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * Helper class for reading <code>persistence.xml</code> files.
 * 
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 */
class PersistenceUnitReader {

	private static final String MAPPING_FILE_NAME = "mapping-file";

	private static final String JAR_FILE_URL = "jar-file";

	private static final String MANAGED_CLASS_NAME = "class";

	private static final String PROPERTIES = "properties";

	private static final String PROVIDER = "provider";

	private static final String EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";

	private static final String NON_JTA_DATA_SOURCE = "non-jta-data-source";

	private static final String JTA_DATA_SOURCE = "jta-data-source";

	private static final String TRANSACTION_TYPE = "transaction-type";

	private static final String PERSISTENCE_UNIT = "persistence-unit";

	private static final String UNIT_NAME = "name";

	private static final String SCHEMA_NAME = "persistence_1_0.xsd";

	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	private static final String META_INF = "META-INF";

	private final Log logger = LogFactory.getLog(getClass());

	private final ResourcePatternResolver resourcePatternResolver;

	private final DataSourceLookup dataSourceLookup;

	public PersistenceUnitReader(ResourceLoader resourceLoader, DataSourceLookup dataSourceLookup) {
		this(new PathMatchingResourcePatternResolver(resourceLoader), dataSourceLookup);
	}

	public PersistenceUnitReader(ResourcePatternResolver resourcePatternResolver, DataSourceLookup dataSourceLookup) {
		Assert.notNull(resourcePatternResolver, "ResourceLoader must not be null");
		Assert.notNull(dataSourceLookup, "DataSourceLookup must not be null");
		this.resourcePatternResolver = resourcePatternResolver;
		this.dataSourceLookup = dataSourceLookup;
	}

	public SpringPersistenceUnitInfo[] readPersistenceUnitInfos(String persistenceXmlLocation) {
		ErrorHandler handler = new SimpleSaxErrorHandler(logger);
		List<SpringPersistenceUnitInfo> infos = new ArrayList<SpringPersistenceUnitInfo>();

		String resourceLocation = null;
		try {
			Resource[] resources = this.resourcePatternResolver.getResources(persistenceXmlLocation);
			for (Resource resource : resources) {
				resourceLocation = resource.toString();
				InputStream stream = resource.getInputStream();
				try {
					Document document = validateResource(handler, stream);
					parseDocument(resource, document, infos);
				}
				finally {
					stream.close();
				}
			}
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Cannot parse persistence unit from " + resourceLocation, ex);
		}
		catch (SAXException ex) {
			throw new IllegalArgumentException("Invalid XML in persistence unit from " + resourceLocation, ex);
		}
		catch (ParserConfigurationException ex) {
			throw new IllegalArgumentException("Internal error parsing persistence unit from " + resourceLocation);
		}

		return infos.toArray(new SpringPersistenceUnitInfo[infos.size()]);
	}

	protected URL determineUnitRootUrl(Resource resource) throws IOException {
		// check META-INF folder
		URL originalURL = resource.getURL();
		
		String urlToString = originalURL.toExternalForm();
		if (!urlToString.contains(META_INF)) {
			logger.warn(resource.getFilename() + " should be located inside META-INF directory; cannot determine persistence unit root URL for "
					+ resource.toString());
			return null;
		}
		if (urlToString.lastIndexOf(META_INF) == urlToString.lastIndexOf('/') - (1 + META_INF.length())) {
			logger.warn(resource.getFilename() + " is not located in the root of META-INF directory; cannot determine persistence unit root URL for "
					+ resource.toString());
			return null;
		}

		String persistenceUnitRoot = urlToString.substring(0, urlToString.lastIndexOf(META_INF));
		return new URL(persistenceUnitRoot);
	}

	/**
	 * Parse the validated document and populates(add to) the given unit info
	 * list.
	 */
	protected List<SpringPersistenceUnitInfo> parseDocument(Resource resource, Document document,
			List<SpringPersistenceUnitInfo> infos) throws IOException {
		Element persistence = document.getDocumentElement();
		URL unitRootURL = determineUnitRootUrl(resource);
		List<Element> units = (List<Element>) DomUtils.getChildElementsByTagName(persistence, PERSISTENCE_UNIT);
		for (Element unit : units) {
			SpringPersistenceUnitInfo info = parsePersistenceUnitInfo(unit);
			info.setPersistenceUnitRootUrl(unitRootURL);
			infos.add(info);
		}

		return infos;
	}

	/**
	 * Utility method that returns the first child element identified by its
	 * name.
	 */
	protected Element getChildElementByName(Element rootElement, String childName) {
		NodeList nl = rootElement.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, childName)) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Utility method that returns the first child element value identified by
	 * its name.
	 */
	protected String getChildElementValueByName(Element rootElement, String childName) {
		Element child = getChildElementByName(rootElement, childName);
		String value = null;
		if (child != null)
			value = DomUtils.getTextValue(child).trim();

		return (StringUtils.hasText(value) ? value : null);
	}

	/**
	 * Parse the unit info DOM element.
	 */
	protected SpringPersistenceUnitInfo parsePersistenceUnitInfo(Element persistenceUnit) throws IOException {
		SpringPersistenceUnitInfo unitInfo = new SpringPersistenceUnitInfo();
		// set name
		unitInfo.setPersistenceUnitName(persistenceUnit.getAttribute(UNIT_NAME).trim());
		// set transaction type
		String txType = persistenceUnit.getAttribute(TRANSACTION_TYPE).trim();

		if (StringUtils.hasText(txType))
			unitInfo.setTransactionType(PersistenceUnitTransactionType.valueOf(txType));

		// datasource
		String jtaDataSource = getChildElementValueByName(persistenceUnit, JTA_DATA_SOURCE);
		if (jtaDataSource != null) {
			unitInfo.setJtaDataSource(this.dataSourceLookup.getDataSource(jtaDataSource));
		}

		String nonJtaDataSource = getChildElementValueByName(persistenceUnit, NON_JTA_DATA_SOURCE);
		if (nonJtaDataSource != null) {
			unitInfo.setNonJtaDataSource(this.dataSourceLookup.getDataSource(nonJtaDataSource));
		}

		// provider
		String provider = getChildElementValueByName(persistenceUnit, PROVIDER);
		if (provider != null)
			unitInfo.setPersistenceProviderClassName(provider);

		// exclude unlisted classes
		Element excludeUnlistedClasses = getChildElementByName(persistenceUnit, EXCLUDE_UNLISTED_CLASSES);
		if (excludeUnlistedClasses != null)
			unitInfo.setExcludeUnlistedClasses(true);

		// mapping file
		parseMappingFiles(persistenceUnit, unitInfo);
		parseJarFiles(persistenceUnit, unitInfo);
		parseClass(persistenceUnit, unitInfo);
		parseProperty(persistenceUnit, unitInfo);
		return unitInfo;
	}

	/**
	 * Parse the <code>property</code> XML elements.
	 */
	@SuppressWarnings("unchecked")
	protected void parseProperty(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) {
		Element propRoot = getChildElementByName(persistenceUnit, PROPERTIES);
		if (propRoot == null)
			return;

		List<Element> properties = DomUtils.getChildElementsByTagName(propRoot, "property");
		for (Element property : properties) {
			String name = property.getAttribute("name");
			String value = property.getAttribute("value");
			unitInfo.addProperty(name, value);
		}
	}

	/**
	 * Parse the <code>class</code> XML elements.
	 */
	@SuppressWarnings("unchecked")
	protected void parseClass(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) {
		List<Element> classes = DomUtils.getChildElementsByTagName(persistenceUnit, MANAGED_CLASS_NAME);
		for (Element element : classes) {
			String value = DomUtils.getTextValue(element).trim();
			if (StringUtils.hasText(value))
				unitInfo.addManagedClassName(value);
		}
	}

	/**
	 * Parse the <code>jar-file</code> XML elements.
	 */
	@SuppressWarnings("unchecked")
	protected void parseJarFiles(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) throws IOException {
		List<Element> jars = DomUtils.getChildElementsByTagName(persistenceUnit, JAR_FILE_URL);
		for (Element element : jars) {
			String value = DomUtils.getTextValue(element).trim();
			if (StringUtils.hasText(value)) {
				Resource resource = this.resourcePatternResolver.getResource(value);
				unitInfo.addJarFileUrl(resource.getURL());
			}
		}
	}

	/**
	 * Parse the <code>mapping-file</code> XML elements.
	 */
	@SuppressWarnings("unchecked")
	protected void parseMappingFiles(Element persistenceUnit, SpringPersistenceUnitInfo unitInfo) {
		List<Element> files = DomUtils.getChildElementsByTagName(persistenceUnit, MAPPING_FILE_NAME);
		for (Element element : files) {
			String value = DomUtils.getTextValue(element).trim();
			if (StringUtils.hasText(value))
				unitInfo.addMappingFileName(value);
		}
	}

	/**
	 * Try to locate the SCHEMA_NAME first on disk before using the URL
	 * specified inside the XML.
	 * 
	 * @return an existing resource or null if one can't be find (or it does not
	 *         exist)
	 */
	protected Resource findSchemaResource(String schemaName) throws IOException {
		// first search the classpath root (Toplink)
		Resource schemaLocation = resourcePatternResolver.getResource("classpath:" + schemaName);

		if (schemaLocation.exists())
			return schemaLocation;

		// do a lookup for unpacked files
		// see the warning in
		// org.springframework.core.io.support.PathMatchingResourcePatternResolver
		// for more info
		Resource[] resources = resourcePatternResolver.getResources("classpath*:**/" + schemaName);
		if (resources.length > 0)
			return resources[0];

		// try org packages (hibernate)
		resources = resourcePatternResolver.getResources("classpath*:org/**/" + schemaName);
		if (resources.length > 0)
			return resources[0];

		// try com packages (some commercial
		resources = resourcePatternResolver.getResources("classpath*:com/**/" + schemaName);
		if (resources.length > 0)
			return resources[0];

		return null;
	}

	/**
	 * Validate the given stream and return a valid DOM document for parsing.
	 */
	protected Document validateResource(ErrorHandler handler, InputStream stream) throws ParserConfigurationException,
			SAXException, IOException {

		// InputSource source = new InputSource(stream);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		dbf.setNamespaceAware(true);

		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Resource schemaLocation = findSchemaResource(SCHEMA_NAME);

		// set schema location only if we found one inside the classpath
		if (schemaLocation != null) {
			if (logger.isDebugEnabled())
				logger.debug("found schema location " + schemaLocation.getURL().toString());
			dbf.setAttribute(JAXP_SCHEMA_SOURCE, schemaLocation.getURL().toString());
		}
		else if (logger.isDebugEnabled())
			logger.debug("no schema location found - falling back to the xml parser");

		// dbf.setAttribute(XERCES_SCHEMA_LOCATION,
		// schemaLocation.getURL().toString());
		/*
		 * see if these should be used on other jdks
		 * 
		 * dbf.setAttribute(JAXP_SCHEMA_SOURCE,
		 * schemaLocation.getURL().toString());
		 * dbf.setAttribute(XERCES_SCHEMA_LOCATION,
		 * schemaLocation.getURL().toString());
		 * 
		 * dbf.setAttribute(XML_SCHEMA_VALIDATION, Boolean.TRUE);
		 * dbf.setAttribute(XML_VALIDATION, Boolean.TRUE);
		 */

		DocumentBuilder parser = dbf.newDocumentBuilder();
		parser.setErrorHandler(handler);
		return parser.parse(stream);
	}

}
