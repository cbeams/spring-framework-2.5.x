/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

/**
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public abstract class BaseService implements ServiceBean
{

    /**
     * A string to modify as lifecycle method get called.
     */
    private static String calledString = "";
    
    

    /**
     * Return the string used for method tracking purposes.
     * The value of the string that gets modified as lifecycle
     * methods are called.
     * @return the string to use for tracking purposes.
     */
    public static String getCalledString()
    {
        return calledString;
    }

    /**
     * Add a value to the called string.
     * @param v the value to add
     */
    public static void addCalledString(String v)
    {
        calledString += v;
    }

}
