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
package org.springframework.jmx;

import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.assemblers.reflection.ReflectiveModelMBeanInfoAssembler;
import org.springframework.jmx.exceptions.MBeanServerNotFoundException;
import org.springframework.jmx.invokers.reflection.ReflectiveMBeanInvoker;
import org.springframework.jmx.naming.KeyNamingStrategy;
import org.springframework.jmx.naming.ObjectNamingStrategy;

/**
 * A bean that allows for any Spring managed to be exposed
 * to an MBeanServer without the need to define any JMX specific
 * information in the bean classes. 
 * 
 * If the bean implements one of the JMX management interface
 * then JmxMBeanAdapter will simply register the MBean with the server
 * automatically.
 * 
 * If the bean does not implement on the JMX management interface then
 * JmxMBeanAdapter will create the management information using the supplied <tt>MetadataAssembler</tt>
 * implementation. Once the MBean data is created JmxMBeanAdapter handles requests to invoke methods
 * on the managed bean using one the <tt>MBeanInvoker</tt> implementations.
 * @author Rob Harrop
 * @since 1.2
 */
public class JmxMBeanAdapter implements InitializingBean, DisposableBean {

    private static final Log log = LogFactory.getLog(JmxMBeanAdapter.class);
    
    /**
     * The beans to be exposed as JMX managed resources.
     */
    private Map beans;

    /**
     * Stores the ModelMBeanInfoAssembler to use for this adapter
     */
    private ModelMBeanInfoAssembler assembler = new ReflectiveModelMBeanInfoAssembler();

    /**
     * Stores the MBeanInvoker instance responsible for invoking operations on MBeans
     */
    private MBeanInvoker invoker = new ReflectiveMBeanInvoker();
    
    /**
     * The strategy to use for creating ObjectNames for
     * an object
     */
    private ObjectNamingStrategy namingStrategy = new KeyNamingStrategy();
    
    /**
     * The MBeanServer instance being used to register beans
     */
    private MBeanServer server;
    
    /**
     * The beans that have been registered.
     */
    private ObjectName[] registeredBeans = null;

    public void afterPropertiesSet() throws Exception {

        // check that at least one bean has been specified
        if (beans == null) {
            throw new IllegalArgumentException("Must set property 'beans' of "
                    + getClass().getName());
        }

        if (beans.size() < 1) {
            throw new IllegalArgumentException(
                    "Must specify at least one bean for registration");
        }

        // if no server was provided
        // then try to load one. 
        // This is useful in environment such as
        // JBoss where there is already an MBeanServer loaded
        if(server == null) {
            log.debug("No MBeanServer provided. Attempting to locate one...");
            locateMBeanServer();
        }
        
        // now register the beans
        registerBeans();
    }

    public void setBeans(Map beans) {
        this.beans = beans;
    }

    public void setAssembler(ModelMBeanInfoAssembler assembler) {
        this.assembler = assembler;
    }
    
    public void setInvoker(MBeanInvoker invoker) {
        this.invoker = invoker;
    }
    
    public void setNamingStrategy(ObjectNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }
    
    public void setServer(MBeanServer server) {
        this.server = server;
    }

    private void locateMBeanServer() {
        List servers = MBeanServerFactory.findMBeanServer(null);

        // check to see if an MBeanServer is registered
        if ((servers == null) || (servers.size() == 0)) {
            throw new MBeanServerNotFoundException(
                    "Unable to locate an MBeanServer instance");
        }

        //TODO: Throw exception if more than one exists

        this.server = (MBeanServer) servers.get(0);
        
        if(log.isDebugEnabled()) {
           log.debug("Found MBeanServer: " + server.toString());
        }
    }

    /**
     * Register the defined beans with the MBeanServer
     *  
     */
    private void registerBeans() throws Exception {

        // TODO: Improve error handling and logging in here

        Object[] keys = beans.keySet().toArray();
        registeredBeans = new ObjectName[keys.length];

        for(int x = 0; x < keys.length; x++){
            String key = (String) keys[x];
            
            Object bean = beans.get(key);
            ObjectName objectName = namingStrategy.getObjectName(bean, key);
            

            ModelMBean mbean = new ModelMBeanImpl(invoker);
            mbean.setManagedResource(bean, "ObjectReference");
            mbean.setModelMBeanInfo(assembler.getMBeanInfo(bean));

            log.info("Registering MBean: " + objectName.toString());
            
            server.registerMBean(mbean, objectName);
            registeredBeans[x] = objectName;
            
            log.info("Registered MBean: " + objectName.toString());
            
        }
    }

    /**
     * Unregister all the beans
     */
    public void destroy() throws Exception {
        log.info("Unregistering all beans");
        
        for(int x = 0; x < registeredBeans.length; x++) {
            server.unregisterMBean(registeredBeans[x]);
        }
        
        server = null;
    }
}