/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.enterpriseservices;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * EnterpriseServices test that sources attributes from source-level metadata.
 * <b>Requires source-level metadata compilation.</b>
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: MetadataEnterpriseServicesTest.java,v 1.1 2003-11-22 09:05:40 johnsonr Exp $
 */
public class MetadataEnterpriseServicesTest extends AbstractEnterpriseServicesTests {
	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public MetadataEnterpriseServicesTest(String arg0) {
		super(arg0);
	}
	
	
	protected BeanFactory getBeanFactory() throws IOException {
		// Load from classpath, NOT a file path
		BeanFactory bf = new ClassPathXmlApplicationContext(new String[] {
					"/org/springframework/enterpriseservices/sourceAttributes.xml",
					"/org/springframework/enterpriseservices/enterpriseServices.xml"});
		return bf;
	}
	
	
}
