/*
 * Created on Sep 26, 2004
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
