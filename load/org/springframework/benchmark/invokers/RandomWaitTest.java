/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

import java.io.InputStream;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.load.AbortTestException;
import org.springframework.load.AbstractTest;
import org.springframework.load.TestFailedException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: RandomWaitTest.java,v 1.1 2003-12-02 22:31:06 johnsonr Exp $
 */
public class RandomWaitTest extends AbstractTest implements InitializingBean {
	
	// Must be shared by all threads
	protected static BeanFactory bf;
	
	static {
		InputStream is = RandomWaitTest.class.getResourceAsStream("beans.xml");
		bf = new XmlBeanFactory(is);
		Object a = bf.getBean("singleton");
		Object b = bf.getBean("singleton");
		if (a != b) throw new RuntimeException("Singletons not ==");
	}
	
	protected String bean;
	
	private int maxMillis = 0;
	
	private int notAdvised = 10;
	
	private Service service;
	
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
