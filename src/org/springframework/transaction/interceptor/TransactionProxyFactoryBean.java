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

package org.springframework.transaction.interceptor;

import java.util.Properties;

import org.aopalliance.intercept.AspectException;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Proxy factory bean for simplified declarative transaction handling.
 * Alternative to the standard AOP ProxyFactoryBean with a TransactionInterceptor.
 *
 * <p>This class is intended to cover the <i>typical</i> case of declarative
 * transaction demarcation: namely, wrapping a (singleton) target object with a
 * transactional proxy, proxying all the interfaces that the target implements.
 *
 * <p>In contrast to TransactionInterceptor, the transaction attributes are
 * specified as properties, with method names as keys and transaction attribute
 * descriptors as values. Method names are always applied to the target class.
 *
 * <p>Internally, a TransactionInterceptor instance is used, but the user of this
 * class does not have to care. Optionally, a MethodPointcut can be specified
 * to cause conditional invocation of the underlying TransactionInterceptor.
 *
 * <p>The preInterceptors and postInterceptors properties can be set to add
 * additional interceptors to the mix, like PerformanceMonitorInterceptor or
 * HibernateInterceptor/JdoInterceptor.
 *
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @author Rod Johnson
 * @since 21.08.2003
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see TransactionInterceptor
 * @see #setTransactionAttributes
 * @version $Id: TransactionProxyFactoryBean.java,v 1.22 2004-03-18 02:46:05 trisberg Exp $
 */
public class TransactionProxyFactoryBean extends ProxyConfig implements FactoryBean, InitializingBean {

	private PlatformTransactionManager transactionManager;

	private Object target;

	private Class[] proxyInterfaces;

	private Properties transactionAttributes;

	private Pointcut pointcut;

	private Object[] preInterceptors;

	private Object[] postInterceptors;

	private Object proxy;

	/**
	 * Set the transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the target object, i.e. the bean to be wrapped with a transactional proxy.
	 * The target may be any object, in case an InvokerInterceptor will be created.
	 * If it is a MethodInterceptor, no wrapper interceptor is created. This enables
	 * the use of a pooling target or prototype interceptor etc.
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * Optional: You only need to set this property to filter the set of interfaces
	 * being proxied (default is to pick up all interfaces on the target),
	 * or if providing a custom invoker interceptor instead of a target.
	 * <p>If left null (the default), the AOP infrastructure works out which
	 * interfaces need proxying.
	 */
	public void setProxyInterfaces(String[] interfaceNames) throws AspectException, ClassNotFoundException {
		this.proxyInterfaces = AopUtils.toInterfaceArray(interfaceNames);
	}

	/**
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * @see TransactionAttributeEditor
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		this.transactionAttributes = transactionAttributes;
	}

	/**
	 * Set a pointcut, i.e a bean that can cause conditional invocation
	 * of the TransactionInterceptor depending on method and attributes passed.
	 * Note: Additional interceptors are always invoked.
	 * @see #setPreInterceptors
	 * @see #setPostInterceptors
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * Set additional interceptors (or advisors) to be applied before the
	 * implicit transaction interceptor, e.g. PerformanceMonitorInterceptor.
	 * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor
	 */
	public void setPreInterceptors(Object[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}

	/**
	 * Set additional interceptors (or advisors) to be applied after the
	 * implicit transaction interceptor, e.g. HibernateInterceptors for
	 * eagerly binding Sessions to the current thread when using JTA.
	 * <p>Note that this is just necessary if you rely on those interceptors in general:
	 * HibernateTemplate and JdoTemplate work nicely with JtaTransactionManager through
	 * implicit on-demand thread binding.
	 * @see org.springframework.orm.hibernate.HibernateInterceptor
	 * @see org.springframework.orm.jdo.JdoInterceptor
	 */
	public void setPostInterceptors(Object[] preInterceptors) {
		this.postInterceptors = preInterceptors;
	}


	public void afterPropertiesSet() throws AopConfigException {
		if (this.target == null) {
			throw new AopConfigException("Target must be set");
		}
		
		if (this.transactionAttributes == null) {
			throw new AopConfigException("'transactionAttributes' property must be set: if there are no transaction methods, don't use a transactional proxy");
		}

		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		tas.setProperties(this.transactionAttributes);

		TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
		transactionInterceptor.setTransactionManager(this.transactionManager);
		transactionInterceptor.setTransactionAttributeSource(tas);
		transactionInterceptor.afterPropertiesSet();

		ProxyFactory proxyFactory = new ProxyFactory();

		if (this.preInterceptors != null) {
			for (int i = 0; i < this.preInterceptors.length; i++) {
				proxyFactory.addAdvisor(GlobalAdvisorAdapterRegistry.getInstance().wrap(this.preInterceptors[i]));
			}
		}

		if (this.pointcut != null) {
			Advisor advice = new DefaultPointcutAdvisor(this.pointcut, transactionInterceptor);
			proxyFactory.addAdvisor(advice);
		}
		else {
			// rely on default pointcut
			proxyFactory.addAdvisor(new TransactionAttributeSourceTransactionAroundAdvisor(transactionInterceptor));
			// could just do the following, but it's usually less efficient because of AOP advice chain caching
			//proxyFactory.addInterceptor(transactionInterceptor);
		}

		if (this.postInterceptors != null) {
			for (int i = 0; i < this.postInterceptors.length; i++) {
				proxyFactory.addAdvisor(GlobalAdvisorAdapterRegistry.getInstance().wrap(this.postInterceptors[i]));
			}
		}

		proxyFactory.copyFrom(this);

		proxyFactory.setTargetSource(createTargetSource(this.target));
		if (this.proxyInterfaces != null) {
			proxyFactory.setInterfaces(this.proxyInterfaces);
		}
		else if (!getProxyTargetClass()) {
			// rely on AOP infrastructure to tell us what interfaces to proxy
			proxyFactory.setInterfaces(AopUtils.getAllInterfaces(this.target));
		}
		this.proxy = proxyFactory.getProxy();
	}

	/**
	 * Set the target or TargetSource.
	 * @param target target. If this is an implementation of TargetSource it is
	 * used as our TargetSource; otherwise it is wrapped in a SingletonTargetSource.
	 * @return a TargetSource for this object
	 */
	protected TargetSource createTargetSource(Object target) {
		if (target instanceof TargetSource) {
			return (TargetSource) target;
		}
		else {
			return new SingletonTargetSource(target);
		}
	}

	public Object getObject() {
		return this.proxy;
	}

	public Class getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		else if (this.target != null) {
			return this.target.getClass();
		}
		else {
			return null;
		}
	}

	public boolean isSingleton() {
		return true;
	}

}
