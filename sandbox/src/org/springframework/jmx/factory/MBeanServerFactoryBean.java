/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.jmx.factory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory Bean to create and obtain an MBeanServer instance.
 * 
 * @author Rob Harrop
 * @since 1.2
 */
public class MBeanServerFactoryBean implements FactoryBean, InitializingBean {

    /**
     * Should the MBeanServerFactoryBean instruct the MBeanServerFactory to
     * maintain an internal reference to the MBeanServer
     */
    private boolean haveFactoryHoldReference = true;

    /**
     * Hold the MBeanServer
     */
    private MBeanServer server = null;

    /**
     * The default domain used by the MBeanServer
     */
    private String defaultDomain = null;

    /**
     * Returns the MBeanServer instance
     * 
     * @return The MBeanServer instance
     */
    public Object getObject() throws Exception {
        return this.server;
    }

    /**
     * Returns the default domain used by the MBeanServer
     * @return
     */
    public String getDefaultDomain() {
        return defaultDomain;
    }
    
    /**
     * Set the default domain to be used by the MBeanServer.
     * Must be set before the MBeanServer is created, that is before
     * afterPropertiesSet() is called.
     * @param defaultDomain The domain name to use
     */
    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomain = defaultDomain;
    }
    
    /**
     * Indicates whether the MBeanServerFactory was instruced to maintain
     * a reference to the MBeanServer after creation.
     * @return True if the MBeanServerFactory has the reference otherwise false
     */
    public boolean getHaveFactoryHoldReference() {
        return haveFactoryHoldReference;
    }
    
    /**
     * Setting this value to true will cause the MBeanServer to be created with a call
     * to MBeanServerFactory.createMBeanServer(), and thus it will be possible to
     * retreive a reference to the MBeanServer using MBeanServerFactory.findMBeanServer().
     */
    public void setHaveFactoryHoldReference(boolean haveFactoryHoldReference) {
        this.haveFactoryHoldReference = haveFactoryHoldReference;
    }
    
    /**
     * Convenience method to retreive the MBeanServer
     * without the need to cast.
     */
    public MBeanServer getServer() {
        return server;
    }
    /**
     * Indicates the type of Object returned by this factory bean
     * 
     * @return Always MBeanServer
     */
    public Class getObjectType() {
        return MBeanServer.class;
    }

    /**
     * Indicates that the MBeanServer returned by this method is a singleton.
     * 
     * @return Always true
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Creats the MBeanServer instance
     */
    public void afterPropertiesSet() throws Exception {

        if (haveFactoryHoldReference) {
            // create an MBeanServer instance that is accessible
            // using MBeanServerFactory.findMBeanServer
            if (defaultDomain != null) {
                server = MBeanServerFactory.createMBeanServer(defaultDomain);
            } else {
                server = MBeanServerFactory.createMBeanServer();
            }
        } else {
            // create an MBeanServer instance that is not accessible
            // using MBeanServerFactory.findMBeanServer
            if (defaultDomain != null) {
                server = MBeanServerFactory.newMBeanServer(defaultDomain);
            } else {
                server = MBeanServerFactory.newMBeanServer();
            }
        }
    }

}