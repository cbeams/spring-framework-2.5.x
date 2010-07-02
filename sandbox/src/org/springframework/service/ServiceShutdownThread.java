/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Registered callback with Runtime.addShutdownHook.  Calls destroy on
 * the ServiceManager.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class ServiceShutdownThread extends Thread
{

    /**
     * The log instance
     */
    private static Log LOG = LogFactory.getLog(ServiceShutdownThread.class);

    /**
     * The service manager instance.
     */
    private ServiceManager _serviceManager;

    /**
     * Create the shutdown callback with a reference to the service manager.
     * @param mgr the service manager instance.
     */
    public ServiceShutdownThread(ServiceManager mgr)
    {
        _serviceManager = mgr;
    }

    /**
     * Call the destroy method of the service manager, log any exceptions thrown.
     */
    public void run()
    {
        try
        {
            _serviceManager.destroy();
        } catch (Exception e)
        {
            try
            {                
                LOG.error("Exception destroying service manager.", e);
            } catch (Throwable t)
            {
                //If somehow the logging got shutdown before this was called....
                System.err.println(
                    "ServiceShutdownThread: Error destroying service manager.  Exception Message = "
                        + t.getMessage());
            }
        }
    }

}
