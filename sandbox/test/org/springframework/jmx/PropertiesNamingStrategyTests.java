/*
 * Created on Jul 14, 2004
 */
package org.springframework.jmx;

import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.jmx.naming.PropertiesNamingStrategy;
import org.springframework.core.io.ClassPathResource;

/**
 * @author robh
 */
public class PropertiesNamingStrategyTests extends AbstractNamingStrategyTests {

    private static final String OBJECT_NAME = "bean:name=namingTest";


    protected ObjectNamingStrategy getStrategy() throws Exception {
        PropertiesNamingStrategy strat = new PropertiesNamingStrategy();
        strat.setPropertiesFile(new ClassPathResource("jmx.names.properties", this.getClass()));
        strat.afterPropertiesSet();
        return strat;
    }

    protected Object getManagedResource() {
        return new Object();
    }

    protected String getKey() {
        return "namingTest";
    }

    protected String getCorrectObjectName() {
        return OBJECT_NAME;
    }
}