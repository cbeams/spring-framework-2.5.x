package org.springframework.jmx.assembler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author Marcus Brito
 * @author Rob Harrop
 */
public class InterfaceBasedModelMBeanInfoAssembler extends ReflectiveModelMBeanInfoAssembler implements InitializingBean {

	private Class[] managedInterfaces;

	public void setManagedInterfaces(Class[] managedInterfaces) {
		this.managedInterfaces = managedInterfaces;
	}

	protected boolean includeReadAttribute(Method method) {
		return isPublicInInterface(method);
	}

	protected boolean includeWriteAttribute(Method method) {
		return isPublicInInterface(method);
	}

	protected boolean includeOperation(Method method) {
		return isPublicInInterface(method);
	}


	private boolean isPublicInInterface(Method method) {
		return isDeclaredInInterface(method) && isPublic(method);
	}

	private boolean isDeclaredInInterface(Method method) {
		Class[] ifaces = managedInterfaces;

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
	}

	private boolean isPublic(Method method) {
		return (method.getModifiers() & Modifier.PUBLIC) > 0;
	}
}
