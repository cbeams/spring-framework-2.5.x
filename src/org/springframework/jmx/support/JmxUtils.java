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

import java.lang.reflect.Method;
import java.util.List;

import javax.management.DynamicMBean;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.util.ClassUtils;

/**
 * Collection of generic utility methods to support Spring JMX.
 * Includes a convenient method to locate an MBeanServer.
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public class JmxUtils {

	private static final String MBEAN_SUFFIX = "MBean";

	private static final Log logger = LogFactory.getLog(JmxUtils.class);

	/**
	 * Attempt to find a locally running <code>MBeanServer</code>. Fails if no
	 * <code>MBeanServer</code> can be found, or if more than one is found.
	 * @return the <code>MBeanServer</code> if found
	 * @throws org.springframework.jmx.MBeanServerNotFoundException
	 * if no <code>MBeanServer</code> is found, or more than one is found
	 */
	public static MBeanServer locateMBeanServer() throws MBeanServerNotFoundException {
		List servers = MBeanServerFactory.findMBeanServer(null);
		// Check to see if an MBeanServer is registered.
		if (servers == null || servers.size() == 0) {
			throw new MBeanServerNotFoundException("Unable to locate an MBeanServer instance");
		}
		if (servers.size() > 1) {
			logger.warn("Found more than one MBeanServer instance. Returning first from list.");
		}
		MBeanServer server = (MBeanServer) servers.get(0);
		if (logger.isDebugEnabled()) {
			logger.debug("Found MBeanServer: " + server);
		}
		return server;
	}

	/**
	 * Convert an array of <code>MBeanParameterInfo</code> into an array of
	 * <code>Class</code> instances corresponding to the parameters.
	 */
	public static Class[] parameterInfoToTypes(MBeanParameterInfo[] paramInfo) throws ClassNotFoundException {
		Class[] types = null;
		if (paramInfo != null && paramInfo.length > 0) {
			types = new Class[paramInfo.length];
			for (int x = 0; x < paramInfo.length; x++) {
				types[x] = ClassUtils.forName(paramInfo[x].getType());
			}
		}
		return types;
	}

	/**
	 * Create a <code>String[]</code> representing the signature of a method.
	 * Each element in the array is the fully qualified class name
	 * of the corresponding argument in the methods signature.
	 */
	public static String[] getMethodSignature(Method method) {
		Class[] types = method.getParameterTypes();
		String[] signature = new String[types.length];
		for (int x = 0; x < types.length; x++) {
			signature[x] = types[x].getName();
		}
		return signature;
	}

	/**
	 * Check whether the supplied <code>Class</code> is a valid MBean resource.
	 * @param beanClass the class of the bean to test
	 */
	public static boolean isMBean(Class beanClass) {
		if (beanClass == null) {
			return false;
		}
		if (DynamicMBean.class.isAssignableFrom(beanClass)) {
			return true;
		}
		Class cls = beanClass;
		while (cls != null && cls != Object.class) {
			if (hasMBeanInterface(cls)) {
				return true;
			}
			cls = cls.getSuperclass();
		}
		return false;
	}

	/**
	 * Return whether an MBean interface exists for the given class
	 * (that is, an interface whose name matches the class name of
	 * the given class but with suffix "MBean).
	 * @param clazz the class to check
	 */
	private static boolean hasMBeanInterface(Class clazz) {
		Class[] implementedInterfaces = clazz.getInterfaces();
		String mbeanInterfaceName = clazz.getName() + MBEAN_SUFFIX;
		for (int x = 0; x < implementedInterfaces.length; x++) {
			if (implementedInterfaces[x].getName().equals(mbeanInterfaceName)) {
				return true;
			}
		}
		return false;
	}

}
