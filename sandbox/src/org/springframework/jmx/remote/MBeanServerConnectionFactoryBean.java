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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author robh
 *
 */
public class MBeanServerConnectionFactoryBean implements FactoryBean,
		InitializingBean {

	private String serviceUrl;
	
	private JMXServiceURL url;
	
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public Object getObject() throws Exception {
		//  create the connecto and return the connection
		JMXConnector connector = JMXConnectorFactory.connect(url);
		return connector.getMBeanServerConnection();
	}

	public Class getObjectType() {
		return MBeanServerConnection.class;
	}

	public boolean isSingleton() {
		return false;
	}


	public void afterPropertiesSet() throws Exception {
		if((serviceUrl == null) || (serviceUrl.length() == 0)) {
			throw new IllegalArgumentException("You must specify a value for the serviceUrl property");
		}
		
		// parse the url now to save time later
		url = new JMXServiceURL(serviceUrl);

	}

}
