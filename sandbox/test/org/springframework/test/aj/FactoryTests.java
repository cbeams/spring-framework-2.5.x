package org.springframework.test.aj;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.springframework.test.CallMonitor;

import static org.springframework.test.aj.AjTestCaseLoader.createSuiteFor;
public class FactoryTests extends TestCase {
    public static TestSuite suite() {
        return createSuiteFor(FactoryTests.class);
    }

    public void testCallGetInstanceTwice() {
        assertEquals(0, CallMonitor.getCounter(FactoryAdvice.FACTORY_NEW_OPERATION));
        Factory.getInstance();
        assertEquals(1, CallMonitor.getCounter(FactoryAdvice.FACTORY_NEW_OPERATION));
        Factory.getInstance();
        assertEquals(1, CallMonitor.getCounter(FactoryAdvice.FACTORY_NEW_OPERATION));
    }
}
