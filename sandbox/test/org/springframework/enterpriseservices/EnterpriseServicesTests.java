/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.enterpriseservices;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * EnterpriseServices test that ources attributes from source-level metadata.
 * @author Rod Johnson
 * @version $Id: EnterpriseServicesTests.java,v 1.1 2003-11-22 09:05:41 johnsonr Exp $
 */
public class EnterpriseServicesTests extends AbstractEnterpriseServicesTests {

	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public EnterpriseServicesTests(String arg0) {
		super(arg0);
	}
	
	protected BeanFactory getBeanFactory() throws IOException {
		// Load from classpath, NOT a file path
		BeanFactory bf = new ClassPathXmlApplicationContext(new String[] {
					"/org/springframework/enterpriseservices/dummyAttributes.xml",
					"/org/springframework/enterpriseservices/enterpriseServices.xml"});
		return bf;
	}
	
	
}
