/*
 * Created on Jul 5, 2004
 */

package org.springframework.jmx;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base JMX test class that pre-loads an ApplicationContext from a user-configurable file. Override the
 * {@link #getApplicationContextPath()} method to control the configuration file location.
 *
 * @author Rob Harrop
 */
public abstract class AbstractJmxTests extends AbstractMBeanServerTests {

	private ClassPathXmlApplicationContext ctx;


	protected final void onSetUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext(getApplicationContextPath());
	}

	protected final void onTearDown() throws Exception {
		if (ctx != null) {
			ctx.close();
		}
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/applicationContext.xml";
	}

	protected ApplicationContext getContext() {
		return this.ctx;
	}
}
