/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.freemarker;

import org.springframework.beans.factory.FactoryBean;

import freemarker.template.Configuration;


/**
 * Factory bean that configures a FreeMarker Configuration and provides it as 
 * bean reference. This bean is intended for any kind of usage of 
 * FreeMarker in application code, e.g. for generating email content. 
 * For web views, FreemarkerConfigurer is used to set up a 
 * ConfigurationFactory.
 *
 * <p>See base class ConfigurationFactory for details.
 * 
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: ConfigurationFactoryBean.java,v 1.1 2004-03-11 20:02:26 davison Exp $
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
