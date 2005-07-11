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
 * Helper class that simplifies interaction with a JMX <code>MBeanServer</code>. If no <code>MBeanServer</code> is
 * specified, either in the constructor or via the <code>server</code> propery, then <code>JmxTemplate</code> will
 * attempt to locate a running <code>MBeanServer</code> in the current VM when the first operation occurs.
 * <p/>
 * Operations are encapsulated as implementations of the <code>JmxCallback</code> interface and passed to the
 * <code>execute</code> method for processing. User code in the <code>JmxCallback</code> can throw any
 * <code>JMException</code> which will be wrapped by <code>JmxTemplate</code> in an unchecked <code>JmxException</code>.
 * 
 * @author Rob Harrop
 * @see #setServer(javax.management.MBeanServer)
 * @see org.springframework.jmx.JmxTemplate#JmxTemplate(javax.management.MBeanServer)
 * @see org.springframework.jmx.support.JmxUtils#locateMBeanServer()
 * @see org.springframework.jmx.support.JmxUtils#convertJMException(javax.management.JMException)
 * @see org.springframework.jmx.JmxException
 */
public class JmxTemplate {

	/**
	 * The <code>MBeanServer</code> wrapped by this template. All operations are executed against this
	 * <code>MBeanServer</code> instance.
	 */
	private MBeanServer server;

	/**
	 * Creates a <code>JmxTemplate</code> with no explicit <code>MBeanServer</code> reference
	 */
	public JmxTemplate() {
	}

	/**
	 * Create a <code>JmxTemplate</code> for executing operations against the specified <code>MBeanServer</code>.
	 * @param server the <code>MBeanServer</code> to execute operations against.
	 */
	public JmxTemplate(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Set the <code>MBeanServer</code> this template will execute operations against.
	 * @param server the <code>MBeanServer</code>, may be null
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Register the provided object as an MBean under the specified <code>ObjectName</code>.
	 * @param mbean the object to be registered
	 * @param objectName the <code>ObjectName</code> to register the MBean under.
	 * @return the registered <code>ObjectInstance</code>
	 * @see MBeanServer#registerMBean(Object, javax.management.ObjectName)
	 */
	public ObjectInstance registerMBean(final Object mbean, final ObjectName objectName) {
		return (ObjectInstance) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.registerMBean(mbean, objectName);
			}
		});
	}

	/**
	 * Unregister the MBean identified by the specified <code>ObjectName</code>.
	 * @param objectName the <code>ObjectName</code> of the MBean to unregister.
	 * @see MBeanServer#unregisterMBean(javax.management.ObjectName)
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
	 * Get the value of the attribute specified from the MBean identified by the given <code>ObjectName</code>.
	 * @param objectName the <code>ObjectName</code> of the MBean from which to read the attribute value
	 * @param attribute the name of the attribute to read
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
	 * Sets a single attribute on the MBean identified by the given <code>ObjectName</code> to the value specified.
	 * @param objectName the <code>ObjectName</code> of the MBean to update
	 * @param attribute the name of the attribute to update
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
	 * Sets a list of attributes on the MBean identified by the given <code>ObjectName</code> to the values provided.
	 * @param objectName the <code>ObjectName</code> of the MBean to update
	 * @param attributes the <code>AttributeList</code> containing the names and values of the attributes to update
	 * @return an <code>AttributeList</code> containing the names and values of the attributes that were successfully updated
	 */
	public AttributeList setAttributes(final ObjectName objectName, final AttributeList attributes) {
		return (AttributeList) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.setAttributes(objectName, attributes);
			}
		});
	}

	/**
	 * Returns the default domain of the wrapped <code>MBeanServer</code>.
	 * @return the default domain name
	 */
	public String getDefaultDomain() {
		return (String) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) {
				return server.getDefaultDomain();

			}
		});
	}

	/**
	 * Returns the number of MBeans registered in the wrapped <code>MBeanServer</code>.
	 * @return the number of MBeans
	 */
	public Integer getMBeanCount() {
		return (Integer) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) {
				return server.getMBeanCount();
			}
		});
	}

	/**
	 * Returns the management metadata associated with the MBean specified by the supplied <code>ObjectName</code>.
	 * @param objectName the <code>ObjectName</code> of the MBean
	 * @return the MBean's management metadata descriptor
	 */
	public MBeanInfo getMBeanInfo(final ObjectName objectName) {
		return (MBeanInfo) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getMBeanInfo(objectName);
			}
		});
	}

	/**
	 * Returns the <code>ObjectInstance</code> for the MBean with the provided <code>ObjectName</code>.
	 * @param objectName the <code>ObjectName</code> of the MBean
	 * @return the object instance
	 */
	public ObjectInstance getObjectInstance(final ObjectName objectName) {
		return (ObjectInstance) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getObjectInstance(objectName);
			}
		});
	}

	/**
	 * Returns all MBeans that match the provided <code>ObjectName</code> pattern and query expression.
	 * @param objectName the <code>ObjectName</code> pattern (may be null)
	 * @param query the query expression
	 * @return the set of matched MBeans
	 */
	public Set queryMBeans(final ObjectName objectName, final QueryExp query) {
		return (Set) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) {
				return server.queryMBeans(objectName, query);
			}
		});
	}

	/**
	 * Queries all MBeans that match the provided query expression, returning a set of matching
	 * <code>ObjectName</code>s.
	 * @param objectName the <code>ObjectName</code> pattern
	 * @param query the query expression
	 * @return the set of matched MBean <code>ObjectName</code>s
	 */
	public Set queryNames(final ObjectName objectName, final QueryExp query) {
		return (Set) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) {
				return server.queryNames(objectName, query);
			}
		});
	}

	/**
	 * Execute the supplied <code>JmxCallback</code> using the wrapped <code>MBeanServer</code>. All
	 * <code>JMException</code>s are wrapped in unchecked <code>JmxExceptions</code>.
	 * @param callback the <code>JmxCallback</code> to execute
	 * @return the object returned by the supplied <code>JmxCallback</code>. May be <code>null</code>.
	 * @see #convertJMException(javax.management.JMException)
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
	 * Returns the wrapped <code>MBeanServer</code>. If no <code>MBeanServer</code> has been configured
	 * this method will attempt to locate one. If an <code>MBeanServer</code> is located, it will be cached
	 * and used for all subsequent invocations on this <code>JmxTemplate</code> instance.
	 * @return the mbean server to execute against
	 */
	protected MBeanServer getMBeanServer() {
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}
		return this.server;
	}

	/**
		 * Converts a checked <code>JMException</code> into a unchecked <code>JmxException</code>.
		 * @param ex the <code>JMException</code>
		 * @return the corresponding <code>JmxException</code>
		 * @see JmxUtils#convertJMException(JMException)
		 */
	protected JmxException convertJMException(JMException ex) {
		return JmxUtils.convertJMException(ex);
	}
}

