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
package org.springframework.jmx.adapters;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;
import org.springframework.jmx.util.JmxUtils;

/**
 * @author robh
 */
public abstract class AbstractConfigurableAdapterHost implements AdapterHost,
        InitializingBean, DisposableBean {

    private ObjectName objectName;

    private MBeanServer server;

    private boolean startAutomatically = true;

    private boolean registerWithServer = false;

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public void setStartAutomatically(boolean startAutomatically) {
        this.startAutomatically = startAutomatically;
    }

    public void setServer(MBeanServer server) {
        this.server = server;
    }

    public void setRegisterWithServer(boolean registerWithServer) {
        this.registerWithServer = registerWithServer;
    }

    public void afterPropertiesSet() {
        if (registerWithServer && objectName == null) {
            throw new IllegalArgumentException(
                    "Must specify ObjectName for AdapterHost");
        }

        if (registerWithServer && server == null) {
            // attempt to locate MBeanServer
            server = JmxUtils.locateMBeanServer();

            if (server == null) {
                // still null - throw exception
                throw new IllegalArgumentException(
                        "Unable to locate MBeanServer. You must set the server property of "
                                + this.getClass().getName());
            }
        }

        initAdapterHost();

        if (registerWithServer) {
            registerAdapter();
        }

        if (startAutomatically) {
            start();
        }
    }

    public void destroy() throws Exception {
        stop();
    }

    private void registerAdapter() {
        // register the adapter with the mbean server
        try {
            server.registerMBean(getAdaptor(), objectName);
        } catch (NotCompliantMBeanException ex) {
            throw new AdapterRegistrationException(
                    "Unable to register adapter with MBeanServer since it is not a compliant MBean.",
                    ex);
        } catch (InstanceAlreadyExistsException ex) {
            throw new AdapterRegistrationException(
                    "Unable to register adapter with MBeanServer since it is already registered.",
                    ex);
        } catch (MBeanRegistrationException ex) {
            throw new AdapterRegistrationException(
                    "An error occurred when trying register the adapter with the MBeanServer.",
                    ex);
        }
    }

    public void setObjectName(String objectName) {
        try {
            this.objectName = ObjectNameManager.getInstance(objectName);
        } catch (MalformedObjectNameException ex) {
            throw new ObjectNamingException(
                    "You supplied an invalid ObjectName for AdapterHost of type: "
                            + this.getClass().getName(), ex);
        }
    }

    /**
     * Implementation can override to perform some additional initialization.
     *  
     */
    protected void initAdapterHost() {
    };
}