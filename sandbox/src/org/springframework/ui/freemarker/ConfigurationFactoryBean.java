/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.freemarker;

import org.springframework.beans.factory.FactoryBean;

import freemarker.template.Configuration;


/**
 * ConfigurationFactoryBean
 * 
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: ConfigurationFactoryBean.java,v 1.1 2004-03-05 19:45:18 davison Exp $
 */
public class ConfigurationFactoryBean extends ConfigurationFactory implements FactoryBean {

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return getConfiguration();
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Configuration.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

}
