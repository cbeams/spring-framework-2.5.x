package org.springframework.beans.factory.xml;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.IOException;

/**
 * @author Rob Harrop
 */
public class PluggableSchemaResolver implements EntityResolver {

    private static final String SPRING_SCHEMA_PREFIX = "http://www.springframework.org/schema/";

    private static final String PACKAGE_PREFIX = "org/springframework/";

    public InputSource resolveEntity(String publicId, String systemId) throws IOException {
        if (systemId != null) {
            Resource schemaResource;
            if (isSpringSchema(systemId)) {
                // spring schema file
                schemaResource = resolveSpringSchema(systemId);
            }
            else {
                // 3rd party schemas
                schemaResource = resolveThirdPartySchema(systemId);
            }

            if (schemaResource != null) {
                InputSource source = new InputSource(schemaResource.getInputStream());
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            }

        }
        return null;
    }

    private Resource resolveThirdPartySchema(String systemId) {
        throw new UnsupportedOperationException();
    }

    private Resource resolveSpringSchema(String systemId) {
        String path = PACKAGE_PREFIX + systemId.substring(SPRING_SCHEMA_PREFIX.length());
        return new ClassPathResource(path);
    }

    private boolean isSpringSchema(String systemId) {
        return (systemId.indexOf(SPRING_SCHEMA_PREFIX) > -1);
    }
}
