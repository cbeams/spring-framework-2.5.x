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
package org.springframework.jmx.proxy;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.springframework.jmx.exceptions.ProxyCreationException;
import org.springframework.util.ClassUtils;

/**
 * @author robh
 */
public abstract class AbstractJmxObjectProxyFactory implements
        JmxObjectProxyFactory {

    /**
     * Store the interfaces to proxy
     */
    protected Class[] proxyInterfaces = null;

    /**
     * Specfiy with invalid invocations should be ignored or not.
     */
    protected boolean ignoreInvalidInvocations = true;

    public boolean getIgnoreInvalidInvocations() {
        return this.ignoreInvalidInvocations;
    }

    public Class[] getProxyInterfaces() {
        return this.proxyInterfaces;
    }

    public void setIgnoreInvalidInvocations(boolean ignoreInvalidInvocatios) {
        this.ignoreInvalidInvocations = ignoreInvalidInvocatios;
    }

    public void setProxyInterfaces(Class[] interfaces) {
        this.proxyInterfaces = interfaces;
    }

    /**
     * @param objectName
     */
    protected Class getClassForInstance(final ObjectName objectName,
            final MBeanServerConnection server) {
        ObjectInstance instance = null;
        
        try {

            instance = server.getObjectInstance(objectName);
            return ClassUtils.forName(instance.getClassName());

        } catch (InstanceNotFoundException ex) {

            // invalid ObjectName provided
            throw new ProxyCreationException(
                    "Unable to locate object specified by object name:"
                            + objectName
                            + ". Check the supplied ObjectName is valid");
        } catch (ClassNotFoundException ex) {

            // Unable to load class
            throw new ProxyCreationException("Unable to load class ["
                    + instance.getClassName() + "] for MBean [" + objectName
                    + "]. Ensure that this class is on the classpath.");
        } catch (IOException ex) {
			throw new ProxyCreationException(
					"An IOException occurred when communicating with the MBeanServer. "
							+ "It is likely that you are communicating with a remote MBeanServer. "
							+ "Check the inner exception for exact details.",
					ex);
		}
    }
}