package org.springframework.aop.framework.support;

import org.aopalliance.intercept.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.InvokerInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;

/**
 * BeanPostProcessor implementation that wraps a group of beans with AOP proxies
 * that delegate to the given interceptors before invoking the bean itself.
 *
 * <p>This is articularly useful if there's a large number of beans that need
 * to get wrapped with similar proxies, i.e. delegating to the same interceptors.
 * Instead of x repetitive proxy definitions for x target beans, you can register
 * one single such post processor with the bean factory to achieve the same effect.
 *
 * <p>Subclasses can apply any strategy to decide if a bean is one to proxy,
 * e.g. by type, by name, by definition details, etc. The default concrete
 * implementation is BeanNameAutoProxyCreator, identifying the beans to proxy
 * via a list of names.
 *
 * @author Juergen Hoeller
 * @since 13.10.2003
 * @see #setInterceptors
 * @see BeanNameAutoProxyCreator
 */
public abstract class AbstractAutoProxyCreator implements BeanPostProcessor, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Interceptor[] interceptors;

	private boolean proxyInterfacesOnly = true;

	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	/**
	 * Set the interceptors that the automatic proxies should delegate to
	 * before invoking the bean itself.
	 */
	public void setInterceptors(Interceptor[] interceptors) {
		this.interceptors = interceptors;
	}

	/**
	 * Set if the proxy should only implement the interfaces of the target.
	 * If this is false, a dynamic runtime subclass of the target will be
	 * created via CGLIB, castable to the target class. Default is true.
	 */
	public void setProxyInterfacesOnly(boolean proxyInterfacesOnly) {
		this.proxyInterfacesOnly = proxyInterfacesOnly;
	}

	/**
	 * Create a proxy with the configured interceptors if the bean is
	 * identified as one to proxy by the subclass.
	 * @see #isBeanToProxy
	 */
	public Object postProcessBean(Object bean, String name, RootBeanDefinition definition) throws BeansException {
		if (isBeanToProxy(bean, name, definition)) {
			logger.info("Creating implicit proxy for bean '" +  name + "'");
			ProxyFactory proxyFactory = new ProxyFactory();
			if (this.interceptors != null) {
				for (int i = 0; i < this.interceptors.length; i++) {
					proxyFactory.addInterceptor(this.interceptors[i]);
				}
			}
			proxyFactory.addInterceptor(new InvokerInterceptor(bean));
			if (this.proxyInterfacesOnly) {
				proxyFactory.setInterfaces(AopUtils.getAllInterfaces(bean));
			}
			return proxyFactory.getProxy();
		}
		else {
			return bean;
		}
	}

	/**
	 * Return whether the given bean is a one to proxy.
	 * If this returns true, postProcessBean will create a proxy
	 * with the configured interceptors for the given bean.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @param definition the definition that the bean was created with
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see #postProcessBean
	 */
	protected abstract boolean isBeanToProxy(Object bean, String name, RootBeanDefinition definition)
	    throws BeansException;

}
