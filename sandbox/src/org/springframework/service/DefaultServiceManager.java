/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * An implementatino of the ServiceManager interface that puts the
 * registered ServiceBean instances through their lifecycle.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class DefaultServiceManager implements ServiceManager
{

    /**
     * The logger instance.
     */
    private static Log LOG = LogFactory.getLog(DefaultServiceManager.class);

    /**
     * The context/beanfactory for looking up the beans.
     */
    private ApplicationContext _ctx;

    /**
     * The array of service beans.
     *
     */
    private ServiceBean[] _serviceBeans;

    /**
     * Simple constructor.
     *
     */
    public DefaultServiceManager()
    {

    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public boolean validateSingleton()
    {
        boolean rval = true;
        String[] beanName = _ctx.getBeanDefinitionNames(ServiceBean.class);
        for (int i = 0; i < beanName.length; i++)
        {
            if (!_ctx.isSingleton(beanName[i]))
            {
                LOG.warn(
                    "Will not manage the bean named "
                        + beanName[i]
                        + " since it is not a singleton");
                rval = false;
            }
        }
        return rval;
    }

    /**
     * Loop over all registered managed beans and call their initialize method.
     * @see org.springframework.service.ServiceBean#initialize()
     */
    public void initialize() throws Exception
    {
        for (int i = 0; i < _serviceBeans.length; i++)
        {
            _serviceBeans[i].initialize();
        }

    }

    /**
     * Loop over all registered managed beans and call their start method.
     * @see org.springframework.service.ServiceBean#start()
     */
    public void start()
    {
        for (int i = 0; i < _serviceBeans.length; i++)
        {
            _serviceBeans[i].start();
        }

    }

    /**
     * Loop over all registered managed beans and call their stop method.
     * @see org.springframework.service.ServiceBean#stop()
     */
    public void stop()
    {
        for (int i = 0; i < _serviceBeans.length; i++)
        {
            _serviceBeans[i].stop();
        }
    }

    /**
     * Loop over all registered managed beans and call their dispose method.
     * @see org.springframework.service.ServiceBean#dispose()
     */
    public void dispose()
    {
        for (int i = 0; i < _serviceBeans.length; i++)
        {
            _serviceBeans[i].dispose();
        }
    }

    /**
     * Set the application context.  This class needs it to look up
     * references to other beans, so it caches a reference.
     * @param ctx {@inheritdoc}
     * @throws ApplicationContextException {@inheritdoc}
     */
    public void setApplicationContext(final ApplicationContext ctx)
        throws ApplicationContextException
    {
        _ctx = ctx;
    }

    /**
     * Set the property of the list of managed beans. This list determines the
     * order that the ServiceBeans will be put through their lifecycle.
     * @param managedBeans the list of managed beans.
     */
    public void setServiceBeans(ServiceBean[] serviceBeans)
    {
        _serviceBeans = serviceBeans;

    }

    /**
     * Call the stop() and dispose() methods when the Spring BeanFactory is destroyed,
     * for example when the application shuts down.  This only occurs if the ServiceManager
     * is a singleton, which is should be.
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception
    {
        stop();
        dispose();        
    }

}
