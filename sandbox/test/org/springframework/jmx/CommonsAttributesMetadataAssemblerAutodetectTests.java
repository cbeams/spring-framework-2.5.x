/*
 * Created on 30-Nov-2004
 */
package org.springframework.jmx;

import org.springframework.jmx.metadata.support.JmxAttributeSource;
import org.springframework.jmx.metadata.support.commons.CommonsAttributesJmxAttributeSource;

/**
 * @author robh
 */
public class CommonsAttributesMetadataAssemblerAutodetectTests extends
        AbstractMetadataAssemblerAutodetectTests {


    public CommonsAttributesMetadataAssemblerAutodetectTests(String name) {
        super(name);
    }

    protected JmxAttributeSource getAttributeSource() {
        return new CommonsAttributesJmxAttributeSource();
    }

}
