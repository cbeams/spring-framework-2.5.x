package org.springframework.jmx;

import junit.framework.TestCase;
import org.springframework.jmx.naming.PropertiesNamingStrategy;
import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.core.io.ClassPathResource;

import javax.management.ObjectName;

/**
 * @author robh
 */
public abstract class AbstractNamingStrategyTests extends TestCase {


    public void testNaming() throws Exception {
        ObjectNamingStrategy strat = getStrategy();

        ObjectName objectName = strat.getObjectName(getManagedResource(), getKey());
        assertEquals(objectName.getCanonicalName(), getCorrectObjectName());
    }

    protected abstract ObjectNamingStrategy getStrategy() throws Exception;

    protected abstract Object getManagedResource() throws Exception;

    protected abstract String getKey();

    protected abstract String getCorrectObjectName();

}
