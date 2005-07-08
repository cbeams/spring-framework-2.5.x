package org.springframework.jmx;

import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ObjectInstance;

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

	public void registerMBean(final Object mbean, final ObjectName objectName) {
		execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer server) throws JMException {
				server.registerMBean(mbean, objectName);
				return null;
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

	public Object execute(JmxCallback callback) {
		MBeanServer server = getMBeanServer();

		try {
			return callback.doWithMBeanServer(server);
		}
		catch (JMException ex) {
			// todo: wrap with a real exception
			throw new JmxException
		}
	}

	protected MBeanServer getMBeanServer() {
		return (this.server != null) ? server : JmxUtils.locateMBeanServer();
	}

	protected JmxException convertJMException(JMException ex) {
		return JmxUtils.convertJMException(ex);
	}
}
