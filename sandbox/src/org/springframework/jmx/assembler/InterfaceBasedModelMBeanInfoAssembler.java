package org.springframework.jmx.assembler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author Marcus Brito
 * @author Rob Harrop
 */
public class InterfaceBasedModelMBeanInfoAssembler extends ReflectiveModelMBeanInfoAssembler implements InitializingBean {

    private Map mappedManagedInterfaces;

	private Class[] managedInterfaces;

	public void setManagedInterfaces(Class[] managedInterfaces) {
		this.managedInterfaces = managedInterfaces;
	}

    public void setMappedManagedInterfaces(Map mappedManagedInterfaces) {
        this.mappedManagedInterfaces = mappedManagedInterfaces;
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
        if(mappedManagedInterfaces != null) {
            ifaces = (Class[])mappedManagedInterfaces.get(beanKey);
        }

        if(ifaces == null) {
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
	}

	private boolean isPublic(Method method) {
		return (method.getModifiers() & Modifier.PUBLIC) > 0;
	}
}
