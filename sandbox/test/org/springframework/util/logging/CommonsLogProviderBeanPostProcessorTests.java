/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import junit.framework.TestCase;

/**
 * @author robh
 * 
 */
public class CommonsLogProviderBeanPostProcessorTests extends TestCase {

	public void testPostProcess() {
		CommonsLogProviderBeanPostProcessor bpp = new CommonsLogProviderBeanPostProcessor();
		bpp.setCommonsLogProvider(new ClassNameCommonsLogProvider());

		LogTestBean bean = new LogTestBean();
		bpp.postProcessBeforeInitialization(bean, "testBean");

		assertNotNull("getLog() shold not return null.", bean.getLog());
	}
}
