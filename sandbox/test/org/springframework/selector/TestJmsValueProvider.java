/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.selector;

import javax.jms.MapMessage;

import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.Result;

import junit.framework.TestCase;

/**
 * Tests for the JMSValueProvider
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class TestJmsValueProvider extends TestCase
{

    /**
     * Constructor for TestJmsValueProvider.
     * @param name
     */
    public TestJmsValueProvider(String name)
    {
        super(name);
    }

    /**
     * Test basic simple usage
     * @throws Exception let JUnit handle it
     */
    public void testJmsValueProvider() throws Exception
    {
        String selector = ".Quantity > 100 AND .Price < 20";
        ISelector isel = Selector.getInstance(selector);

        MapMessage mapMsg = new SimpleMapMessage();
        mapMsg.setDouble("Quantity", 101.0);
        mapMsg.setDouble("Price", 19.0);

        IValueProvider vp =
            org.springframework.selector.vp.JMSValueProvider.valueOf(mapMsg);

        Result result = isel.eval(vp, null);

        assertTrue("Should evaluate to true", result == Result.RESULT_TRUE);

    }
}
