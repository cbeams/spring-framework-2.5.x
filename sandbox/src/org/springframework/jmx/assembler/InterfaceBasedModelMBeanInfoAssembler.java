
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
 * Extends the implementation of the <code>ReflectiveModelMBeanInfoAssembler</code> to allow
 * for the management interface of a bean to be defined using arbitrary interfaces.
 * Any methods or properties that are defined in these interfaces are exposed as MBean
 * operations and attributes.
 * <p/>
 * By default, this class votes on the inclusion of each operation or attribute
 * based on the interfaces implemented by the bean class. However, you can supply an array
 * of interfaces via the <code>managedInterfaces</code> property that will be used instead.
 * If you have multiple beans and you wish each bean to use a different set of interfaces then
 * you can map bean keys, that is the name used to pass the bean to the <code>MBeanExporter</code>
 * to a list of interface names using the <code>mappings</code> property.
 * <p/>
 * If you specify values for both <code>mappings</code> and <code>managedInterfaces</code> Spring will
 * attempt to find interfaces in the mappings first. If no interfaces for the bean are found it will
 * use the interfaces defined by the <code>managedInterfaces</code>. If <code>managedInterfaces</code> is
 * <code>null</code> then the interfaces implemented by the bean class are used to create the management
 * interface.
 *
 * @author Marcus Brito
 * @author Rob Harrop
 * @see #setMappings(java.util.Properties)
 * @see #setManagedInterfaces(Class[])
 * @see org.springframework.jmx.MBeanExporter
 * @see ReflectiveModelMBeanInfoAssembler
 */
public class InterfaceBasedModelMBeanInfoAssembler extends ReflectiveModelMBeanInfoAssembler implements InitializingBean {

	/**
	 * Stores the mappings of bean keys to a comma-separated list of interface names.
	 * The property key should match the bean key and the property value should match
	 * the list of interface names.
	 */
	private Properties mappings;

	/**
	 * Stores the mappings of bean keys to an array of <code>Class</code>es. Populated
	 * from the data stored in <code>mappings</code>.
	 *
	 * @see #mappings
	 */
	private Map convertedMappings;

	/**
	 * Stores the array of interfaces to use for creating the management
	 * interface.
	 */
	private Class[] managedInterfaces;

	/**
	 * Sets the array of interfaces to use for creating the management. These
	 * interfaces will be used for a bean if no entry corresponding to that
	 * bean is found in the <code>mappings</code> property.
	 *
	 * @param managedInterfaces an array of <code>Class</code> indicating the interfaces to use.
	 * Each entry <strong>MUST</strong> be an interface.
	 */
	public void setManagedInterfaces(Class[] managedInterfaces) {
		this.managedInterfaces = managedInterfaces;
	}

	/**
	 * Sets the mappings of bean keys to a comma-separated list of interface names.
	 * The property key should match the bean key and the property value should match
	 * the list of interface names. When searching for interfaces for a bean, Spring
	 * will check these mappings first.
	 *
	 * @param mappings the mappins of bean keys to interface names.
	 */
	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}

	/**
	 * Checks to see if the <code>Method</code> is declared in
	 * one of the configured interfaces and that it is public.
	 *
	 * @param method the accessor <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 *         configured interfaces, otherwise <code>false</code>.
	 * @see #isPublicInInterface(java.lang.reflect.Method, String)
	 */
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * Checks to see if the <code>Method</code> is declared in
	 * one of the configured interfaces and that it is public.
	 *
	 * @param method the mutator <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 *         configured interfaces, otherwise <code>false</code>.
	 * @see #isPublicInInterface(java.lang.reflect.Method, String)
	 */
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * Checks to see if the <code>Method</code> is declared in
	 * one of the configured interfaces and that it is public.
	 *
	 * @param method the operation <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 *         configured interfaces, otherwise <code>false</code>.
	 * @see #isPublicInInterface(java.lang.reflect.Method, String)
	 */
	protected boolean includeOperation(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * Checks to see if the <code>Method</code> is both public and declared in
	 * one of the configured interfaces.
	 *
	 * @param method the <code>Method</code> to check.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 *         configured interfaces and is public, otherwise <code>false</code>.
	 */
	private boolean isPublicInInterface(Method method, String beanKey) {
		return isPublic(method) && isDeclaredInInterface(method, beanKey);
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

	/**
	 * Checks to see if the <code>Method</code> is public.
	 * @param method the <code>Method</code> to check.
	 * @return <code>true</code> if the <code>Method</code> is public, else <code>false</code>.
	 */
	private boolean isPublic(Method method) {
		return (method.getModifiers() & Modifier.PUBLIC) > 0;
	}
}
