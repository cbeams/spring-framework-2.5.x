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
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jmx.assembler.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.jmx.assembler.ModelMBeanInfoAssembler;
import org.springframework.jmx.assembler.ReflectiveModelMBeanInfoAssembler;
import org.springframework.jmx.naming.KeyNamingStrategy;
import org.springframework.jmx.naming.ObjectNamingStrategy;
import org.springframework.jmx.util.JmxUtils;

/**
 * A bean that allows for any Spring-managed to be exposed to an <code>MBeanServer</code>
 * without the need to define any JMX-specific information in the bean classes.
 *
 * <p>If the bean implements one of the JMX management interface then
 * MBeanExporter will simply register the MBean with the server automatically.
 *
 * <p>If the bean does not implement on the JMX management interface then
 * <code>MBeanExporter</code> will create the management information using the
 * supplied <code>ModelMBeanMetadataAssembler</code> implementation.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Marcus Brito
 * @since 1.2
 */
public class MBeanExporter implements InitializingBean, DisposableBean, BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The MBeanServer instance being used to register beans.
	 */
	private MBeanServer server;

	/**
	 * The beans to be exposed as JMX managed resources.
	 */
	private Map beans;

	/**
	 * Stores the <code>ModelMBeanInfoAssembler</code> to use for this adapter.
	 */
	private ModelMBeanInfoAssembler assembler = new ReflectiveModelMBeanInfoAssembler();

	/**
	 * The strategy to use for creating ObjectNames for an object.
	 */
	private ObjectNamingStrategy namingStrategy = new KeyNamingStrategy();

	/**
	 * Stores the <code>ModelMBeanProvider</code> used by this class to obtain
	 * <code>ModelMBean</code> instances.
	 */
	private ModelMBeanProvider mbeanProvider = new RequiredModelMBeanProvider();

	/**
	 * Stores the BeanFactory for use in autodetection process.
	 */
	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * The beans that have been registered by this adapter.
	 */
	private ObjectName[] registeredBeans;


	/**
	 * Specify an instance <code>MBeanServer</code> with which all beans should
	 * be registered. The <code>MBeanExporter</code> will attempt to locate an
	 * existing <code>MBeanServer</code> if none is supplied.
	 * @param server an instance of <code>MBeanServer</code>.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Supply a <code>Map</code> of beans to be registered with the JMX
	 * <code>MBeanServer</code>.
	 * @param beans a <code>Map</code> whose entries are the beans to register via JMX.
	 */
	public void setBeans(Map beans) {
		this.beans = beans;
	}

	/**
	 * Set the implementation of the <code>ModelMBeanInfoAssembler</code> interface
	 * to use for this instance.
	 * @param assembler an implementation of the <code>ModelMBeanInfoAssembler</code> interface.
	 */
	public void setAssembler(ModelMBeanInfoAssembler assembler) {
		this.assembler = assembler;
	}

	/**
	 * Set the implementation of the <code>ObjectNamingStrategy</code> interface to
	 * use for this instance.
	 * @param namingStrategy an implementation of the <code>ObjectNamingStrategy</code> interface.
	 */
	public void setNamingStrategy(ObjectNamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * Set the implementation of the <code>ModelMBeanProvider</code> interface to
	 * use for this instance.
	 * @param mbeanProvider an implementation of the <code>ModelMBeanProvider</code> interface.
	 */
	public void setBeanProvider(ModelMBeanProvider mbeanProvider) {
		this.mbeanProvider = mbeanProvider;
	}

	/**
	 * Implemented to grab the <code>BeanFactory</code> to allow for auto detection of
	 * managed bean resources.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
		}
		else {
			logger.info("Not using a ConfigurableListableBeanFactory - auto-detection of managed beans is disabled");
		}
	}


	public void afterPropertiesSet() throws Exception {
		// register the beans now
		registerBeans();
	}

	/**
	 * Registers the defined beans with the <code>MBeanServer</code>. Each bean is exposed
	 * to the <code>MBeanServer</code> via a <code>ModelMBean</code>. The actual implemetation
	 * of the <code>ModelMBean</code> interface used depends on the implementation of the
	 * <code>ModelMBeanProvider</code> interface that is configuerd. By default the <code>
	 * RequiredModelMBean</code> class that is supplied with all JMX implementations is used. The management
	 * interface produced for each bean is dependent on te <code>ModelMBeanInfoAssembler</code>
	 * implementation being used. The <code>ObjectName</code> given to each bean is dependent on
	 * the implementation of the <code>ObjectNamingStrategy</code> interface being used.
	 */
	protected void registerBeans() throws JMException {

		// If no server was provided then try to load one.
		// This is useful in environment such as
		// JBoss where there is already an MBeanServer loaded
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}

		// The beans property may be null.
		// Initially if we are relying solely on auto-detection.
		if (this.beans == null) {
			this.beans = new HashMap();
		}

		// Allow the metadata assembler a chance to
		// vote for bean inclusion.
		if (this.assembler instanceof AutodetectCapableModelMBeanInfoAssembler) {
			autodetectBeans();
		}

		// Check we now have at least one bean.
		if (this.beans.isEmpty()) {
			throw new IllegalArgumentException("Must specify at least one bean for registration");
		}

		Object[] keys = this.beans.keySet().toArray();
		this.registeredBeans = new ObjectName[keys.length];

		try {
			for (int x = 0; x < keys.length; x++) {
				String key = (String) keys[x];
				Object val = this.beans.get(key);
				ObjectName objectName = registerBean(key, val);
				this.registeredBeans[x] = objectName;
				if (logger.isInfoEnabled()) {
					logger.info("Registered MBean: " + objectName.toString());
				}
			}
		}
		catch (InvalidTargetObjectTypeException ex) {
			// We should never get this!
			logger.error("An invalid object type was used when specifying a managed resource", ex);
			throw new JMException("An invalid object type was used when specifying a managed resource. " +
					"This is a serious error and points to an error in the Spring JMX Code. Root cause: " +
					ex.getMessage());
		}
	}

	private ObjectName registerBean(String beanKey, Object mapValue)
			throws JMException, InvalidTargetObjectTypeException {

		if (mapValue instanceof String) {
			String beanName = (String) mapValue;

			// might be a bean name
			if (logger.isDebugEnabled()) {
				logger.debug("Key '" + beanKey + "' is mapped to String value '" + beanName +
						"' - might be a bean name");
			}

			BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition(beanName);
			if (beanDefinition.isLazyInit()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found bean name for lazy init bean with key [" + beanKey +
							"]. Registering bean with lazy init support.");
				}
				return registerLazyInit(beanKey, beanName);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("String value under key [" + beanKey + "] either did not point to a bean or the " +
							"bean it pointed to was not registered for lazy initialization. " +
							"Registering bean normally with JMX server.");
				}
				Object bean = this.beanFactory.getBean(beanName);
				return registerBean(beanKey, bean);
			}
		}
		else {
			if (isMBean(mapValue)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Located MBean under key [" + beanKey + "] registering with JMX server " +
							"without Spring intervention.");
				}
				return registerMBean(beanKey, mapValue);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Located bean under key [" + beanKey + "] registering with JMX server.");
				}
				return registerSimpleBean(beanKey, mapValue);
			}
		}
	}

	private boolean isMBean(Object object) {
		// TODO: Extend this implementation to cover all user-created MBeans.
		return (object instanceof DynamicMBean);
	}

	private ObjectName registerSimpleBean(String beanKey, Object bean)
			throws JMException, InvalidTargetObjectTypeException {

		ObjectName objectName = this.namingStrategy.getObjectName(bean, beanKey);

		if (logger.isDebugEnabled()) {
			logger.debug("Registering and assembling MBean: " + objectName);
		}

		ModelMBean mbean = this.mbeanProvider.getModelMBean();
		mbean.setModelMBeanInfo(this.assembler.getMBeanInfo(beanKey, bean.getClass()));
		mbean.setManagedResource(bean, "ObjectReference");

		this.server.registerMBean(mbean, objectName);

		return objectName;
	}

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

		ModelMBean mbean = this.mbeanProvider.getModelMBean();
		mbean.setModelMBeanInfo(this.assembler.getMBeanInfo(beanKey, targetSource.getTargetClass()));
		mbean.setManagedResource(proxy, "ObjectReference");

		this.server.registerMBean(mbean, objectName);

		return objectName;
	}

	private ObjectName registerMBean(String beanKey, Object mbean) throws JMException {
		ObjectName objectName = this.namingStrategy.getObjectName(mbean, beanKey);
		this.server.registerMBean(mbean, objectName);
		return objectName;
	}

	/**
	 * Invoked when using an <code>AutodetectCapableModelMBeanInfoAssembler</code>. Gives the
	 * assembler the opportunity to additional beans from the <code>BeanFactory</code> to the list
	 * of beans to be exposed via JMX. This implementation prevents a bean from being added to the
	 * list automatically if it has already been added manually and it prevents certain internal
	 * classes from being registered automatically.
	 */
	private void autodetectBeans() {
		AutodetectCapableModelMBeanInfoAssembler autodetectAssembler =
				(AutodetectCapableModelMBeanInfoAssembler) this.assembler;

		String[] beanNames = this.beanFactory.getBeanDefinitionNames();

		for (int x = 0; x < beanNames.length; x++) {
			String beanName = beanNames[x];
			Class type = this.beanFactory.getType(beanName);

			if (type != null && autodetectAssembler.includeBean(beanName, type)) {
				if (logger.isInfoEnabled()) {
					logger.debug("Bean Name: " + beanName +
							" has been autodetected for JMXification. Instantiating Now.");
				}

				Object bean = this.beanFactory.getBean(beanName);

				if (!this.beans.containsValue(bean)) {
					// not already registered for JMXification
					this.beans.put(beanName, bean);
					if (logger.isInfoEnabled()) {
						logger.debug("Bean with name '" + beanName + "' has been auto-registered for JMXification");
					}
				}
				else {
					if (logger.isInfoEnabled()) {
						logger.debug("Bean with name '" + beanName + "' is already registered for JMXification");
					}
				}
			}
		}
	}

	/**
	 * Unregisters all the beans when the enclosing <code>BeanFactory</code> is destroyed.
	 */
	public void destroy() throws Exception {
		logger.info("Unregistering all JMXified beans on shutdown");
		for (int x = 0; x < this.registeredBeans.length; x++) {
			this.server.unregisterMBean(this.registeredBeans[x]);
		}
	}

}
