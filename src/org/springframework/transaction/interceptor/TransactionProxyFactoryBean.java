package org.springframework.transaction.interceptor;

import java.util.Properties;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.support.DefaultInterceptionAroundAdvisor;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Proxy factory bean for simplified declarative transaction handling.
 * Alternative to the standard AOP ProxyFactoryBean with a TransactionInterceptor.
 *
 * <p>This class is intended to cover the <i>typical</i> case of declarative
 * transaction demarcation: wrapping a target object with a transactional proxy,
 * proxying all the interfaces that the target implements.
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
 * @version $Id: TransactionProxyFactoryBean.java,v 1.17 2003-12-11 11:36:46 johnsonr Exp $
 */
public class TransactionProxyFactoryBean extends ProxyConfig implements FactoryBean, InitializingBean {

	private PlatformTransactionManager transactionManager;

	private Object target;

	private Properties transactionAttributes;

	/** 
	 * Interfaces to proxy. If left null (the default)
	 * the AOP infrastructure works out which interfaces need proxying
	 */
	private Class[] interfaces;

	private Pointcut pointcut;

	private Interceptor[] preInterceptors;

	private Interceptor[] postInterceptors;

	private Object proxy;

	/**
	 * Set the transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the target object, i.e. the bean to be wrapped with a
	 * transactional proxy. The target may be any object, in case an
	 * InvokerInterceptor will be created. If it is a MethodInterceptor no
	 * wrapper interceptor is created. This enables the use of a pooling target
	 * or prototype interceptor etc.
	 */
	public void setTarget(Object target) {
		this.target = target;
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
	 * Set a MethodPointcut, i.e a bean that can cause conditional invocation
	 * of the TransactionInterceptor depending on method and attributes passed.
	 * Note: Additional interceptors are always invoked.
	 * <p>Needs to be a subclass of AbstractMethodPointcut, to be able to set
	 * the internally used TransactionInterceptor to it.
	 * @see #setPreInterceptors
	 * @see #setPostInterceptors
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * Set additional interceptors to be applied before the implicit transaction
	 * interceptor, e.g. PerformanceMonitorInterceptor.
	 * @see org.springframework.aop.interceptor.PerformanceMonitorInterceptor
	 */
	public void setPreInterceptors(Interceptor[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}

	/**
	 * Set additional interceptors to be applied aftr the implicit transaction
	 * interceptor, e.g. HibernateInterceptors or JdoInterceptors for binding
	 * Sessions respectively PersistenceManagers to the current thread when
	 * using JtaTransactionManager. Note that this is just necessary if you
	 * rely on those interceptors in general: HibernateTemplate and JdoTemplate
	 * work nicely with JtaTransactionManager through implicit thread binding.
	 * @see org.springframework.orm.hibernate.HibernateInterceptor
	 * @see org.springframework.orm.jdo.JdoInterceptor
	 */
	public void setPostInterceptors(Interceptor[] preInterceptors) {
		this.postInterceptors = preInterceptors;
	}
	
	
	/**
	 * Optional: you only need to set this property to filter the set of interfaces
	 * being proxied (default is to pick up all interfaces on the target),
	 * or if providing a custom invoker interceptor instead of a target.
	 */
	public void setProxyInterfaces(String[] interfaceNames) throws AspectException, ClassNotFoundException {
		this.interfaces = AopUtils.toInterfaceArray(interfaceNames);
	}
	

	public void afterPropertiesSet() throws AopConfigException {
		if (this.target == null) {
			throw new AopConfigException("Target must be set");
		}
		
		if (this.transactionAttributes == null) {
			throw new AopConfigException("'transactionAttributes' property must be set: if there are no transaction methods, don't use a transactional proxy");
		}

		PropertiesTransactionAttributeSource tas = new PropertiesTransactionAttributeSource();
		tas.setProperties(this.transactionAttributes);

		TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
		transactionInterceptor.setTransactionManager(this.transactionManager);
		transactionInterceptor.setTransactionAttributeSource(tas);
		transactionInterceptor.afterPropertiesSet();

		ProxyFactory proxyFactory = new ProxyFactory();

		if (this.preInterceptors != null) {
			for (int i = 0; i < this.preInterceptors.length; i++) {
				proxyFactory.addInterceptor(this.preInterceptors[i]);
			}
		}

		if (this.pointcut != null) {
			Advisor advice = new DefaultInterceptionAroundAdvisor(this.pointcut, transactionInterceptor);
			proxyFactory.addAdvisor(advice);
		}
		else {
			// Rely on default pointcut
			proxyFactory.addAdvisor(new TransactionAttributeSourceTransactionAroundAdvisor(transactionInterceptor));
			
			// Could just do the following, but it's usually less efficient because of AOP advice chain caching
			//proxyFactory.addInterceptor(transactionInterceptor);
		}

		if (this.postInterceptors != null) {
			for (int i = 0; i < this.postInterceptors.length; i++) {
				proxyFactory.addInterceptor(this.postInterceptors[i]);
			}
		}

		proxyFactory.copyFrom(this);

		proxyFactory.setTargetSource(createTargetSource(this.target));
		if (this.interfaces != null) {
			proxyFactory.setInterfaces(this.interfaces);
		}
		else if (!getProxyTargetClass()) {
			// Rely on AOP infrastucture to tell us what interfaces to proxy
			proxyFactory.setInterfaces(AopUtils.getAllInterfaces(this.target));
		}
		this.proxy = proxyFactory.getProxy();
	}

	
	/**
	 * Set the target or TargetSource
	 * @param pTarget target. If this is an implementation of TargetSource it
     * is used as our TargetSource; otherwise it is wrapped
     * in a SingletonTargetSource.
	 * @return a TargetSource for this object
	 */
	protected TargetSource createTargetSource(Object pTarget) {
		if (pTarget instanceof TargetSource) {
			return (TargetSource) pTarget;
		}
		else {
			return new SingletonTargetSource(pTarget);
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
