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
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.QueryExp;

import org.springframework.jmx.support.JmxUtils;

/**
 * @author Rob Harrop
 */
public class JmxTemplate {
	private MBeanServer server;

	public JmxTemplate() {
	}

	public JmxTemplate(MBeanServer server) {
		this.server = server;
	}

	public void setServer(MBeanServer server) {
		this.server = server;
	}

	public ObjectInstance registerMBean(final Object mbean, final ObjectName objectName) {
		return (ObjectInstance)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.registerMBean(mbean, objectName);
			}
		});
	}

	public void unregisterMBean(final ObjectName objectName) {
		execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				server.unregisterMBean(objectName);
				return null;
			}
		});
	}

	public Object getAttribute(final ObjectName objectName, final String attribute) {
		return execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getAttribute(objectName, attribute);
			}
		});
	}

	public AttributeList getAttributes(final ObjectName objectName, final String[] attributes) {
		return (AttributeList) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getAttributes(objectName, attributes);
			}
		});
	}

	public void setAttribute(final ObjectName objectName, final String attribute, final Object value) {
		execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				server.setAttribute(objectName, new Attribute(attribute, value));
				return null;
			}
		});
	}

	public AttributeList setAttributes(final ObjectName objectName, final AttributeList attributes) {
		return (AttributeList) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.setAttributes(objectName, attributes);
			}
		});
	}

	public String getDefaultDomain() {
		return (String) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getDefaultDomain();

			}
		});
	}
	public Integer getMBeanCount() {
		return (Integer) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getMBeanCount();
			}
		});
	}

	public MBeanInfo getMBeanInfo(final ObjectName objectName) {
		return (MBeanInfo) execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getMBeanInfo(objectName);
			}
		});
	}

	public ObjectInstance getObjectInstance(final ObjectName objectName) {
		return (ObjectInstance)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.getObjectInstance(objectName);
			}
		});
	}

	public Set queryMBeans(final ObjectName objectName, final QueryExp query) {
		return (Set)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.queryMBeans(objectName, query);
			}
		});
	}

	public Set queryNames(final ObjectName objectName, final QueryExp query) {
		return (Set)execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				return server.queryNames(objectName, query);
			}
		});
	}

	public Object execute(JmxCallback callback) {
		MBeanServer server = getMBeanServer();

		try {
			return callback.doWithMBeanServer(server);
		}
		catch (JMException ex) {
			throw convertJMException(ex);
		}
	}

	protected MBeanServer getMBeanServer() {
		return (this.server != null) ? server : JmxUtils.locateMBeanServer();
	}

	protected JmxException convertJMException(JMException ex) {
		return JmxUtils.convertJMException(ex);
	}
}
