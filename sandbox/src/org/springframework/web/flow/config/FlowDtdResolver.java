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
 * 
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