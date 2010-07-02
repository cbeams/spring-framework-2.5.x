/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

/**
 * Simple class for testing. 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class SecondService extends BaseService
{
    /**
     * Do nothing constructor
     *
     */
    public SecondService()
    {
        
    }

    /**
     * Add '2init' to the called string.
     * @see org.springframework.service.ServiceBean#initialize()
     */
    public void initialize() throws Exception
    {
        addCalledString("2init-");

    }

    /**
     * Add '2start' to the called string.
     * @see org.springframework.service.ServiceBean#start()
     */
    public void start()
    {
        addCalledString("2start-");

    }

    /**
     * Add '2dispose' to the called string.
     * @see org.springframework.service.ServiceBean#dispose()
     */
    public void dispose()
    {
        addCalledString("2dispose-");

    }

    /**
     * Add '2stop' to the called string.
     * @see org.springframework.service.ServiceBean#stop()
     */
    public void stop()
    {
        addCalledString("2stop-");

    }

}
