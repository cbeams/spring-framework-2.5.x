/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.selector;

import java.util.HashMap;
import java.util.Map;

import org.springframework.selector.parser.NumericValue;
import org.springframework.selector.parser.Result;

import junit.framework.TestCase;

/**
 * Test some simple usage of the Selector API using a Map as a ValueProvider
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class TestSimpleEvaulation extends TestCase
{

    /**
     * Constructor for TestSimpleEvaulation.
     * @param name
     */
    public TestSimpleEvaulation(String name)
    {
        super(name);
    }

    /**
     * Test the Selector using a Map as a value provider.
     * @throws org.springframework.selector.parser.InvalidSelectorException
     */
    public void testValueMap()
        throws org.springframework.selector.parser.InvalidSelectorException
    {
        String selector = "Quantity > 100 AND Price < 20";
        ISelector isel = Selector.getInstance(selector);
        Map identifiers = isel.getIdentifiers();
        assertNotNull("Identifier map should not be null", identifiers);
        assertEquals(
            "Two identifiers should be parsed from " + selector,
            2,
            identifiers.size());
        assertTrue(identifiers.containsKey("Quantity"));
        assertTrue(identifiers.containsKey("Price"));
        assertTrue(
            identifiers.get("Quantity")
                instanceof org.springframework.selector.parser.Identifier);
        assertTrue(
            identifiers.get("Price")
                instanceof org.springframework.selector.parser.Identifier);

        Map values = new HashMap();
        //Using NumericValue is a result of keeping the Java and C# codebase as similar as possible for
        //translation using the JLCA.
        values.put("Quantity", new NumericValue(new Integer(101)));
        values.put("Price", new NumericValue(new Integer(19)));
        Result result = isel.eval(values);
        assertTrue("Should evalulate to true", result == Result.RESULT_TRUE);

        values.put("Quantity", new NumericValue(new Integer(99)));
        result = isel.eval(values);
        assertFalse("Should evaluate to false", result == Result.RESULT_FALSE);
    }
}
