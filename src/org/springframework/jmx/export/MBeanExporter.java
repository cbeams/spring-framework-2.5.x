/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jmx.export;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jmx.export.assembler.AutodetectCapableMBeanInfoAssembler;
import org.springframework.jmx.export.assembler.MBeanInfoAssembler;
import org.springframework.jmx.export.assembler.SimpleReflectiveMBeanInfoAssembler;
import org.springframework.jmx.export.naming.KeyNamingStrategy;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.jmx.support.JmxUtils;

/**
 * A bean that allows for any Spring-managed bean to be exposed to a JMX
 * <code>MBeanServer</code>, without the need to define any JMX-specific
 * information in the bean classes.
 *
 * <p>If the bean implements one of the JMX management interfaces then
 * MBeanExporter can simply register the MBean with the server automatically,
 * through its autodetection process.
 *
 * <p>If the bean does not implement one of the JMX management interfaces then
 * <code>MBeanExporter</code> will create the management information using the
 * supplied <code>MBeanMetadataAssembler</code> implementation.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setBeans
 * @see #setAutodetect
 */
public class MBeanExporter implements BeanFactoryAware, InitializingBean, DisposableBean {

	/**
	 * <code>Log</code> instance for this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The <code>MBeanServer</code> instance being used to register beans.
	 */
	private MBeanServer server;

	/**
	 * The beans to be exposed as JMX managed resources.
	 */
	private Map beans;

	/**
	 * Whether to autodetect MBeans in the bean factory.
	 */
	private boolean autodetect = false;

	/**
	 * Stores the <code>MBeanInfoAssembler</code> to use for this adapter.
	 */
	private MBeanInfoAssembler assembler = new SimpleReflectiveMBeanInfoAssembler();

	/**
	 * The strategy to use for creating <code>ObjectName</code>s for an object.
	 */
	private ObjectNamingStrategy namingStrategy = new KeyNamingStrategy();

	/**
	 * Stores the <code>BeanFactory</code> for use in autodetection process.
	 */
	private ListableBeanFactory beanFactory;

	/**
	 * The beans that have been registered by this adapter.
	 */
	private Set registeredBeans;


	/**
	 * Specify an instance <code>MBeanServer</code> with which all beans should
	 * be registered. The <code>MBeanExporter</code> will attempt to locate an
	 * existing <code>MBeanServer</code> if none is supplied.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Supply a <code>Map</code> of beans to be registered with the JMX
	 * <code>MBeanServer</code>.
	 * <p>The String keys are the basis for the creation of JMX object names.
	 * By default, a JMX <code>ObjectName</code> will be created straight
	 * from the given key. This can be customized through specifying a
	 * custom <code>NamingStrategy</code>.
	 * <p>Both bean instances and bean names are allowed as values.
	 * Bean instances are typically linked in through bean references.
	 * Bean names will be resolved as beans in the current factory, respecting
	 * lazy-init markers (that is, not triggering initialization of such beans).
	 * @param beans Map with bean instances or bean names as values
	 * @see #setNamingStrategy
	 * @see org.springframework.jmx.export.naming.KeyNamingStrategy
	 * @see javax.management.ObjectName#ObjectName(String)
	 */
	public void setBeans(Map beans) {
		this.beans = beans;
	}

	/**
	 * Set whether to autodetect MBeans in the bean factory that this exporter
	 * runs in. Will also ask an <code>AutodetectCapableMBeanInfoAssembler</code>
	 * if available.
	 * <p>This feature is turned off by default. Explicitly specify "true" here
	 * to enable autodetection.
	 * @see #setAssembler
	 * @see AutodetectCapableMBeanInfoAssembler
	 * @see org.springframework.jmx.support.JmxUtils#isMBean
	 */
	public void setAutodetect(boolean autodetect) {
		this.autodetect = autodetect;
	}

	/**
	 * Set the implementation of the <code>MBeanInfoAssembler</code> interface to use
	 * for this exporter. Default is a <code>SimpleReflectiveMBeanInfoAssembler</code>.
	 * <p>The passed-in assembler can optionally implement the
	 * <code>AutodetectCapableMBeanInfoAssembler</code> interface, which enables it
	 * to particiapte in the exporter's MBean autodetection process.
	 * @see org.springframework.jmx.export.assembler.SimpleReflectiveMBeanInfoAssembler
	 * @see org.springframework.jmx.export.assembler.AutodetectCapableMBeanInfoAssembler
	 * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler
	 * @see #setAutodetect
	 */
	public void setAssembler(MBeanInfoAssembler assembler) {
		this.assembler = assembler;
	}

	/**
	 * Set the implementation of the <code>ObjectNamingStrategy</code> interface
	 * to use for this exporter. Default is a <code>KeyNamingStrategy</code>.
	 * @see org.springframework.jmx.export.naming.KeyNamingStrategy
	 * @see org.springframework.jmx.export.naming.MetadataNamingStrategy
	 */
	public void setNamingStrategy(ObjectNamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * This callback is only required for resolution of bean names in the "beans"
	 * <code>Map</code> and for autodetection of MBeans (in the latter case,
	 * a <code>ListableBeanFactory</code> is required).
	 * @see #setBeans
	 * @see #setAutodetect
	 * @see org.springframework.beans.factory.ListableBeanFactory
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ListableBeanFactory) {
			this.beanFactory = (ListableBeanFactory) beanFactory;
		}
		else {
			logger.info("Not running in a ListableBeanFactory: autodetection of MBeans not available");
		}
	}

	/**
	 * Start bean registration automatically when deployed in an
	 * <code>ApplicationContext</code>.
	 * @see #registerBeans()
	 */
	public void afterPropertiesSet() throws JMException {
		// register the beans now
		registerBeans();
	}


	/**
	 * Registers the defined beans with the <code>MBeanServer</code>. Each bean is exposed
	 * to the <code>MBeanServer</code> via a <code>ModelMBean</code>. The actual implemetation
	 * of the <code>ModelMBean</code> interface used depends on the implementation of the
	 * <code>ModelMBeanProvider</code> interface that is configured. By default the
	 * <code>RequiredModelMBean</code> class that is supplied with all JMX implementations
	 * is used.
	 * <p>The management interface produced for each bean is dependent on the
	 * <code>MBeanInfoAssembler</code> implementation being used.
	 * The <code>ObjectName</code> given to each bean is dependent on the implementation
	 * of the <code>ObjectNamingStrategy</code> interface being used.
	 */
	protected void registerBeans() throws JMException {
		// If no server was provided then try to load one.
		// This is useful in environment such as JBoss or WebLogic
		// where there is already an MBeanServer loaded.
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}

		// The beans property may be null, for example
		// if we are relying solely on auto-detection.
		if (this.beans == null) {
			this.beans = new HashMap();
		}

		// Perform autodetection, if desired.
		if (this.autodetect) {
			if (this.beanFactory == null) {
				throw new JMException("Cannot autodetect MBeans if not running in a BeanFactory");
			}

			// Autodetect any beans that are already MBeans.
			logger.info("Autodetecting user-defined JMX MBeans");
			autodetectMBeans();

			// Allow the metadata assembler a chance to vote for bean inclusion.
			if (this.assembler instanceof AutodetectCapableMBeanInfoAssembler) {
				autodetectBeans((AutodetectCapableMBeanInfoAssembler) this.assembler);
			}
		}

		// Check we now have at least one bean.
		if (this.beans.isEmpty()) {
			throw new IllegalArgumentException("Must specify at least one bean for registration");
		}

		this.registeredBeans = new HashSet(this.beans.size());
		try {
			for (Iterator it = this.beans.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String beanKey = (String) entry.getKey();
				Object value = entry.getValue();
				ObjectName objectName = registerBeanNameOrInstance(beanKey, value);
				if (objectName != null) {
					this.registeredBeans.add(objectName);
				}
			}
		}
		catch (InvalidTargetObjectTypeException ex) {
			// Unregister beans already registered by this exporter.
			unregisterBeans();
			// We should never get this!
			throw new JMException("An invalid object type was used when specifying a managed resource. " +
					"This is a serious error and points to an error in the Spring JMX code. Root cause: " +
					ex.getMessage());
		}
		catch (JMException ex) {
			// Unregister beans already registered by this exporter.
			unregisterBeans();
			throw ex;
		}
	}


	/**
	 * Registers an individual bean with the <code>MBeanServer</code>. This method
	 * is responsible for deciding <strong>how</strong> a bean should be exposed
	 * to the <code>MBeanServer</code>. Specifically, if the <code>mapValue</code>
	 * is the name of a bean that is configured for lazy initialization, then
	 * a prxoy to the resource is registered with the <code>MBeanServer</code>
	 * so that the the lazy load behavior is honored. If the bean is already an
	 * MBean then it will be registered directly with the <code>MBeanServer</code>
	 * without any intervention. For all other beans or bean names, the resource
	 * itself is registered with the <code>MBeanServer</code> directly.
	 * @param beanKey the key associated with this bean in the beans map
	 * @param mapValue the value configured for this bean in the beans map.
	 * May be either the <code>String</code> name of a bean, or the bean itself.
	 * @return the <code>ObjectName</code> under which the resource was registered
	 * @throws JMException in case of an error in the underlying JMX infrastructure
	 * @throws InvalidTargetObjectTypeException an error in the definition of the MBean resource
	 * @see #setBeans
	 * @see #registerLazyInit
	 * @see #registerMBean
	 * @see #registerSimpleBean
	 */
	private ObjectName registerBeanNameOrInstance(String beanKey, Object mapValue)
			throws JMException, InvalidTargetObjectTypeException {

		if (mapValue instanceof String) {
			// Bean name pointing to a potentially lazy-init bean in the factory.
			if (this.beanFactory == null) {
				throw new JMException("Cannot resolve bean names if not running in a BeanFactory");
			}

			String beanName = (String) mapValue;
			if (isBeanDefinitionLazyInit(this.beanFactory, beanName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found bean name for lazy init bean with key [" + beanKey +
							"]. Registering bean with lazy init support.");
				}
				return registerLazyInit(beanKey, beanName);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("String value under key [" + beanKey + "] points to a bean that was not " +
							"registered for lazy initialization. Registering bean normally with JMX server.");
				}
				Object bean = this.beanFactory.getBean(beanName);
				return registerBeanInstance(beanKey, bean);
			}
		}

		else {
			// Plain bean instance -> register it directly.
			return registerBeanInstance(beanKey, mapValue);
		}
	}

	/**
	 * Registers an existing MBean or an MBean adapter for a plain bean
	 * with the <code>MBeanServer</code>.
	 * @param beanKey the key associated with this bean in the beans map
	 * @param beanInstance the bean to register, either an MBean or a plain bean
	 * @return the <code>ObjectName</code> under which the bean was registered
	 * with the <code>MBeanServer</code>
	 * @throws JMException an error in the underlying JMX infrastructure
	 */
	private ObjectName registerBeanInstance(String beanKey, Object beanInstance)
			throws JMException, InvalidTargetObjectTypeException {

		if (JmxUtils.isMBean(beanInstance.getClass())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Located MBean under key [" + beanKey + "]: registering with JMX server");
			}
			return registerMBean(beanKey, beanInstance);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Located bean under key [" + beanKey + "] registering with JMX server.");
			}
			return registerSimpleBean(beanKey, beanInstance);
		}
	}

	/**
	 * Registers an existing MBean with the <code>MBeanServer</code>.
	 * @param beanKey the key associated with this bean in the beans map
	 * @param mbean the bean to register
	 * @return the <code>ObjectName</code> under which the bean was registered
	 * with the <code>MBeanServer</code>
	 * @throws JMException an error in the underlying JMX infrastructure
	 */
	private ObjectName registerMBean(String beanKey, Object mbean) throws JMException {
		try {
			ObjectName objectName = this.namingStrategy.getObjectName(mbean, beanKey);

			if (logger.isDebugEnabled()) {
				logger.debug("Registering MBean [" + objectName + "]");
			}
			this.server.registerMBean(mbean, objectName);
			return objectName;
		}
		catch (MalformedObjectNameException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to auto-register MBean [" + mbean + "] with key [" + beanKey + "]");
			}

			return null;
		}
	}

	/**
	 * Registers a plain bean as MBean with the <code>MBeanServer</code>.
	 * The management interface for the bean is created by the configured
	 * <code>MBeanInfoAssembler</code>.
	 * @param beanKey the key associated with this bean in the beans map
	 * @param bean the bean to register
	 * @return the <code>ObjectName</code> under which the bean was registered
	 * with the <code>MBeanServer</code>
	 * @throws JMException in case of an error in the underlying JMX infrastructure
	 * @throws InvalidTargetObjectTypeException an error in the definition of the MBean resource
	 */
	private ObjectName registerSimpleBean(String beanKey, Object bean)
			throws JMException, InvalidTargetObjectTypeException {

		ObjectName objectName = this.namingStrategy.getObjectName(bean, beanKey);
		if (logger.isDebugEnabled()) {
			logger.debug("Registering and assembling MBean [" + objectName + "]");
		}

		ModelMBean mbean = createModelMBean();
		Class beanClass = getBeanClass(bean);
		mbean.setModelMBeanInfo(this.assembler.getMBeanInfo(beanKey, beanClass));
		mbean.setManagedResource(bean, "ObjectReference");

		this.server.registerMBean(mbean, objectName);
		return objectName;
	}

	/**
	 * Registers beans that are configured for lazy initialization with the
	 * <code>MBeanServer<code> indirectly through a proxy.
	 * @param beanKey the key associated with this bean in the beans map
	 * @return the <code>ObjectName</code> under which the bean was registered
	 * with the <code>MBeanServer</code>
	 * @throws JMException an error in the underlying JMX infrastructure
	 * @throws InvalidTargetObjectTypeException an error in the definition of the MBean resource
	 */
	private ObjectName registerLazyInit(String beanKey, String beanName)
			throws JMException, InvalidTargetObjectTypeException {

		LazyInitTargetSource targetSource = new LazyInitTargetSource();
		targetSource.setTargetBeanName(beanName);
		targetSource.setBeanFactory(this.beanFactory);

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(targetSource);
		proxyFactory.setProxyTargetClass(true);
		proxyFactory.setFrozen(true);

		Object proxy = proxyFactory.getProxy();
		ObjectName objectName = this.namingStrategy.getObjectName(proxy, beanKey);
		if (logger.isDebugEnabled()) {
			logger.debug("Registering lazy-init MBean [" + objectName + "]");
		}

		ModelMBean mbean = createModelMBean();
		mbean.setModelMBeanInfo(this.assembler.getMBeanInfo(beanKey, targetSource.getTargetClass()));
		mbean.setManagedResource(proxy, "ObjectReference");

		this.server.registerMBean(mbean, objectName);
		return objectName;
	}


	/**
	 * Attempts to detect any beans defined in the <code>ApplicationContext</code> that are
	 * valid MBeans and registers them automatically with the <code>MBeanServer</code>.
	 */
	private void autodetectMBeans() {
		autodetect(new AutodetectCallback() {
			public boolean include(String beanName, Class beanClass) {
				return JmxUtils.isMBean(beanClass);
			}
		});
	}

	/**
	 * Invoked when using an <code>AutodetectCapableMBeanInfoAssembler</code>.
	 * Gives the assembler the opportunity to add additional beans from the
	 * <code>BeanFactory</code> to the list of beans to be exposed via JMX.
	 * <p>This implementation prevents a bean from being added to the list
	 * automatically if it has already been added manually, and it prevents
	 * certain internal classes from being registered automatically.
	 */
	private void autodetectBeans(final AutodetectCapableMBeanInfoAssembler assembler) {
		autodetect(new AutodetectCallback() {
			public boolean include(String beanName, Class beanClass) {
				return (beanClass != null && assembler.includeBean(beanName, beanClass));
			}
		});
	}

	/**
	 * Performs the actual autodetection process, delegating to an instance
	 * <code>AutodetectCallback</code> to vote on the inclusion of a given bean.
	 * @param callback the <code>AutodetectCallback</code> to use when deciding
	 * whether to include a bean or not
	 */
	private void autodetect(AutodetectCallback callback) {
		String[] beanNames = this.beanFactory.getBeanNamesForType(null);
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			Class beanClass = this.beanFactory.getType(beanName);
			if (callback.include(beanName, beanClass)) {
				boolean lazyInit = isBeanDefinitionLazyInit(this.beanFactory, beanName);
				Object beanInstance = (!lazyInit ? this.beanFactory.getBean(beanName) : null);
				if (!this.beans.containsValue(beanName) &&
						(beanInstance == null || !this.beans.containsValue(beanInstance))) {
					// Not already registered for JMX exposure.
					this.beans.put(beanName, (beanInstance != null ? beanInstance : beanName));
					if (logger.isInfoEnabled()) {
						logger.info("Bean with name '" + beanName + "' has been autodetected for JMX exposure");
					}
				}
				else {
					if (logger.isInfoEnabled()) {
						logger.debug("Bean with name '" + beanName + "' is already registered for JMX exposure");
					}
				}
			}
		}
	}


	/**
	 * Return whether the specified bean definition should be considered as lazy-init.
	 * @param beanFactory the bean factory that is supposed to contain the bean definition
	 * @param beanName the name of the bean to check
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#getBeanDefinition
	 * @see org.springframework.beans.factory.config.BeanDefinition#isLazyInit
	 */
	protected boolean isBeanDefinitionLazyInit(ListableBeanFactory beanFactory, String beanName) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			return false;
		}
		try {
			BeanDefinition bd = ((ConfigurableListableBeanFactory) beanFactory).getBeanDefinition(beanName);
			return bd.isLazyInit();
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Probably a directly registered singleton.
			return false;
		}
	}

	/**
	 * Return the actual bean class of the given bean instance.
	 * This is the class exposed to the <code>MBeanInfoAssembler</code>
	 * (which, for example, might check the given class for annotations).
	 * @param bean the bean instance (might be an AOP proxy)
	 * @return the bean class to expose
	 * @see org.springframework.jmx.export.assembler.MBeanInfoAssembler#getMBeanInfo(String, Class)
	 * @see org.springframework.aop.framework.Advised#getTargetSource
	 * @see org.springframework.aop.TargetSource#getTargetClass
	 */
	protected Class getBeanClass(Object bean) {
		if (bean instanceof Advised) {
			return ((Advised) bean).getTargetSource().getTargetClass();
		}
		return bean.getClass();
	}

	/**
	 * Create an instance of a class that implements <code>ModelMBean</code>.
	 * <p>This method is called to obtain a <code>ModelMBean</code> instance to
	 * use when registering a bean. This method is called once per bean during the
	 * registration phase and must return a new instance of <code>ModelMBean</code>
	 * @return a new instance of a class that implements <code>ModelMBean</code>
	 * @throws MBeanException if creation of the ModelMBean failed
	 */
	protected ModelMBean createModelMBean() throws MBeanException {
		return new RequiredModelMBean();
	}


	/**
	 * Unregisters all beans that this exported has exposed via JMX
	 * when the enclosing <code>ApplicationContext</code> is destroyed.
	 */
	public void destroy() {
		unregisterBeans();
	}

	/**
	 * Unregisters all beans that this exported has exposed via JMX.
	 */
	private void unregisterBeans() {
		logger.info("Unregistering JMX-exposed beans on shutdown");
		for (Iterator it = this.registeredBeans.iterator(); it.hasNext();) {
			ObjectName objectName = (ObjectName) it.next();
			try {
				this.server.unregisterMBean(objectName);
			}
			catch (JMException ex) {
				logger.error("Could not unregister MBean [" + objectName + "]", ex);
			}
		}
		this.registeredBeans.clear();
	}


	/**
	 * Internal callback interface for the autodetection process.
	 */
	private static interface AutodetectCallback {

		/**
		 * Called during the autodetection process to decide whether
		 * or not a bean should be include.
		 * @param beanName the name of the bean
		 * @param beanClass the class of the bean
		 */
		boolean include(String beanName, Class beanClass);
	}

}
