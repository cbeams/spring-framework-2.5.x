/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * EnterpriseServices test that sources attributes from source-level metadata.
 * <b>Requires source-level metadata compilation.</b>
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: MetadataEnterpriseServicesTest.java,v 1.1 2003-12-12 21:31:25 johnsonr Exp $
 */
public class MetadataEnterpriseServicesTest extends AbstractMetadataAutoProxyTests {
	
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
