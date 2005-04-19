
package org.springframework.jmx.support;

import javax.management.MBeanServer;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassUtils;

/**
 * @author Rob Harrop
 */
public class WebLogicMBeanServerFactoryBean implements FactoryBean {

	private static final String HELPER_CLASS = "weblogic.management.Helper";

	private static final String MBEAN_HOME_CLASS = "weblogic.management.MBeanHome";

	private static final String GET_MBEAN_HOME_METHOD = "getMBeanHome";

	private static final String GET_MBEAN_SERVER_METHOD = "getMBeanServer";

	private String username = "weblogic";

	private String password = "weblogic";

	private String url = null;

	private String serverName = null;

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Object getObject() throws Exception {

		Class helperClass = ClassUtils.forName(HELPER_CLASS);

		Object[] args = new Object[]{username, password, url, serverName};
		Class[] argTypes = new Class[]{String.class, String.class, String.class, String.class};

		Object mbeanHome = helperClass.getMethod(GET_MBEAN_HOME_METHOD, argTypes).invoke(null, args);

		return mbeanHome.getClass().getMethod(GET_MBEAN_SERVER_METHOD, null).invoke(mbeanHome, null);
	}

	public Class getObjectType() {
		return MBeanServer.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
