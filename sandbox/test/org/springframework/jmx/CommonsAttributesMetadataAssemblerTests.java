/*
 * Created on 30-Nov-2004
 */
package org.springframework.jmx;

import org.springframework.jmx.metadata.support.JmxAttributeSource;
import org.springframework.jmx.metadata.support.commons.CommonsAttributesJmxAttributeSource;

/**
 * @author robh
 */
public class CommonsAttributesMetadataAssemblerTests extends
        AbstractMetadataAssemblerTests {

    private static final String OBJECT_NAME = "bean:name=testBean3";

    public CommonsAttributesMetadataAssemblerTests(String name) {
        super(name);

    }

    protected JmxAttributeSource getAttributeSource() {
        return new CommonsAttributesJmxAttributeSource();
    }

    protected String getObjectName() {
        return OBJECT_NAME;
    }
}
