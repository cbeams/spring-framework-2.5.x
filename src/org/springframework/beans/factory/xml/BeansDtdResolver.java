/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * EntityResolver implementation for the Spring beans DTD,
 * to load the DTD from the Spring class path (or JAR file).
 *
 * <p>Fetches "spring-beans.dtd" from the class path resource
 * "/org/springframework/beans/factory/xml/spring-beans.dtd",
 * no matter whether specified as some local URL that includes "spring-beans"
 * in the DTD name or as "http://www.springframework.org/dtd/spring-beans.dtd".
 *
 * @author Juergen Hoeller
 * @since 04.06.2003
 * @see ResourceEntityResolver
 */
public class BeansDtdResolver implements EntityResolver {

	private static final String DTD_NAME = "spring-beans";

	protected final Log logger = LogFactory.getLog(getClass());

	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to resolve XML entity with public ID [" + publicId +
					"] and system ID [" + systemId + "]");
		}
		if (systemId != null && systemId.indexOf(DTD_NAME) > systemId.lastIndexOf("/")) {
			String dtdFile = systemId.substring(systemId.indexOf(DTD_NAME));
			if (logger.isDebugEnabled()) {
				logger.debug("Trying to locate [" + dtdFile + "] in Spring jar");
			}
			try {
				Resource resource = new ClassPathResource(dtdFile, getClass());
				InputSource source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				if (logger.isDebugEnabled()) {
					logger.debug("Found beans DTD [" + systemId + "] in classpath");
				}
				return source;
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve beans DTD [" + systemId + "]: not found in class path", ex);
				}
			}
		}
		// Use the default behavior -> download from website or wherever.
		return null;
	}

}
