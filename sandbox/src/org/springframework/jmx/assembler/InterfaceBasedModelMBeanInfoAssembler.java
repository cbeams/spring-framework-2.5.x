
package org.springframework.jmx.assembler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Marcus Brito
 * @author Rob Harrop
 */
public class InterfaceBasedModelMBeanInfoAssembler extends ReflectiveModelMBeanInfoAssembler implements InitializingBean {

	private Properties mappings;

	private Map convertedMappings;

	private Class[] managedInterfaces;

	public void setManagedInterfaces(Class[] managedInterfaces) {
		this.managedInterfaces = managedInterfaces;
	}

	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}

	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	protected boolean includeOperation(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}


	private boolean isPublicInInterface(Method method, String beanKey) {
		return isDeclaredInInterface(method, beanKey) && isPublic(method);
	}

	private boolean isDeclaredInInterface(Method method, String beanKey) {

		Class[] ifaces = null;

		if (convertedMappings != null) {
			ifaces = (Class[]) convertedMappings.get(beanKey);
		}

		if (ifaces == null) {
			ifaces = managedInterfaces;
		}

		if (ifaces == null || ifaces.length == 0) {
			ifaces = method.getDeclaringClass().getInterfaces();
		}

		for (int i = 0; i < ifaces.length; i++) {
			Method[] methods = ifaces[i].getDeclaredMethods();

			for (int j = 0; j < methods.length; j++) {
				Method ifaceMethod = methods[j];
				if (ifaceMethod.getName().equals(method.getName()) && Arrays.equals(ifaceMethod.getParameterTypes(), method.getParameterTypes())) {
					return true;
				}
			}
		}

		return false;
	}

	public void afterPropertiesSet() throws Exception {
		if (managedInterfaces != null) {
			for (int x = 0; x < managedInterfaces.length; x++) {
				if (!managedInterfaces[x].isInterface()) {
					throw new IllegalArgumentException(managedInterfaces[x].getName() + " is not an interface.");
				}
			}
		}

		if (mappings != null) {
			convertedMappings = new HashMap();

			for (Enumeration en = mappings.keys(); en.hasMoreElements();) {
				String beanKey = (String) en.nextElement();

				String[] classNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));

				Class[] classes = convertToClasses(beanKey, classNames);

				convertedMappings.put(beanKey, classes);
			}

			// no need to keep properties hanging around
			mappings.clear();
			mappings = null;
		}
	}

	private Class[] convertToClasses(String beanKey, String[] names) {
		Class[] classes = new Class[names.length];

		for (int x = 0; x < classes.length; x++) {
			try {
				Class cls = ClassUtils.forName(names[x].trim());

				if (!cls.isInterface()) {
					throw new ApplicationContextException("Class [" + names[x] + "] mapped to beanKey ["
							+ beanKey + "] is not an interface.");
				}

				classes[x] = cls;
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException("Class [" + names[x] + "] mapped to beanKey ["
						+ beanKey + "] cannot be found.", ex);
			}
		}
		return classes;
	}

	private boolean isPublic(Method method) {
		return (method.getModifiers() & Modifier.PUBLIC) > 0;
	}
}
