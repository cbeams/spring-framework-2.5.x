package org.springframework.jmx;

import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.jmx.naming.KeyNamingStrategy;

/**
 * @author robh
 */
public class KeyNamingStrategyTests extends AbstractNamingStrategyTests{

    private static final String OBJECT_NAME = "spring:name=test";

    protected ObjectNamingStrategy getStrategy() throws Exception {
        return new KeyNamingStrategy();
    }

    protected Object getManagedResource() throws Exception {
        return new Object();
    }

    protected String getKey() {
        return OBJECT_NAME;
    }

    protected String getCorrectObjectName() {
        return OBJECT_NAME;
    }
}
