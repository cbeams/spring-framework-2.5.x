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

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.UnknownAdviceTypeException;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
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
 * @version $Id: ProxyFactoryBean.java,v 1.20 2004-02-11 17:18:16 jhoeller Exp $
 * @see #setInterceptorNames
 * @see #setProxyInterfaces
 * @see org.aopalliance.intercept.MethodInterceptor
 * @see org.springframework.aop.InterceptionAroundAdvisor
 * @see org.springframework.aop.InterceptionIntroductionAdvisor
 */
public class ProxyFactoryBean extends AdvisedSupport implements FactoryBean, BeanFactoryAware, AdvisedSupportListener {

	/**
	 * This suffix in a value in an interceptor list indicates to expand globals.
	 */
	public static final String GLOBAL_SUFFIX = "*";
	
	private boolean singleton = true;
	
	/** If this is a singleton, the cached instance */
	private Object singletonInstance;
	
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
	
	private String targetName = null;


	/**
	 * Set the names of the interfaces we're proxying. If no interface
	 * is given, a CGLIB for the actual class will be created.
	 */
	public void setProxyInterfaces(String[] interfaceNames) throws AspectException, ClassNotFoundException {
		Class[] interfaces = AopUtils.toInterfaceArray(interfaceNames);
		setInterfaces(interfaces);
	}

	/**
	 * Set the list of Interceptor/Advisor bean names. This must
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
		if (singleton) {
			// Eagerly initialize the shared singleton instance
			getSingletonInstance();
			// We must listen to superclass advice change events to recache singleton
			// instance if necessary
			addListener(this);
		}
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
			//throw new AopConfigException("Interceptor names are required");
			return;
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

				Object refreshedAdvisor = namedBeanToAdvisorOrTargetSource(bean);
				// Might have just refreshed target source
				if (refreshedAdvisor instanceof Advisor) {
					// What about aspect interfaces!? we're only updating
					replaceAdvisor(advisors[i], (Advisor) refreshedAdvisor);
				}
				else {
					setTargetSource((TargetSource) refreshedAdvisor);
				}
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
		logger.debug("Adding advisor or TargetSource [" + next + "] with name [" + name + "]");
		// We need to add a method pointcut so that our source reference matches
		// what we find from superclass interceptors
		Object advisor = namedBeanToAdvisorOrTargetSource(next);
		if (advisor instanceof Advisor) {
			// If it wasn't just updating the TargetSource
			logger.debug("Adding advisor with name [" + name + "]");
			addAdvisor((Advisor) advisor);
			//			Record the pointcut as descended from the given bean name.
			 // This allows us to refresh the interceptor list, which we'll need to
			 // do if we have to create a new prototype instance. Otherwise the new
			 // prototype instance wouldn't be truly independent, because it might reference
			 // the original instances of prototype interceptors.
			 this.sourceMap.put(advisor, name);
		}
		else {
			logger.debug("Adding TargetSource [" + advisor + "] with name [" + name + "]");
			setTargetSource((TargetSource) advisor);
			// Save target name
			this.targetName = name;
		}
	}
	
	private void refreshTarget() {
		logger.debug("Refreshing target with name '" + this.targetName + "'");
		if (this.targetName == null) {
			throw new AopConfigException("Target name cannot be null when refreshing!");
		}
		Object target = this.beanFactory.getBean(this.targetName);
		setTarget(target);
	}

	/**
	 * Return Advisor or TargetSource
	 */
	private Object namedBeanToAdvisorOrTargetSource(Object next) {
		try {
			Advisor adv = GlobalAdvisorAdapterRegistry.getInstance().wrap(next);
			return adv;
		}
		catch (UnknownAdviceTypeException ex) {
			// TODO consider checking that it's the last in the list?
			if (next instanceof TargetSource) {
				return (TargetSource) next;
			}
			else {
				// It's not a pointcut or interceptor.
				// It's a bean that needs an invoker around it.
				return new SingletonTargetSource(next);
				//throw new AopConfigException("Illegal type: bean '" + name + "' must be of type MethodPointcut or Interceptor");
			}
		}
	}

	/**
	 * Return a proxy. Invoked when clients obtain beans
	 * from this factory bean. Create an instance of the AOP proxy to be returned by this factory.
	 * The instance will be cached for a singleton, and create on each call to
	 * getObject() for a proxy.
	 * @return Object a fresh AOP proxy reflecting the current
	 * state of this factory
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws BeansException {
		return (this.singleton) ?
			getSingletonInstance() :
			newPrototypeInstance();
	}
	
	
	private Object getSingletonInstance() {
		if (singletonInstance == null) {
			// This object can configure the proxy directly if it's
			// being used as a singleton
			singletonInstance = createAopProxy().getProxy();
		}
		return singletonInstance;
	}
	
	private Object newPrototypeInstance() {
		refreshAdvisorChain();
		refreshTarget();
		// In the case of a prototype, we need to give the proxy
		// an independent instance of the configuration
		if (logger.isDebugEnabled())
			logger.debug("Creating copy of prototype ProxyFactoryBean config: " + this);
		AdvisedSupport copy = new AdvisedSupport();
		copy.copyConfigurationFrom(this);
		if (logger.isDebugEnabled())
			logger.debug("Copy has config: " + copy);
		return copy.createAopProxy().getProxy();
	}


	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return getTargetSource().getTargetClass();
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

	/**
	 * @see org.springframework.aop.framework.AdvisedSupportListener#activated(org.springframework.aop.framework.AdvisedSupport)
	 */
	public void activated(AdvisedSupport advisedSupport) {
	}

	/**
	 * Blow away and recache singleton to allow for advice changes.
	 * @see org.springframework.aop.framework.AdvisedSupportListener#adviceChanged(org.springframework.aop.framework.AdvisedSupport)
	 */
	public void adviceChanged(AdvisedSupport advisedSupport) {
		logger.info("Advice has changed; recaching singleton instance");
		this.singletonInstance = null;
		getSingletonInstance();
	}

}
