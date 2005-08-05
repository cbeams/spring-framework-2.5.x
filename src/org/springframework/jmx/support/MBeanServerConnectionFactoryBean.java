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

package org.springframework.jmx.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.target.AbstractLazyInitTargetSource;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

/**
 * <code>FactoryBean</code> implementation that creates an <code>MBeanServerConnection</code>
 * to a remote <code>MBeanServer</code> exposed via a <code>JMXServerConnector</code>.
 * Exposes the <code>MBeanServer</code> for bean references.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see MBeanServerFactoryBean
 * @see ConnectorServerFactoryBean
 * @since 1.2
 */
public class MBeanServerConnectionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	/**
	 * The service URL of the remote <code>MBeanServer</code>.
	 */
	private JMXServiceURL serviceUrl;

	/**
	 * Environment parameters to pass to the <code>JMXConnector</code> during construction.
	 */
	private Map environment;

	/**
	 * The <code>JMXConnector</code> connected to the remote <code>MBeanServer</code>.
	 */
	private JMXConnector connector;

	/**
	 * The <code>MBeanServerConnection</code> to the remote <code>MBeanServer</code>.
	 */
	private MBeanServerConnection connection;

	/**
	 * Indicates whether or not the connection to the remote <code>MBeanServer</code> should be established
	 * on startup or when the connection is first accessed.
	 */
	private boolean connectOnStartup = true;

	/**
	 * Set the service URL of the remote <code>MBeanServer</code>.
	 */
	public void setServiceUrl(String url) throws MalformedURLException {
		this.serviceUrl = new JMXServiceURL(url);
	}

	/**
	 * Set the environment properties used to construct the <code>JMXConnector</code>
	 * as <code>java.util.Properties</code> (String key/value pairs).
	 */
	public void setEnvironment(Properties environment) {
		this.environment = environment;
	}

	/**
	 * Set the environment properties used to construct the <code>JMXConnector</code>
	 * as a <code>Map</code> of String keys and arbitrary Object values.
	 */
	public void setEnvironmentMap(Map environment) {
		this.environment = environment;
	}

	public void setConnectOnStartup(boolean connectOnStartup) {
		this.connectOnStartup = connectOnStartup;
	}

	/**
	 * Creates a <code>JMXConnector</code> for the given settings
	 * and exposes the associated <code>MBeanServerConnection</code>.
	 */
	public void afterPropertiesSet() throws IOException {
		if (this.serviceUrl == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}

		if (this.connectOnStartup) {
			connect();
		}
		else {
			createLazyConnection();
		}
	}


	public Object getObject() {
		return this.connection;
	}

	public Class getObjectType() {
		return (this.connection != null ? this.connection.getClass() : MBeanServerConnection.class);
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Closes the underlying <code>JMXConnector</code>.
	 */
	public void destroy() throws IOException {
		this.connector.close();
	}

	/**
	 * Connects to the remote <code>MBeanServer</code> using the configured service URL and
	 * environment properties.
	 */
	private void connect() throws IOException {
		this.connector = JMXConnectorFactory.connect(this.serviceUrl, this.environment);
		this.connection = this.connector.getMBeanServerConnection();
	}

	/**
	 * Creates lazy proxies for the <code>JMXConnector</code> and <code>MBeanServerConnection</code>
	 */
	private void createLazyConnection() {
		TargetSource lazyConnectorSource = new JMXConnectorLazyInitTargetSource();
		TargetSource lazyConnectionSource = new MBeanServerConnectionLazyInitTargetSource();

		this.connector = (JMXConnector) ProxyFactory.getProxy(JMXConnector.class, lazyConnectorSource);
		this.connection = (MBeanServerConnection) ProxyFactory.getProxy(MBeanServerConnection.class, lazyConnectionSource);
	}

	/**
	 * Lazily creates an <code>MBeanServerConnection</code>.
	 */
	private class MBeanServerConnectionLazyInitTargetSource extends AbstractLazyInitTargetSource {

		protected Object createObject() throws Exception {
			return connector.getMBeanServerConnection();
		}

		public Class getTargetClass() {
			return getObjectType();
		}
	}

	/**
	 * Lazily creates a <code>JMXConnector</code> using the configured service URL and
	 * environment properties
	 * @see MBeanServerConnectionFactoryBean#setServiceUrl(String)
	 * @see MBeanServerConnectionFactoryBean#setEnvironment(java.util.Properties)
	 */
	private class JMXConnectorLazyInitTargetSource extends AbstractLazyInitTargetSource {

		protected Object createObject() throws Exception {
			return JMXConnectorFactory.connect(serviceUrl, environment);
		}

		public Class getTargetClass() {
			return (connector == null) ? JMXConnector.class : connector.getClass();
		}
	}
}
