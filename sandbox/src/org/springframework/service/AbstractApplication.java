/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Implementation of interface Application methods.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public abstract class AbstractApplication implements Application
{
    /**
     * The logger instance.
     */
    private static Log LOG = LogFactory.getLog(AbstractApplication.class);

    /**
     * The application context reference.
     */
    private ConfigurableApplicationContext appContext_;

    /**
     * The version of the application.
     */
    private String version_;

    /**
     * The short name of the application.
     */
    private String name_;

    /**
     * The long description of the application.
     */
    private String description_;

    /**
     * Set the application context.  Called by BeanFactory
     * @param ctx the Application context.
     * @throws org.springframework.context.ApplicationContextException from spring.
     */
    public void setApplicationContext(ApplicationContext ctx)
        throws BeansException
    {
        if (ctx instanceof ConfigurableApplicationContext)
        {
            appContext_ = (ConfigurableApplicationContext) ctx;
            if (appContext_.getBeanFactory()
                instanceof ConfigurableBeanFactory)
            {
                Runtime.getRuntime().addShutdownHook(
                    new ApplicationShutdownThread(
                        (ConfigurableBeanFactory) appContext_
                            .getBeanFactory()));
            } else
            {
                LOG.warn(
                    "Did not register ApplicationShutdownThread.  BeanFactory not of type ConfigurableBeanFactory");
            }
        } else
        {
            LOG.warn(
                "Did not register ApplicationShutdownThread.  ApplicationContext not of type ConfigurableApplicationContext");
        }
    }

    /**
     * Get the application context.
     * @return the application context.
     */
    public final ApplicationContext getAppContext()
    {
        return appContext_;
    }

    /**
     * The name of the application
     * @return the name of the application
     */
    public final String getName()
    {
        return name_;
    }

    /**
     * The version of the application
     * @return the version of the application
     */
    public final String getVersion()
    {
        return version_;
    }

    /**
     * Set the name of the application.  Will be used to print a banner
     * to the console.
     * @param name the name of the application.
     */
    public final void setName(final String name)
    {
        name_ = name;
    }

    /**
     * Set the version of the application.  Will be used to print a
     * banner to the console.
     * @param version the version of the application.
     */
    public final void setVersion(final String version)
    {
        version_ = version;
    }

    /**
     * Get the description of the application.
     * @return the description of the application.
     */
    public final String getDescription()
    {
        return description_;
    }

    /**
     * Set the description of the application
     * @param string a description of the application.
     */
    public final void setDescription(final String string)
    {
        description_ = string;
    }

    /**
     * A suggestion for an application banner that displays name 
     * and version of applicstion.
     * TODO Make a better banner impl.
     *
     */
    public String getBanner()
    {
        StringBuffer sb = new StringBuffer("\n");
        sb.append("*************************\n");
        sb.append("**\n");
        sb.append("** Name:  " + getName() + "\n");
        sb.append("**\n");
        sb.append("** Description: " + getDescription() + "\n");
        sb.append("**\n");
        sb.append("** Version:  " + getVersion() + "\n");
        sb.append("**\n");
        sb.append("*************************\n");
        return sb.toString();
    }
}
