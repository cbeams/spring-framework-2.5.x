/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.load.AbortTestException;
import org.springframework.load.AbstractTest;
import org.springframework.load.TestFailedException;

/**
 * 
 * @author Rod Johnson
 */
public class RandomWaitTest extends AbstractTest implements InitializingBean {
	
	// Must be shared by all threads
	protected static BeanFactory bf;
	
	static {
		bf = new XmlBeanFactory(new ClassPathResource("beans.xml", RandomWaitTest.class));
		Object a = bf.getBean("singleton");
		Object b = bf.getBean("singleton");
		if (a != b) throw new RuntimeException("Singletons not ==");
	}
	
	protected String bean;
	
	protected int maxMillis = 0;
	
	protected int notAdvised = 10;
	
	protected Service service;
	
	public void setBean(String name) {
		this.bean = name;
	}
	
	public void setMaxMillis(int stringComps) {
		this.maxMillis = stringComps;
	}
	
	public void setNotAdvisedCount(int count) {
		this.notAdvised = count;
	}

	/**
	 * @see org.springframework.load.AbstractTest#runPass(int)
	 */
	protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
		Service myService = getService();
		myService.takeUpToMillis(maxMillis);
		
		for (int j = 0; j < notAdvised; j++) {
			myService.notAdvised();
		}
	}

	protected Service getService() {
		return service;
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void afterPropertiesSet() throws Exception {
		service = (Service) bf.getBean(bean);
		System.out.println("Service bean class for group " + getGroup() + "=" + service.getClass());
	}
	
	public String getGroup() {
		return bean;
	}

}
