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
package org.springframework.jmx.remote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;
import org.springframework.jmx.util.JmxUtils;

/**
 * @author Rob Harrop
 */
public class ConnectorServiceBean implements InitializingBean {

	private MBeanServer mbeanServer;

	private JMXConnectorServer connectorServer;

	private boolean registerConnectorAsMBean = false;

	private String objectName;

	private String serviceUrl = "service:jmx:jmxmp://localhost:9876";

	private Map environment;

	public void setServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

	public void setRegisterConnectorAsMBean(boolean registerConnectorAsMBean) {
		this.registerConnectorAsMBean = registerConnectorAsMBean;
	}

	public void setEnvironment(Map environment) {
		this.environment = environment;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getServiceUrl() {
		return this.serviceUrl;
	}

	public void afterPropertiesSet() throws Exception {
		start();
	}

	public void start() throws MalformedURLException, IOException, JMException {
		if (mbeanServer == null) {
			this.mbeanServer = JmxUtils.locateMBeanServer();
		}

		ObjectName oname = null;

		// check that the object name is specified
		if ((registerConnectorAsMBean)) {
			if (objectName == null) {
				throw new ObjectNamingException(
						"Must set value for property, objectName, "
								+ "when registerConnectorAsBean is true.");
			} else {
				try {
					oname = ObjectNameManager.getInstance(objectName);
				} catch (MalformedObjectNameException ex) {
					throw new ObjectNamingException("ObjectName: " + objectName
							+ " is malformed.", ex);
				}
			}
		}

		// create the service url
		JMXServiceURL url = new JMXServiceURL(serviceUrl);

		// create the connector now
		connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url,
				environment, mbeanServer);

		// do we want to register the connector with the mbean server?
		if (registerConnectorAsMBean) {
			mbeanServer.registerMBean(connectorServer, oname);
		}

		// now start the connector
		connectorServer.start();
	}
}