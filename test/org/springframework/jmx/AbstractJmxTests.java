/*
 * Created on Jul 5, 2004
 */

package org.springframework.jmx;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rob Harrop
 */
public abstract class AbstractJmxTests extends AbstractMBeanServerTests {

	private ClassPathXmlApplicationContext ctx;


	protected void onSetUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext(getApplicationContextPath());
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/applicationContext.xml";
	}

	protected ApplicationContext getContext() {
		return this.ctx;
	}
}
