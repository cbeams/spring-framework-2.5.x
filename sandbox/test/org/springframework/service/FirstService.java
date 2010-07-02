/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

/**
 * Simple class for testing. 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class FirstService extends BaseService
{

    /**
     * Do nothing constructor
     *
     */
    public FirstService()
    {
        
    }

    /**
     * Add '1init' to the called string.
     * @see org.springframework.service.ServiceBean#initialize()
     */
    public void initialize() throws Exception
    {
        addCalledString("1init-");

    }

    /**
     * Add '1start' to the called string.
     * @see org.springframework.service.ServiceBean#start()
     */
    public void start()
    {
        addCalledString("1start-");

    }

    /**
     * Add '1dispose' to the called string.
     * @see org.springframework.service.ServiceBean#dispose()
     */
    public void dispose()
    {
        addCalledString("1dispose-");

    }

    /**
     * Add '1stop' to the called string.
     * @see org.springframework.service.ServiceBean#stop()
     */
    public void stop()
    {
        addCalledString("1stop-");

    }

}
