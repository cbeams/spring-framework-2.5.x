package org.springframework.beans.factory.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Rob Harrop
 */
public class DelegatingEntityResolver implements EntityResolver {

    private static final String DTD_SUFFIX = ".dtd";

    private static final String XSD_SUFFIX = ".xsd";

    protected final Log logger = LogFactory.getLog(getClass());

    private EntityResolver dtdResolver = new BeansDtdResolver();

    private EntityResolver schemaResolver = new PluggableSchemaResolver();

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId != null) {
            if (systemId.endsWith(DTD_SUFFIX)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to resolve DTD [" + systemId + "] using ["
                            + this.dtdResolver.getClass().getName() + "].");
                }
                return this.dtdResolver.resolveEntity(publicId, systemId);
            }
            else if (systemId.endsWith(XSD_SUFFIX)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Attempting to resolve XML Schema [" + systemId + "] using ["
                            + this.dtdResolver.getClass().getName() + "].");
                }
                return this.schemaResolver.resolveEntity(publicId, systemId);
            }

        }

        return null;
    }
}
