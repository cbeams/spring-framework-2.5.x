/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.interceptor.*;
import org.springframework.aop.support.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanFactoryUtils;
import org.springframework.core.OrderComparator;

/**
 * FactoryBean implementation for use to source AOP proxies from a Spring BeanFactory.
 *
 * <p>Interceptors and Advisors are identified by a list of bean names in the current bean factory.
 * These beans should be of type Interceptor or an Advisor subtype
 * (presently InterceptionAroundAdvisor or InterceptionIntroductionAdvisor). 
 * The last entry in
 * the list can be the name of any bean in the factory. If it's neither an Interceptor
 * or a MethodPointcut, a new InvokerInterceptor is added to wrap it.
 *
 * <p>Global interceptors and advisors can be added at the factory level. The specified ones are
 * expanded in an interceptor list where an "xxx*" entry is included in the list,
 * matching the given prefix with the bean names (e.g. "global*" would match both
 * "globalBean1" and "globalBean2", "*" all defined interceptors). The matching
 * interceptors get applied according to their returned order value, if they
 * implement the Ordered interface. An interceptor name list may not conclude
 * with a global "xxx*" pattern, as global interceptors cannot invoke targets.
 *
 * <p>Creates a J2SE proxy when proxy interfaces are given, a CGLIB proxy for the
 * actual target class if not. Note that the latter will only work if the target class
 * does not have final methods, as a dynamic subclass will be created at runtime.
 *
 * <p>It's possible to cast a proxy obtained from this factory to Advisor,
 * or to obtain the ProxyFactoryBean reference and programmatically
 * manipulate it. This won't work for existing prototype references, which are independent. However,
 * it will work for prototypes subsequently obtained from the factory. Changes to interception
 * will work immediately on singletons (including existing references). However, to change
 * interfaces or target it's necessary to obtain a new instance from
 * the factory. This means that singleton instances obtained from the factory do not
 * have the same object identity. However, they do have the same interceptors and target, and
 * changing any reference will change all objects.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: ProxyFactoryBean.java,v 1.13 2003-11-18 16:32:05 colins Exp $
 * @see #setInterceptorNames
 * @see #setProxyInterfaces
 * @see org.aopalliance.intercept.MethodInterceptor
 * @see org.springframework.aop.InterceptionAroundAdvisor
 * @see org.springframework.aop.InterceptionIntroductionAdvisor
 */
public class ProxyFactoryBean extends AdvisedSupport implements FactoryBean, BeanFactoryAware {

	/**
	 * This suffix in a value in an interceptor list indicates to expand globals.
	 */
	public static final String GLOBAL_SUFFIX = "*";

	private boolean singleton = true;

	/**
	 * Owning bean factory, which cannot be changed after this
	 * object is initialized.
	 */
	private BeanFactory beanFactory;

	/**
	 * Map from PointCut or interceptor to bean name or null,
	 * depending on where it was sourced from. If it's sourced
	 * from a bean name, it will need to be refreshed each time a
	 * new prototype instance is created.
	 */
	private Map sourceMap = new HashMap();

	/**
	 * Names of interceptor and pointcut beans in the factory.
	 * Default is for globals expansion only.
	 */
	private String[] interceptorNames = null;


	/**
	 * Set the names of the interfaces we're proxying. If no interface
	 * is given, a CGLIB for the actual class will be created.
	 */
	public void setProxyInterfaces(String[] interfaceNames) throws AspectException, ClassNotFoundException {
		Class[] interfaces = new Class[interfaceNames.length];
		for (int i = 0; i < interfaceNames.length; i++) {
			interfaces[i] = Class.forName(interfaceNames[i], true, Thread.currentThread().getContextClassLoader());
			// Check it's an interface
			if (!interfaces[i].isInterface())
				throw new AspectException("Can proxy only interfaces: " + interfaces[i] + " is a class");
		}
		setInterfaces(interfaces);
	}

	/**
	 * Set the list of Interceptor/MethodPointcut bean names. This must
	 * always be set to use this factory bean in a bean factory.
	 */
	public void setInterceptorNames(String[] interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		logger.debug("Set BeanFactory. Will configure interceptor beans...");
		createAdvisorChain();
		logger.info("ProxyFactoryBean config: " + this);
	}


	/**
	 * Create the advisor (interceptor) chain. The advisors that
	 * are sourced from a BeanFactory will be refreshed each time
	 * a new prototype instance is added. Interceptors
	 * added programmatically through the factory API are
	 * unaffected by such changes.
	 */
	private void createAdvisorChain() throws AopConfigException, BeansException {
		if (this.interceptorNames == null || this.interceptorNames.length == 0) {
			throw new AopConfigException("Interceptor names are required");
		}
		// Globals can't be last
		if (this.interceptorNames[this.interceptorNames.length - 1].endsWith(GLOBAL_SUFFIX)) {
			throw new AopConfigException("Target required after globals");
		}

		// Materialize interceptor chain from bean names
		for (int i = 0; i < this.interceptorNames.length; i++) {
			String name = this.interceptorNames[i];
			logger.debug("Configuring interceptor '" + name + "'");

			if (name.endsWith(GLOBAL_SUFFIX)) {
				if (!(this.beanFactory instanceof ListableBeanFactory)) {
					throw new AopConfigException("Can only use global advisors or interceptors with a ListableBeanFactory");
				}
				else {
					addGlobalAdvisor((ListableBeanFactory) this.beanFactory,
					                                  name.substring(0, name.length() - GLOBAL_SUFFIX.length()));
				}
			}
			else {
				// Add a named interceptor
				Object advice = this.beanFactory.getBean(this.interceptorNames[i]);
				//logger.debug("Adding pointcut or interceptor [" + pointcutOrInterceptor + "] from bean name '" + this.interceptorNames[i] + "'");
				addAdvisor(advice, this.interceptorNames[i]);
			}
		}
	}

	/**
	 * Refresh named beans from the interceptor chain.
	 * We need to do this every time a new prototype instance is
	 * returned, to return distinct instances of prototype interfaces
	 * and pointcuts.
	 */
	private void refreshAdvisorChain() {
		Advisor[] advisors = getAdvisors();
		for (int i = 0; i < advisors.length; i++) {
			String beanName = (String) this.sourceMap.get(advisors[i]);
			if (beanName != null) {
				logger.info("Refreshing bean named '" + beanName + "'");

				Object bean = this.beanFactory.getBean(beanName);

				Advisor refreshedAdvisor = objectToAdvisor(bean);
				// What about aspect interfaces!? we're only updating
				replaceAdvice(advisors[i], refreshedAdvisor);
				// Keep name mapping up to date
				sourceMap.put(refreshedAdvisor, beanName);
			}
			else {
				// We can't throw an exception here, as the user may have added additional
				// pointcuts programmatically we don't know about
				logger.info("Cannot find bean name for Advisor [" + advisors[i] + 
					"] when refreshing advisor chain");
			}
		}
	}

	/**
	 * Add all global interceptors and pointcuts.
	 */
	private void addGlobalAdvisor(ListableBeanFactory beanFactory, String prefix) {
		String[] globalAdvisorNames = BeanFactoryUtils.beanNamesIncludingAncestors(beanFactory, Advisor.class);
		String[] globalInterceptorNames = BeanFactoryUtils.beanNamesIncludingAncestors(beanFactory, Interceptor.class);
		List beans = new ArrayList(globalAdvisorNames.length + globalInterceptorNames.length);
		Map names = new HashMap();
		for (int i = 0; i < globalAdvisorNames.length; i++) {
			String name = globalAdvisorNames[i];
			Object bean = beanFactory.getBean(name);
			beans.add(bean);
			names.put(bean, name);
		}
		for (int i = 0; i < globalInterceptorNames.length; i++) {
			String name = globalInterceptorNames[i];
			Object bean = beanFactory.getBean(name);
			beans.add(bean);
			names.put(bean, name);
		}
		Collections.sort(beans, new OrderComparator());
		for (Iterator it = beans.iterator(); it.hasNext();) {
			Object bean = it.next();
			String name = (String) names.get(bean);
			if (name.startsWith(prefix)) {
				addAdvisor(bean, name);
			}
		}
	}

	/**
	 * Add the given interceptor, pointcut or object to the interceptor list.
	 * Because of these three possibilities, we can't type the signature
	 * more strongly.
	 * @param next interceptor, pointcut or target object.
	 * @param name bean name from which we obtained this object in our owning
	 * bean factory.
	 */
	private void addAdvisor(Object next, String name) {
		logger.debug("Adding advisor or interceptor [" + next + "] with name [" + name + "]");
		// We need to add a method pointcut so that our source reference matches
		// what we find from superclass interceptors
		Advisor advisor = objectToAdvisor(next);
		addAdvisor(advisor);
		// Record the pointcut as descended from the given bean name.
		// This allows us to refresh the interceptor list, which we'll need to
		// do if we have to create a new prototype instance. Otherwise the new
		// prototype instance wouldn't be truly independent, because it might reference
		// the original instances of prototype interceptors.
		this.sourceMap.put(advisor, name);
	}

	private Advisor objectToAdvisor(Object next) {
		Advisor advisor;
		if (next instanceof Advisor) {
			advisor = (Advisor) next;
		}
		else if (next instanceof MethodInterceptor) {
			advisor = new DefaultInterceptionAroundAdvisor((MethodInterceptor) next);
		}
		else {
			// It's not a pointcut or interceptor.
			// It's a bean that needs an invoker around it.
			InvokerInterceptor ii = new InvokerInterceptor(next);
			advisor = new DefaultInterceptionAroundAdvisor(ii);
			//throw new AopConfigException("Illegal type: bean '" + name + "' must be of type MethodPointcut or Interceptor");
		}
		return advisor;
	}

	/**
	 * Return a proxy. Invoked when clients obtain beans
	 * from this factory bean.
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws BeansException {
		return createInstance();
	}

	/**
	 * Create an instance of the AOP proxy to be returned by this factory.
	 * The instance will be cached for a singleton, and create on each call to
	 * getObject() for a proxy.
	 * @return Object a fresh AOP proxy reflecting the current
	 * state of this factory
	 */
	private Object createInstance() {
		AopProxy proxy = null;
		if (this.singleton) {
			// This object can configure the proxy directly if it's
			// being used as a singleton
			proxy = createAopProxy();
		}
		else {
			refreshAdvisorChain();
			// In the case of a prototype, we need to give the proxy
			// an independent instance of the configuration
			if (logger.isDebugEnabled())
				logger.debug("Creating copy of prototype ProxyFactoryBean config: " + this);
			AdvisedSupport copy = new AdvisedSupport();
			copy.copyConfigurationFrom(this);
			proxy = copy.createAopProxy();
		}
		return proxy.getProxy();
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return (getTarget() != null) ? this.getTarget().getClass() : null;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return this.singleton;
	}

	/**
	 * Set the value of the singleton property. Governs whether this factory
	 * should always return the same proxy instance (which implies the same target)
	 * or whether it should return a new prototype instance, which implies that
	 * the target and interceptors may be new instances also, if they are obtained
	 * from prototype bean definitions.
	 * This allows for fine control of independence/uniqueness in the object graph.
	 * @param singleton
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

}
