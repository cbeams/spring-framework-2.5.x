/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.AbstractFactoryBean;

/**
 * Simple factory to allow testing of FactoryBean 
 * support in AbstractBeanFactory. Depending on whether its
 * singleton property is set, it will return a singleton
 * or a prototype instance.
 * Implements InitializingBean interface, so we can check that
 * factories get this lifecycle callback if they want.
 * @author Rod Johnson
 * @since 10-Mar-2003
 * version $Id: DummyFactory.java,v 1.5 2003-11-05 11:28:15 jhoeller Exp $
 */
public class DummyFactory extends AbstractFactoryBean implements InitializingBean, PropertyValuesProviderFactoryBean {
	
	public static final String SINGLETON_NAME = "Factory singleton";
	
	private boolean isInitialized;

	private static boolean prototypeCreated;

	private TestBean testBean;

	private TestBean otherTestBean;

	public DummyFactory() {
		this.testBean = new TestBean();
		this.testBean.setName(SINGLETON_NAME);
		this.testBean.setAge(25);
	}

	public void setOtherTestBean(TestBean otherTestBean) {
		this.otherTestBean = otherTestBean;
	}

	public TestBean getOtherTestBean() {
		return otherTestBean;
	}

	public void afterPropertiesSet() {
		if (isInitialized)
			throw new RuntimeException("Cannot call afterPropertiesSet twice on the one bean");
		this.isInitialized = true;
	}
	
	/**
	 * Was this initialized by invocation of the
	 * afterPropertiesSet() method from the InitializingBean interface?
	 */
	public boolean wasInitialized() {
		return this.isInitialized;
	}

	public static boolean wasPrototypeCreated() {
		return prototypeCreated;
	}

	/**
	 * Return the managed object, supporting both singleton
	 * and prototype mode.
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws BeansException {
		if (isSingleton()) {
			//System.out.println("DummyFactory returned new SINGLETON");
			return this.testBean;
		}
		else {
			//System.out.println("DummyFactory created new PROTOTYPE");
			TestBean prototype = new TestBean("prototype created at " + System.currentTimeMillis(), 11);
			//System.out.println("prot name is " + prototype.getName());
			prototypeCreated = true;
			return prototype;
		}
	}

	public Class getObjectType() {
		return TestBean.class;
	}

}
