/*
 * Created on Sep 15, 2004
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
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;

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