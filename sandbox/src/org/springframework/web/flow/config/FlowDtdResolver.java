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
package org.springframework.web.flow.config;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EntityResolver implementation for the Spring web flow DTD, to load the DTD
 * from the classpath. The implementation is similar to that of the
 * <code>org.springframework.beans.factory.xml.BeansDtdResolver</code>.
 * @author Erwin Vervaet
 */
public class FlowDtdResolver implements EntityResolver {

	private static final String WEB_FLOW_ELEMENT = "web-flow";

	private static final String WEB_FLOW_CONFIG_PACKAGE = "/org/springframework/web/flow/config/";

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId != null && systemId.indexOf(WEB_FLOW_ELEMENT) > systemId.lastIndexOf("/")) {
			String dtdFile = systemId.substring(systemId.indexOf(WEB_FLOW_ELEMENT));
			try {
				Resource resource = new ClassPathResource(WEB_FLOW_CONFIG_PACKAGE + dtdFile, getClass());
				InputSource source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				return source;
			}
			catch (IOException ex) {
				// fall trough below
			}
		}
		return null; // let the parser handle it
	}
}