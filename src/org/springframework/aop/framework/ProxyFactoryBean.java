/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.UnknownAdviceTypeException;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.OrderComparator;

/**
 * FactoryBean implementation for use to source AOP proxies from a Spring BeanFactory.
 *
 * <p>Interceptors and Advisors are identified by a list of bean names in the current
 * bean factory. These beans should be of type Interceptor or Advisor. The last entry
 * in the list can be the name of any bean in the factory. If it's neither an
 * Interceptor nor an Advisor, a new SingletonTargetSource is added to wrap it. If it;s
 * a TargetSource, it is used as this proxy factory's TargetSource. It's normally preferred
 * to use the "targetSource" property to set the TargetSource. It is not possible to use
 * both the targetSource property and an interceptor name: this is treated as a
 * configuration error.
 *
 * <p>Global interceptors and advisors can be added at the factory level. The specified
 * ones are expanded in an interceptor list where an "xxx*" entry is included in the
 * list, matching the given prefix with the bean names (e.g. "global*" would match
 * both "globalBean1" and "globalBean2", "*" all defined interceptors). The matching
 * interceptors get applied according to their returned order value, if they
 * implement the Ordered interface. An interceptor name list may not conclude
 * with a global "xxx*" pattern, as global interceptors cannot invoke targets.
 *
 * <p>Creates a J2SE proxy when proxy interfaces are given, a CGLIB proxy for the
 * actual target class if not. Note that the latter will only work if the target class
 * does not have final methods, as a dynamic subclass will be created at runtime.
 *
 * <p>It's possible to cast a proxy obtained from this factory to <code>Advised</code>, or to
 * obtain the ProxyFactoryBean reference and programmatically manipulate it.
 * This won't work for existing prototype references, which are independent. However,
 * it will work for prototypes subsequently obtained from the factory. Changes to
 * interception will work immediately on singletons (including existing references).
 * However, to change interfaces or target it's necessary to obtain a new instance
 * from the factory. This means that singleton instances obtained from the factory
 * do not have the same object identity. However, they do have the same interceptors
 * and target, and changing any reference will change all objects.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setInterceptorNames
 * @see #setProxyInterfaces
 * @see org.aopalliance.intercept.MethodInterceptor
 * @see org.springframework.aop.framework.Advised
 * @see org.springframework.aop.target.SingletonTargetSource
 */
public class ProxyFactoryBean extends AdvisedSupport
    implements FactoryBean, BeanFactoryAware, AdvisedSupportListener {
	
	/*
	 * Implementation notes. There are two cases of usage of this class:
	 * usage as a singleton, when only one object will be created, and usage
	 * as a prototype, when the FactoryBean.getObject() method must return an 
	 * independent proxy on each invocation. In the latter case, a distinct instance of
	 * any non-singleton Advisors or Advices must be used, as well as a distinct
	 * target/TargetSource instance if the target is a prototype and is specified in the
	 * interceptorNames list, rather than using the target or targetSource property. 
	 * 
	 * If this factory is used as a singleton, the advice chain in this class is used
	 * and all Advisors/Advices are materialized when the singleton instance is created.
	 * If it's a prototype, new AdvisedSupport instances are created with
	 * a copy of the advice chain to create each proxy and support independent
	 * manipulation of advice. Any advisor/advice bean names that that are prototypes
	 * are replaced by placeholders in the advisor chain held in this class and
	 * an independent advisor chain is materialized when each prototype instance
	 * is created.
	 * 
	 * Revision as of September 20, 2004 partly based on patch provided by Chris Eldredge.
	 */

	/**
	 * This suffix in a value in an interceptor list indicates to expand globals.
	 */
	public static final String GLOBAL_SUFFIX = "*";

	/**
	 * Names of Advisor and Advice beans in the factory.
	 * Default is for globals expansion only.
	 */
	private String[] interceptorNames;
	
	private boolean singleton = true;

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/**
	 * Owning bean factory, which cannot be changed after this
	 * object is initialized.
	 */
	private BeanFactory beanFactory;

	/**
	 * Name of the target or TargetSource bean. Null if the TargetSource is not specified in
	 * the interceptorNames list.
	 */
	private String targetName;


	/** If this is a singleton, the cached singleton proxy instance */
	private Object singletonInstance;


	/**
	 * Set the names of the interfaces we're proxying. If no interface
	 * is given, a CGLIB for the actual class will be created.
	 */
	public void setProxyInterfaces(String[] interfaceNames) throws ClassNotFoundException {
		Class[] interfaces = AopUtils.toInterfaceArray(interfaceNames);
		setInterfaces(interfaces);
	}

	/**
	 * Set the list of Advice/Advisor bean names. This must always be set
	 * to use this factory bean in a bean factory.
	 * <p>The referenced beans should be of type Interceptor, Advisor or Advice
	 * The last entry in the list can be the name of any bean in the factory.
	 * If it's neither an Advice nor an Advisor, a new SingletonTargetSource
	 * is added to wrap it. Such a target bean cannot be used if the target or targetSource
	 * property is set, in which case the interceptorNames array must contain
	 * only Advice/Advisor bean names.
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see org.springframework.aop.Advisor
	 * @see org.aopalliance.aop.Advice
	 * @see org.springframework.aop.target.SingletonTargetSource
	 */
	public void setInterceptorNames(String[] interceptorNames) {
		this.interceptorNames = interceptorNames;
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
	 * Specify the AdvisorAdapterRegistry to use.
	 * Default is the global AdvisorAdapterRegistry.
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		createAdvisorChain();
		if (this.singleton) {
			this.targetSource = freshTargetSource();
			// eagerly initialize the shared singleton instance
			getSingletonInstance();
			// We must listen to superclass advice change events to recache the singleton
			// instance if necessary.
			addListener(this);
		}
	}


	/**
	 * Return a proxy. Invoked when clients obtain beans
	 * from this factory bean. Create an instance of the AOP proxy to be returned by this factory.
	 * The instance will be cached for a singleton, and create on each call to
	 * getObject() for a proxy.
	 * @return Object a fresh AOP proxy reflecting the current
	 * state of this factory
	 */
	public Object getObject() throws BeansException {
		return (this.singleton) ? getSingletonInstance() : newPrototypeInstance();
	}

	public Class getObjectType() {
		return (this.singleton ? getSingletonInstance().getClass() : getTargetSource().getTargetClass());
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return this.singleton;
	}


	private Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			this.singletonInstance = this.createAopProxy().getProxy();
		}
		return this.singletonInstance;
	}

	/**
	 * Create a new prototype instance of this class's created proxy object,
	 * backed by an independent advisedSupport configuration
	 * @return a totally independent proxy, whose advice we may manipulate in isolation
	 */
	private synchronized Object newPrototypeInstance() {
		// In the case of a prototype, we need to give the proxy
		// an independent instance of the configuration.
		// In this case, no poxy will have an instance of this object's configuration,
		// but will have an independent copy.
		if (logger.isDebugEnabled()) {
			logger.debug("Creating copy of prototype ProxyFactoryBean config: " + this);
		}
		AdvisedSupport copy = new AdvisedSupport();
		// The copy needs a fresh advisor chain, and a fresh TargetSource.
		copy.copyConfigurationFrom(this, freshTargetSource(), freshAdvisorChain());
		if (logger.isDebugEnabled()) {
			logger.debug("Copy has config: " + copy);
		}
		return copy.createAopProxy().getProxy();
	}

	/**
	 * Create the advisor (interceptor) chain. Aadvisors that are sourced
	 * from a BeanFactory will be refreshed each time a new prototype instance
	 * is added. Interceptors added programmatically through the factory API
	 * are unaffected by such changes.
	 */
	private void createAdvisorChain() throws AopConfigException, BeansException {
		if (this.interceptorNames == null || this.interceptorNames.length == 0) {
			return;
		}
		
		// Globals can't be last unless we specified a targetSource using the property... 
		if (this.interceptorNames[this.interceptorNames.length - 1].endsWith(GLOBAL_SUFFIX) &&
				this.targetSource == EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("Target required after globals");
		}

		// materialize interceptor chain from bean names
		for (int i = 0; i < this.interceptorNames.length; i++) {
			String name = this.interceptorNames[i];
			if (logger.isDebugEnabled()) {
				logger.debug("Configuring advisor or advice '" + name + "'");
			}

			if (name.endsWith(GLOBAL_SUFFIX)) {
				if (!(this.beanFactory instanceof ListableBeanFactory)) {
					throw new AopConfigException(
					    "Can only use global advisors or interceptors with a ListableBeanFactory");
				}
				addGlobalAdvisor((ListableBeanFactory) this.beanFactory,
				    name.substring(0, name.length() - GLOBAL_SUFFIX.length()));
				continue;
			}
			else if (i == this.interceptorNames.length - 1 && this.targetSource == EMPTY_TARGET_SOURCE) {
				// The last name in the chain may be an Advisor/Advice or a target/TargetSource
				// Unfortunately we don't know. We must
				// Look at type of the bean
				if (!isNamedBeanAnAdvisorOrAdvice(interceptorNames[i])) {
						// Must be an interceptor
						this.targetName = this.interceptorNames[i];
						logger.info("Bean with name '" + interceptorNames[i] + "' concluding interceptor chain is not an advisor class: " +
								"treating it as a target or TargetSource");
						continue;
				}
				// If it IS an advice, or we can't tell, fall through and treat it as an advice...
			}
	
			// if we get here, we need to add a named interceptor
			// we must check if it's a singleton or prototype
			Object advice = null;
			if (isSingleton() || this.beanFactory.isSingleton(this.interceptorNames[i])) {
				// Add the real Advisor/Advice to the chain
				advice = this.beanFactory.getBean(this.interceptorNames[i]);
			}
			else {
				// It's a prototype Advice or Advisor: replace with a prototype
				//avoid unnecessary creation of prototype bean just for advisor chain initialization
				advice = new PrototypePlaceholderAdvisor(interceptorNames[i]);
			}
			addAdvisorOnChainCreation(advice, this.interceptorNames[i]);
		}
	}
	
	/**
	 * Look at bean factory metadata to work out whether
	 * this bean name, which concludes the interceptorNames list,
	 * is an Advisor or Advice, or may be a target
	 * @param beanName bean name to check
	 * @return true if it's an Advisor or Advice
	 */
	private boolean isNamedBeanAnAdvisorOrAdvice(String beanName) {
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry bdr = (BeanDefinitionRegistry) this.beanFactory;
			Class namedBeanClass = bdr.getBeanDefinition(beanName).getBeanClass();
			if (FactoryBean.class.isAssignableFrom(namedBeanClass)) {
				// if it's a FactoryBean, we want to look at what it creates, not the factory class
				namedBeanClass = beanFactory.getBean(beanName).getClass();
			}
			return Advisor.class.isAssignableFrom(namedBeanClass) || Advice.class.isAssignableFrom(namedBeanClass);
		}
		// Treat it as an Advisor if we can't tell
		return true;
	}


	/**
	 * Return an independent advisor chain.
	 * We need to do this every time a new prototype instance is returned,
	 * to return distinct instances of prototype Advisors and Advices.
	 */
	private List freshAdvisorChain() {		
		Advisor[] advisors = getAdvisors();
		List freshAdvisors = new ArrayList(advisors.length);

		for (int i = 0; i < advisors.length; i++) {
			if (advisors[i] instanceof PrototypePlaceholderAdvisor) {
				PrototypePlaceholderAdvisor pa = (PrototypePlaceholderAdvisor) advisors[i];
				if (logger.isDebugEnabled()) {
					logger.debug("Refreshing bean named '" + pa.getBeanName() + "'");
				}
				// Replace the placeholder with a fresh protoype instance resulting
				// from a getBean() lookup
				Object bean = this.beanFactory.getBean(pa.getBeanName());
				Advisor refreshedAdvisor = namedBeanToAdvisor(bean);
				freshAdvisors.add(refreshedAdvisor);
			}
			else {
				// Add the shared instance
				freshAdvisors.add(advisors[i]);
			}
		}
		return freshAdvisors;
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
				addAdvisorOnChainCreation(bean, name);
			}
		}
	}

	/**
	 * Invoked when advice chain is created.
	 * <p>Add the given advice, advisor or object to the interceptor list.
	 * Because of these three possibilities, we can't type the signature
	 * more strongly.
	 * @param next advice, advisor or target object
	 * @param name bean name from which we obtained this object in our owning
	 * bean factory
	 */
	private void addAdvisorOnChainCreation(Object next, String name) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding advisor or TargetSource [" + next + "] with name [" + name + "]");
		}
		
		// We need to convert to an Advisor if necessary so that our source reference matches
		// what we find from superclass interceptors.
		Advisor advisor = namedBeanToAdvisor(next);
		
			// if it wasn't just updating the TargetSource
		if (logger.isDebugEnabled()) {
			logger.debug("Adding advisor with name [" + name + "]");
		}			
		addAdvisor((Advisor) advisor);
	}
	
	/**
	 * @return a TargetSource to use when creating a proxy. If the target was not
	 * specified at the end of the interceptorNames list, the TargetSource will be this
	 * class's TargetSource member. Otherwise, we get the target bean and wrap it
	 * in a TargetSource if necessary.
	 */
	private TargetSource freshTargetSource() {
		if (this.targetName == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Not refreshing target: bean name not specified in interceptorNames");
			}
			return this.targetSource;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Refreshing target with name '" + this.targetName + "'");
			}
			Object target = this.beanFactory.getBean(this.targetName);
			return (target instanceof TargetSource) ? (TargetSource) target : new SingletonTargetSource(target);
		}
	}

	/**
	 * Convert the following object sourced from calling getBean() on a name in the interceptorNames array to an
	 * Advisor or TargetSource.
	 */
	private Advisor namedBeanToAdvisor(Object next) {
		try {
			return this.advisorAdapterRegistry.wrap(next);
		}
		catch (UnknownAdviceTypeException ex) {
			// We expected this to be an Advisor or Advice,
			// but it wasn't. This is a configuration error.
			throw new AopConfigException("Unknown advisor type " + next.getClass() +
					"; Can only include Advisor or Advice type beans in interceptorNames chain expect for last entry," +
					"which may be target or TargetSource", ex);
		}
	}


	/**
	 * @see org.springframework.aop.framework.AdvisedSupportListener#activated(org.springframework.aop.framework.AdvisedSupport)
	 */
	public void activated(AdvisedSupport advisedSupport) {
		// Nothing to do
	}

	/**
	 * Blow away and recache singleton on an advice change.
	 */
	public void adviceChanged(AdvisedSupport advisedSupport) {
		if (singleton) {
			logger.info("Advice has changed; recaching singleton instance");
			this.singletonInstance = null;
			getSingletonInstance();
		}
	}
	
	
	/**
	 * Used in the interceptor chain where we need to replace a bean with a prototype
	 * on creating a proxy
	 */
	public static class PrototypePlaceholderAdvisor implements Advisor {
		private final String beanName;
		private final String mesg;
		
		public PrototypePlaceholderAdvisor(String beanName) {
			this.beanName = beanName;
			this.mesg = "Placeholder for prototype Advisor/Advice with bean name ='" + beanName + "'";
		}
		
		public String getBeanName() {
			return beanName;
		}
		
		public Advice getAdvice() {
			throw new UnsupportedOperationException("Cannot invoke methods: " + mesg);
		}
		
		public boolean isPerInstance() {
			throw new UnsupportedOperationException("Cannot invoke methods: " + mesg);
		}
		
		public String toString() {
			return mesg;
		}
	}	// class PrototypePlaceholderAdvisor
	

}
