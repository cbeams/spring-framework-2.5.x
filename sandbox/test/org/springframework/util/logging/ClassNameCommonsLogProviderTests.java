/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import org.springframework.beans.TestBean;

/**
 * @author robh
 *
 */
public class ClassNameCommonsLogProviderTests extends
		AbstractCommonsLogProviderTests {

	protected CommonsLogProvider getLogProvider() {
		return new ClassNameCommonsLogProvider();
	}

	protected Object getBean() {
		return new TestBean();
	}

	protected String getBeanName() {
		return "testBean";
	}
	
	protected String getLogName() {
		return "org.springframework.beans.TestBean";
	}

}
