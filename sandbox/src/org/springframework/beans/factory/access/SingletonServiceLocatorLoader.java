/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.beans.factory.access;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A bean-factory post processor configured to trigger the instantiation and
 * configuration of a global service locator from a Spring application context.
 * <p>
 * <p>
 * This allows for the locator and the services it provides access to to be
 * configured via Spring just like any other bean. In addition, as a
 * <code>BeanFactoryPostProcessor</code> this class ensures locators are
 * instantiated before the objects that use them. This is often necessary when
 * objects access the locator through a convenient static accessors (e.g.
 * getInstance()), as opposed to having a reference to the locator injected
 * through a setter or constructor.
 * <p>
 * <p>
 * There are two different ways to load a singleton service locator:
 * <ol>
 * <li>By specifying one or more bean id references in the application context
 * that point to locators with static load() methods. In this case, the load()
 * method is called directly on the locator instance to configure the singleton
 * instance. For example:
 * 
 * <pre>
 *    &lt;bean id=&quot;serviceLocatorLoader&quot;
 *          class=&quot;com.csi.commons.utils.beans.SingletonServiceLocatorLoader&quot;&gt;
 *      &lt;constructor-arg index=&quot;0&quot;&gt;
 *        &lt;list&gt;
 *          &lt;value&gt;consoleServices&lt;/value&gt;
 *        &lt;/list&gt;
 *      &lt;/constructor-arg&gt;        
 *    &lt;/bean&gt;
 * </pre>
 * 
 * ... will call the static <code>load</code> method on the
 * <code>ConsoleServices</code> class, passing in the configured
 * <code>consoleServices</code> bean.
 * 
 * <li>By specifying one or more singleton locator accessor classes with the
 * bean ID that corresponds to the instance to be loaded and shared. In this
 * case, load is called on the singleton locator accessor, and not the locator
 * itself. This approach completely abstracts away singleton status from the
 * locator class. As an example:
 * </ol>
 * 
 * <pre>
 *    &lt;bean id=&quot;serviceLocatorLoader&quot;
 *          class=&quot;com.csi.commons.utils.beans.SingletonServiceLocatorLoader&quot;&gt;
 *      &lt;constructor-arg index=&quot;0&quot;&gt;
 *        &lt;list&gt;
 *          &lt;value&gt;consoleServices@com.acme.ConsoleServicesSingletonLocator&lt;/value&gt;
 *        &lt;/list&gt;
 *      &lt;/constructor-arg&gt;        
 *    &lt;/bean&gt;
 * </pre>
 * 
 * ... will call the static <code>load</code> method on the
 * <code>ConsoleServicesSingletonLocator</code> class, passing in the
 * configured <code>consoleServices</code> bean.
 * 
 * Note - take care not to abuse this pattern. Generally dependency
 * injection/IoC should be preferred to singleton, getInstance() style lookup.
 * 
 * @author Keith Donald
 */
public class SingletonServiceLocatorLoader implements BeanFactoryPostProcessor, Ordered {
    private static final Log logger = LogFactory
            .getLog(SingletonServiceLocatorLoader.class);
    public String[] locatorBeanIds;
    private int order = Integer.MAX_VALUE;
    private String loadMethodName = "load";
    private static final char CLASS_SEPARATOR = '@';

    /**
     * Creates a SingletonServiceLocatorLoader that loads the specified services
     * using a static <code>load</code> method.
     * 
     * @param beanIds
     *            The configured service locator bean ids.
     */
    public SingletonServiceLocatorLoader(String[] beanIds) {
        Assert.hasElements(beanIds);
        this.locatorBeanIds = beanIds;
    }

    /**
     * Set the name of the static load method to call to configure locators
     * with the shared bean instance.
     * 
     * @param loadMethodName
     *            The load method name, <code>load</code> is used by default.
     */
    public void setLoadMethodName(String loadMethodName) {
        Assert.notNull(loadMethodName);
        this.loadMethodName = loadMethodName;
    }
    
    /**
     * @see org.springframework.core.Ordered#getOrder()
     */
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory
     */
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (int i = 0; i < locatorBeanIds.length; i++) {
            logger
                    .info("Instantating locator bean '" + locatorBeanIds[i]
                            + "'");
            String locatorBeanId = locatorBeanIds[i];

            Object locatorInstance = null;
            Class globalLocatorClass = null;
            int classSep = locatorBeanId.indexOf(CLASS_SEPARATOR);
            if (classSep == -1) {
                // no class specified, use locator class to load singleton
                // instance
                locatorInstance = beanFactory.getBean(locatorBeanIds[i]);
                globalLocatorClass = locatorInstance.getClass();
            } else {
                // singleton class specified, use it to load shared instance
                try {
                    locatorInstance = beanFactory.getBean(locatorBeanId
                            .substring(0, classSep));
                    globalLocatorClass = Class.forName(locatorBeanId
                            .substring(classSep + 1));
                } catch (ClassNotFoundException e) {
                    logger.warn("No class found '"
                            + locatorBeanId.substring(0, classSep) + "'");
                    logger
                            .warn("The singleton locator expression must be in the form <sharedBeanIdToLoad@singletonServiceLocatorAccessorClassName>");
                }
            }
            if (locatorInstance == null || globalLocatorClass == null) {
                return;
            }
            Method loadMethod = getStaticLoadMethod(globalLocatorClass,
                    locatorInstance);
            if (loadMethod == null) {
                logger
                        .warn("No public static 'load' method found on singleton service locator '"
                                + globalLocatorClass + "'");
            }
            try {
                loadMethod.invoke(null, new Object[] { locatorInstance });
            } catch (Exception e) {
                logger.warn("Unable to load locator '" + locatorInstance + "'",
                        e);
            }
        }
    }

    private Method getStaticLoadMethod(Class globalLocatorClass, Object instance) {
        Class[] args = new Class[] { instance.getClass() };
        Method method = ClassUtils.getStaticMethod(loadMethodName,
                globalLocatorClass, args);
        if (method == null) {
            Class[] interfaces = instance.getClass().getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                args[0] = interfaces[i];
                method = ClassUtils.getStaticMethod(loadMethodName,
                        globalLocatorClass, args);
                if (method != null) {
                    break;
                }
            }
        }
        return method;
    }

}