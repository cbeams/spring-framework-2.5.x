/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.enterpriseservices;

import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.framework.support.AdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.AbstractPoolingInvokerInterceptor;
import org.springframework.aop.interceptor.CommonsPoolingInvokerInterceptor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.metadata.Attributes;
import org.springframework.metadata.bcel.BcelAttributes;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * BeanPostProcessor that can be added to an ApplicationContext to configure declarative
 * enterprise services on all beans in the current context that define metadata attributes.
 * Similar to the .NET declarative service provision model. The AttributeRegistry is set to
 * use Spring Metadata.
 * Has knowledge of pooling and other specific enterprise aspects.
 * @author Rod Johnson
 * @version $Id: EnterpriseServices.java,v 1.1 2003-11-22 09:05:39 johnsonr Exp $
 */
public class EnterpriseServices extends AdvisorAutoProxyCreator  {


	// TODO will there be a standard default way of getting Attributes interface?
	private Attributes attributes = new BcelAttributes();
	
	public EnterpriseServices() {
	}


	/**
	 * Set the Spring metadata attributes used by this object.
	 * Default is to use source-level attributes.
	 * @param attributes Spring metadata attributes
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the Spring metadata attributes used by this object
	 */
	protected Attributes getAttributes() {
		return this.attributes;
	}

	
	/**
	 * DO final pooling style stuff
	 * TODO what if not otherwise proxied?
	 * TODO everything else could be pulled up into a superclass
	 * @see org.springframework.aop.framework.support.AbstractAutoProxyCreator#createInvokerInterceptor(java.lang.Object, java.lang.String)
	 */
	protected Interceptor createInvokerInterceptor(Object bean, String beanName, RootBeanDefinition definition) {
		
		List l = this.attributes.getAttributes(bean.getClass(), PoolingAttribute.class);
		if (l.size() == 1) {
			if (getBeanFactory().isSingleton(beanName))
				throw new BeanDefinitionStoreException("Cannot pool singleton bean '" + beanName + "'", null);
			logger.info("Configuring pooling...");
			
			PoolingAttribute pa = (PoolingAttribute) l.get(0);
			AbstractPoolingInvokerInterceptor cpii = createPoolingInvoker();
			cpii.setPoolSize(pa.getSize());
			cpii.setTargetBeanName(beanName);
			try {
				// Infinite cycle: tries to create the bean if we don't use a different factory
				ListableBeanFactoryImpl bf2 = new ListableBeanFactoryImpl();
				bf2.registerBeanDefinition(beanName, definition);
				cpii.setBeanFactory(bf2);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}
			return cpii;
		}
		else {
			return super.createInvokerInterceptor(bean, beanName);
		}
	}

	// TODO could pull into a strategy? or differentiate by subclasses
	protected AbstractPoolingInvokerInterceptor createPoolingInvoker() {
		CommonsPoolingInvokerInterceptor cpii = new CommonsPoolingInvokerInterceptor();
		return cpii;
	}
	
	protected boolean hasCustomInvoker(Object bean, String beanName) {
		List l = this.attributes.getAttributes(bean.getClass(), PoolingAttribute.class);
		return !l.isEmpty();
	}
	
	/**
	 * @see org.springframework.aop.framework.support.AbstractAutoProxyCreator#shouldSkip(java.lang.Object, java.lang.String)
	 */
	protected boolean shouldSkip(Object bean, String name) {
		// Can avoid subtle bugs with the TransactionAdvice not getting attributes set
		return bean instanceof PlatformTransactionManager;
	}


}
