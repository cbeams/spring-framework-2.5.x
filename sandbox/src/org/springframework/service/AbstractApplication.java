/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * Implementation of interface Application methods.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public abstract class AbstractApplication implements Application
{
    /**
     * The application context reference.
     */
    private ApplicationContext appContext_;

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
    public final void setApplicationContext(final ApplicationContext ctx)
        throws ApplicationContextException
    {
        appContext_ = ctx;
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
