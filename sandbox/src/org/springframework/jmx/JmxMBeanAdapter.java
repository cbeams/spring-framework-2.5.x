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

import java.util.HashMap;
import java.util.Map;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jmx.assemblers.reflection.ReflectiveModelMBeanInfoAssembler;
import org.springframework.jmx.exceptions.MBeanAssemblyException;
import org.springframework.jmx.invokers.reflection.ReflectiveMBeanInvoker;
import org.springframework.jmx.naming.KeyNamingStrategy;
import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.jmx.proxy.JmxProxyFactoryBean;

/**
 * A bean that allows for any Spring managed to be exposed to an MBeanServer
 * without the need to define any JMX specific information in the bean classes.
 * 
 * If the bean implements one of the JMX management interface then
 * JmxMBeanAdapter will simply register the MBean with the server automatically.
 * 
 * If the bean does not implement on the JMX management interface then
 * JmxMBeanAdapter will create the management information using the supplied
 * <tt>MetadataAssembler</tt> implementation. Once the MBean data is created
 * JmxMBeanAdapter handles requests to invoke methods on the managed bean using
 * one the <tt>MBeanInvoker</tt> implementations.
 * 
 * @author Rob Harrop
 * @since 1.2
 */
public class JmxMBeanAdapter implements InitializingBean, DisposableBean,
        BeanFactoryAware, BeanFactoryPostProcessor {

    private static final Log log = LogFactory.getLog(JmxMBeanAdapter.class);

    /**
     * The beans to be exposed as JMX managed resources.
     */
    private Map beans;

    /**
     * Stores the ModelMBeanInfoAssembler to use for this adapter
     */
    private ModelMBeanInfoAssembler assembler = new ReflectiveModelMBeanInfoAssembler();

    private MBeanInvoker invoker = new ReflectiveMBeanInvoker();

    /**
     * The strategy to use for creating ObjectNames for an object
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

    /**
     * Flag indicating whether the platform specific RequiredModelMBean class
     * should be used or whether the Spring implementation of ModelMBean should
     * be used.
     */
    private boolean useRequiredModelMBean = false;

    /**
     * Stores the BeanFactory for use in autodetection process
     */
    private ConfigurableListableBeanFactory beanFactory = null;

    public void afterPropertiesSet() throws Exception {

        // the beans property may be null
        // initially if we are relying solely
        // on autodetection
        if (beans == null) {
            beans = new HashMap();
        }

        // if no server was provided
        // then try to load one.
        // This is useful in environment such as
        // JBoss where there is already an MBeanServer loaded
        if (server == null) {
            log.debug("No MBeanServer provided. Attempting to locate one...");
            this.server = JmxUtils.locateMBeanServer();
        }
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

    public void setUseRequiredModelMBean(boolean useRequiredModelMBean) {
        this.useRequiredModelMBean = useRequiredModelMBean;
    }

    /**
     * Register the defined beans with the MBeanServer
     *  
     */
    public void registerBeans() {

        // allow the metadata assembler a chance to
        // vote for bean inclusion
        if (assembler instanceof AutodetectCapableModelMBeanInfoAssembler) {
            autodetectBeans();
        }

        // check we now have at least one bean
        if (beans.size() < 1) {
            throw new IllegalArgumentException(
                    "Must specify at least one bean for registration");
        }

        Object[] keys = beans.keySet().toArray();
        registeredBeans = new ObjectName[keys.length];

        try {
            for (int x = 0; x < keys.length; x++) {
                String key = (String) keys[x];

                Object bean = beans.get(key);
                ObjectName objectName = namingStrategy.getObjectName(bean, key);

                if (bean instanceof DynamicMBean) {
                    log.info("Registering User Created MBean: "
                            + objectName.toString());

                    server.registerMBean(bean, objectName);
                } else {
                    ModelMBean mbean = getModelMBean(objectName);
                    mbean.setManagedResource(bean, "ObjectReference");
                    mbean.setModelMBeanInfo(assembler.getMBeanInfo(bean));

                    log.info("Registering and Assembling MBean: "
                            + objectName.toString());

                    server.registerMBean(mbean, objectName);
                }
                registeredBeans[x] = objectName;

                log.info("Registered MBean: " + objectName.toString());

            }
        } catch (JMException ex) {
            throw new MBeanAssemblyException(
                    "A JMX error occured when trying to assemble "
                            + "the management interface metadata.", ex);
        } catch (InvalidTargetObjectTypeException ex) {
            // we should never get this
            log
                    .warn("Received InvalidTargetObjectTypeException - this should not occur!");
            throw new MBeanAssemblyException(
                    "An invalid object type was used when specifying a managed resource. "
                            + "This is a serious error and points to an error in the Spring JMX Code",
                    ex);
        }
    }

    /**
     * Invoked when using an AutodetectCapableModelMBeanInfoAssembler - gives
     * the assembler the chance to autodetect beans.
     */
    private void autodetectBeans() {
        AutodetectCapableModelMBeanInfoAssembler autodetectAssembler = (AutodetectCapableModelMBeanInfoAssembler) assembler;

        String[] beanNames = beanFactory.getBeanDefinitionNames();
        String[] excludeBeans = beanFactory
                .getBeanDefinitionNames(JmxProxyFactoryBean.class);

        for (int x = 0; x < beanNames.length; x++) {
            String beanName = beanNames[x].trim();
            boolean exclude = false;

            for (int y = 0; y < excludeBeans.length; y++) {
                String excludeName = excludeBeans[y].trim();
                if (beanName.equals(excludeName)) {
                    exclude = true;
                }
            }

            if (exclude)
                continue;

            Object bean = beanFactory.getBean(beanName);

            if (!beans.containsValue(bean)) {
                // not already registered
                // for JMXification

                if (autodetectAssembler.includeBean(beanName, bean)) {
                    if (log.isInfoEnabled()) {
                        log.info("Bean Name: " + beanName
                                + " has been autodetected for JMXification");
                    }
                    beans.put(beanName, bean);
                }
            }
        }
    }

    /**
     * Gets an instance of the appropriate ModelMBean implementation.
     * 
     * @return
     */
    private ModelMBean getModelMBean(ObjectName objectName)
            throws MBeanException {
        if (useRequiredModelMBean) {
            return new RequiredModelMBean();
        } else {
            try {
                return new ModelMBeanImpl(invoker, objectName);
            } catch (Exception ex) {
                throw new MBeanException(ex,
                        "Unable to create ModelMBeanImpl class - check supplied invokerClass is valid");
            }
        }

    }

    /**
     * Unregister all the beans
     */
    public void destroy() throws Exception {
        log.info("Unregistering all beans");

        for (int x = 0; x < registeredBeans.length; x++) {
            server.unregisterMBean(registeredBeans[x]);
        }

        server = null;
    }

    /**
     * Implemented to grab the BeanFactory to allow for auto detection of
     * managed bean resources
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        } else {
            log
                    .info("Not using a ConfigurableListableBeanFactory - auto detection of managed beans is disabled");
        }
    }

    /**
     * Used to invoke the registerBeans() method automatically when running in
     * an <tt>ApplicationContext</tt> even if the bean is not a singleton
     */
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // register the beans
        registerBeans();
    }
}