package org.springframework.aop.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author Rob Harrop
 */
class MethodLocatingFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

	private BeanFactory beanFactory;

	private String beanName;

	private String methodName;

	private Method method;

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object getObject() throws Exception {
		return this.method;
	}

	public Class getObjectType() {
		return Method.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		if(!StringUtils.hasText(this.beanName)) {
			throw new IllegalArgumentException("Property [beanName] is required.");
		}

		if(!StringUtils.hasText(this.methodName)) {
			throw new IllegalArgumentException("Property [methodName] is required.");
		}

		Class beanClass = this.beanFactory.getBean(this.beanName).getClass();

		Method[] methods = beanClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if(this.methodName.equals(method.getName())) {
				this.method = method;
			}
		}

		if(this.method == null) {
			throw new IllegalArgumentException("Unable to locate method [" 
					+ this.methodName + "] on bean [" + this.beanName + "].");
		}
	}
}
