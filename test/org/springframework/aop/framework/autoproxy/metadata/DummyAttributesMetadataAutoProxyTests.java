/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Metadata auto proxy test that sources attributes from source-level metadata.
 * @author Rod Johnson
 * @version $Id: DummyAttributesMetadataAutoProxyTests.java,v 1.1 2003-12-12 21:31:25 johnsonr Exp $
 */
public class DummyAttributesMetadataAutoProxyTests extends AbstractMetadataAutoProxyTests {
	
	public DummyAttributesMetadataAutoProxyTests(String arg0) {
		super(arg0);
	}
	
	protected BeanFactory getBeanFactory() throws IOException {
		// Load from classpath
		BeanFactory bf = new ClassPathXmlApplicationContext(new String[] {
					"/org/springframework/aop/framework/autoproxy/metadata/dummyAttributes.xml",
					"/org/springframework/aop/framework/autoproxy/metadata/enterpriseServices.xml"});
		return bf;
	}
	
	
}
