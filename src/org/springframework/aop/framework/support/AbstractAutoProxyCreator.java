package org.springframework.aop.framework.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.InvokerInterceptor;
import org.springframework.aop.framework.MethodPointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;

/**
 * BeanPostProcessor implementation that wraps a group of beans with AOP proxies
 * that delegate to the given interceptors before invoking the bean itself.
 * Also supports additional specific interceptors per bean instance.
 *
 * <p>This is particularly useful if there's a large number of beans that need
 * to get wrapped with similar proxies, i.e. delegating to the same interceptors.
 * Instead of x repetitive proxy definitions for x target beans, you can register
 * one single such post processor with the bean factory to achieve the same effect.
 *
 * <p>Subclasses can apply any strategy to decide if a bean is to be proxied,
 * e.g. by type, by name, by definition details, etc. They can also return
 * additional interceptors that should just be applied to the specific bean
 * instance. The default concrete implementation is BeanNameAutoProxyCreator,
 * identifying the beans to be proxied via a list of bean names.
 *
 * @author Juergen Hoeller
 * @since 13.10.2003
 * @see #setInterceptors
 * @see BeanNameAutoProxyCreator
 */
public abstract class AbstractAutoProxyCreator implements BeanPostProcessor, Ordered {

	/**
	 * Convenience constant for subclasses: Return value for "do not proxy".
	 * @see #getInterceptorsAndPointcutsForBean
	 */
	protected final Object[] DO_NOT_PROXY = null;

	/**
	 * Convenience constant for subclasses: Return value for
	 * "proxy without additional interceptors, just the common ones".
	 * @see #getInterceptorsAndPointcutsForBean
	 */
	protected final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Object[] interceptors;

	private boolean applyCommonInterceptorsFirst = true;

	private boolean proxyInterfacesOnly = true;

	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	/**
	 * Set the interceptors and pointcuts that the automatic proxies
	 * should delegate to before invoking the bean itself.
	 */
	public void setInterceptors(Object[] interceptors) {
		this.interceptors = interceptors;
	}

	/**
	 * Set whether the common interceptors should be applied before
	 * bean-specific ones. Default is true; else, bean-specific
	 * interceptors will get applied first.
	 */
	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
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
	 * @see #getInterceptorsAndPointcutsForBean
	 */
	public Object postProcessBean(Object bean, String name, RootBeanDefinition definition) throws BeansException {
		Object[] specificInterceptors = getInterceptorsAndPointcutsForBean(bean, name, definition);
		if (specificInterceptors != null) {
			List allInterceptors = new ArrayList();
			allInterceptors.addAll(Arrays.asList(specificInterceptors));
			if (this.interceptors != null) {
				if (this.applyCommonInterceptorsFirst) {
					allInterceptors.addAll(0, Arrays.asList(this.interceptors));
				}
				else {
					allInterceptors.addAll(Arrays.asList(this.interceptors));
				}
			}
			if (logger.isInfoEnabled()) {
				int nrOfCommonInterceptors = this.interceptors != null ? this.interceptors.length : 0;
				logger.info("Creating implicit proxy for bean '" +  name + "' with " + nrOfCommonInterceptors +
										" common interceptors and " + specificInterceptors.length + " specific interceptors");
			}
			ProxyFactory proxyFactory = new ProxyFactory();
			for (Iterator it = allInterceptors.iterator(); it.hasNext();) {
				Object interceptor = it.next();
				if (interceptor instanceof MethodPointcut) {
					proxyFactory.addMethodPointcut((MethodPointcut) interceptor);
				}
				else if (interceptor instanceof Interceptor) {
					proxyFactory.addInterceptor((Interceptor) interceptor);
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
	 * Return whether the given bean is to be proxied,
	 * and what additional interceptors and pointcuts to apply.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @param definition the definition that the bean was created with
	 * @return an array of additional interceptors for the particular bean;
	 * or an empty array if no additional interceptors but just the common ones;
	 * or null if no proxy at all, not even with the common interceptors.
	 * See constants DO_NOT_PROXY and PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS.
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see #postProcessBean
	 * @see #DO_NOT_PROXY
	 * @see #PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
	 */
	protected abstract Object[] getInterceptorsAndPointcutsForBean(Object bean, String name,
	                                                               RootBeanDefinition definition)
	    throws BeansException;

}
