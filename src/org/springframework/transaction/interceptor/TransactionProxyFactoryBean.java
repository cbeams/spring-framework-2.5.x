package org.springframework.transaction.interceptor;

import java.util.Iterator;
import java.util.Properties;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.framework.AbstractMethodPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopUtils;
import org.springframework.aop.framework.InvokerInterceptor;
import org.springframework.aop.framework.ProxyFactory;
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
 * @since 21.08.2003
 * @version $Id: TransactionProxyFactoryBean.java,v 1.5 2003-10-06 13:52:30 jhoeller Exp $
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see TransactionInterceptor
 * @see #setTransactionAttributes
 */
public class TransactionProxyFactoryBean implements FactoryBean, InitializingBean {

	private PlatformTransactionManager transactionManager;

	private Object target;

	private Properties transactionAttributes;

	private boolean proxyInterfacesOnly = true;

	private AbstractMethodPointcut methodPointcut;

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
	 * transactional proxy.
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
	 * Set if the proxy should only implement the interfaces of the target.
	 * If this is false, a dynamic runtime subclass of the target will be
	 * created via CGLIB, castable to the target class. Default is true.
	 */
	public void setProxyInterfacesOnly(boolean proxyInterfacesOnly) {
		this.proxyInterfacesOnly = proxyInterfacesOnly;
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
	public void setMethodPointcut(AbstractMethodPointcut pointcut) {
		this.methodPointcut = pointcut;
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

	public void afterPropertiesSet() throws AopConfigException {
		if (this.target == null) {
			throw new AopConfigException("Target must be set");
		}

		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		TransactionAttributeEditor tae = new TransactionAttributeEditor();
		for (Iterator it = this.transactionAttributes.keySet().iterator(); it.hasNext();) {
			String methodName = (String)it.next();
			String value = this.transactionAttributes.getProperty(methodName);
			tae.setAsText(value);
			TransactionAttribute attr = (TransactionAttribute)tae.getValue();
			tas.addTransactionalMethod(methodName, attr);
		}

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

		if (this.methodPointcut != null) {
			this.methodPointcut.setInterceptor(transactionInterceptor);
			proxyFactory.addMethodPointcut(this.methodPointcut);
		}
		else {
			proxyFactory.addInterceptor(transactionInterceptor);
		}

		if (this.postInterceptors != null) {
			for (int i = 0; i < this.postInterceptors.length; i++) {
				proxyFactory.addInterceptor(this.postInterceptors[i]);
			}
		}

		proxyFactory.addInterceptor(new InvokerInterceptor(this.target));
		if (this.proxyInterfacesOnly) {
			proxyFactory.setInterfaces(AopUtils.getAllInterfaces(this.target));
		}
		this.proxy = proxyFactory.getProxy();
	}

	public Object getObject() {
		return this.proxy;
	}

	public boolean isSingleton() {
		return true;
	}

}
