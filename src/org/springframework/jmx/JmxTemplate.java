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

package org.springframework.jmx;

import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;

import org.springframework.jmx.support.JmxUtils;

/**
 * Helper class that simplifies JMX MBeanServer access code. 
 * @author Rob Harrop
 */
public class JmxTemplate {
	
	/**
	 * The MBeanServer wrapped by this template; the one this template will execute its
	 * operations against. 
	 */
	private MBeanServer server;

	/**
	 * Default constructor for a JmxTemplate. Note: if the "server" property is not set
	 * explicitly this template will attempt to obtain a reference to the first MBeanServer 
	 * present in the running Java VM.
	 */
	public JmxTemplate() {
	}

	/**
	 * Create a template for executing operations against the specified MBeanServer.
	 * @param server the MBeanServer to execute operations against.
	 */
	public JmxTemplate(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Set the MBeanServer this template will execute operations against.
	 * @param server the MBeanServer, may be null
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Register the provided object as an MBean in the wrapped MBeanServer 
	 * with the provided ObjectName.
	 * @param mbean the object to be registered
	 * @param objectName the name the object will be identified by in the mbean server
	 * @return the registered ObjectInstance
	 */
	public ObjectInstance registerMBean(final Object mbean, final ObjectName objectName) {
		return (ObjectInstance)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.registerMBean(mbean, objectName);
			}
		});
	}

	/**
	 * Unregister an MBean with the provided object name in the MBeanServer wrapped by this
	 * template.
	 * @param objectName the object name.
	 */
	public void unregisterMBean(final ObjectName objectName) {
		execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				server.unregisterMBean(objectName);
				return null;
			}
		});
	}

	/**
	 * Get the value of the attribute provided on the MBean identified by the provided object name.
	 * @param objectName the object name
	 * @param attribute the attribute name
	 * @return the attribute value, typically of a pervasive type.
	 */
	public Object getAttribute(final ObjectName objectName, final String attribute) {
		return execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getAttribute(objectName, attribute);
			}
		});
	}

	/**
	 * Get the list of attribute values on the MBean identified by the provided object name.
	 * @param objectName the object name
	 * @param attributes the attribute names
	 * @return the attribute list
	 */
	public AttributeList getAttributes(final ObjectName objectName, final String[] attributes) {
		return (AttributeList) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getAttributes(objectName, attributes);
			}
		});
	}

	/**
	 * Sets a single attribute on the MBean with the provided object name to the value provided.
	 * @param objectName the object name
	 * @param attribute the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(final ObjectName objectName, final String attribute, final Object value) {
		execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				server.setAttribute(objectName, new Attribute(attribute, value));
				return null;
			}
		});
	}

	/**
	 * Sets a list of attributes on the MBean with the provided object name to the valujes provided.
	 * @param objectName the object name
	 * @param attributes the attribute list
	 * @return the attribute list of those whose values were updated
	 */
	public AttributeList setAttributes(final ObjectName objectName, final AttributeList attributes) {
		return (AttributeList) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.setAttributes(objectName, attributes);
			}
		});
	}

	/**
	 * Returns the default domain of the wrapped MBeanServer.
	 * @return the default domain name
	 */
	public String getDefaultDomain() {
		return (String) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getDefaultDomain();

			}
		});
	}
	
	/**
	 * Returns the number of MBeans registered in the wrapped MBeanServer.
	 * @return the number of MBeans
	 */
	public Integer getMBeanCount() {
		return (Integer) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getMBeanCount();
			}
		});
	}

	/**
	 * Returns the management metadata associated with the MBean registered in the wrapped 
	 * MBeanServer with the provided object name.
	 * @param objectName the MBean's object name
	 * @return the MBean's management metadata descriptor (an MBeanInfo)
	 */
	public MBeanInfo getMBeanInfo(final ObjectName objectName) {
		return (MBeanInfo) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getMBeanInfo(objectName);
			}
		});
	}

	/**
	 * Returns the ObjectInstance for the MBean with the provided object name.
	 * @param objectName the object name
	 * @return the object instance
	 */
	public ObjectInstance getObjectInstance(final ObjectName objectName) {
		return (ObjectInstance)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getObjectInstance(objectName);
			}
		});
	}

	/**
	 * Queryies all MBeans that match the provided query expression.
	 * @param objectName the object name pattern (may be null)
	 * @param query the query expression
	 * @return the set of matched MBeans
	 */
	public Set queryMBeans(final ObjectName objectName, final QueryExp query) {
		return (Set)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.queryMBeans(objectName, query);
			}
		});
	}

	/**
	 * Queries all MBeans that match the provided query expression, returning a set of matching
	 * ObjectNames.
	 * @param objectName the object name pattern (may be null)
	 * @param query the query expression
	 * @return the set of matched MBean ObjectNames
	 */
	public Set queryNames(final ObjectName objectName, final QueryExp query) {
		return (Set)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.queryNames(objectName, query);
			}
		});
	}

	/**
	 * Most generic template method, accepting a JmxCallback that is expected to execute
	 * custom logic against MBeanServer wrapped by this template.
	 * @param callback the callback
	 * @return the object returned by the callback (may be null)
	 */
	public Object execute(JmxCallback callback) {
		MBeanServer server = getMBeanServer();
		try {
			return callback.doWithMBeanServer(server);
		}
		catch (JMException ex) {
			throw convertJMException(ex);
		}
	}

	/**
	 * Returns the wrapped MBeanServer.
	 * @return the mbean server to execute against
	 */
	protected MBeanServer getMBeanServer() {
		return (this.server != null) ? server : JmxUtils.locateMBeanServer();
	}

	/**
	 * Converts a checked JMException into a strongly-typed unchecked JmxException.
	 * @param ex the JMException
	 * @return the corresponding JmxException
	 */
	protected JmxException convertJMException(JMException ex) {
		return JmxUtils.convertJMException(ex);
	}
}
