/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.support;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * EnterpriseServices test that ources attributes from source-level metadata.
 * @author Rod Johnson
 * @version $Id: AdvisorAutoProxyCreatorTests.java,v 1.1 2003-12-09 12:58:31 johnsonr Exp $
 */
public class AdvisorAutoProxyCreatorTests extends AbstractAdvisorAutoProxyCreatorTests {

	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public AdvisorAutoProxyCreatorTests(String arg0) {
		super(arg0);
	}
	
	protected BeanFactory getBeanFactory() throws IOException {
		return new ClassPathXmlApplicationContext("/org/springframework/aop/framework/support/advisorAutoProxyCreator.xml");
	}
	
}
