/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;


/**
 * Registered callback with Runtime.addShutdownHook.  Calls destroy on
 * the BeanFactory.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class ApplicationShutdownThread extends Thread
{

    /**
     * The log instance
     */
    private static Log LOG = LogFactory.getLog(ApplicationShutdownThread.class);

    /**
     * The Appcontext instance.
     */
    private ConfigurableBeanFactory _beanFactory;

    /**
     * Create the shutdown callback with a reference to the service manager.
     * @param bf the bean factory.
     */
    public ApplicationShutdownThread(ConfigurableBeanFactory bf)
    {
        _beanFactory = bf;
    }

    /**
     * Call the destroy method of the bean factory. Log any exceptions thrown.
     */
    public void run()
    {
        try
        {
            LOG.info("Destroying all singletons registered in the BeanFactory");
            _beanFactory.destroySingletons();
        } catch (Exception e)
        {
            try
            {                
                LOG.error("Exception destroying service manager.", e);
            } catch (Throwable t)
            {
                //If somehow the logging got shutdown before this was called....
                System.err.println(
                    "ApplicationShutdownThread:  Error destroying service manager.  Exception Message = "
                        + e.getMessage());
            }
        }
    }

}
