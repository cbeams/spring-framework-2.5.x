/*
 * Created on Nov 5, 2004
 */
package org.springframework.util.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author robh
 *  
 */
public class CommonsLogProviderBeanPostProcessor implements BeanPostProcessor {

	private static final String SET_METHOD = "setLog";

	private static final Class[] SET_METHOD_TYPES = new Class[] { Log.class };

	private CommonsLogProvider logProvider = new ClassNameCommonsLogProvider();

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		Method m = BeanUtils.findMethod(bean.getClass(), SET_METHOD,
				SET_METHOD_TYPES);

		if ((m != null) && (m.getReturnType() == void.class)) {
			setLog(m, bean, beanName);
		}
		
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	public void setCommonsLogProvider(CommonsLogProvider logProvider) {
		this.logProvider = logProvider;
	}

	private void setLog(Method setLogMethod, Object bean, String beanName) {
		try {
			setLogMethod.invoke(bean, new Object[] { logProvider.getLogForBean(
					bean, beanName) });
		} catch (IllegalAccessException ex) {
			throw new CommonsLogProviderException(
					"Unable to access target bean.", ex);
		} catch (InvocationTargetException ex) {
			throw new CommonsLogProviderException(
					"Exception when invoking setLog method on target bean.", ex);
		}
	}
}