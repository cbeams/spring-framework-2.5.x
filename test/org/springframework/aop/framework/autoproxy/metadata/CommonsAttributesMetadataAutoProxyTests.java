/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.framework.autoproxy.metadata;

import java.io.IOException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.metadata.commons.CommonsAttributeCompilerUtils;

/**
 * Metadata auto proxy creator test that sources attributes 
 * using Jakarta Commons Attributes.
 * <b>Requires source-level metadata compilation.</b>
 * <br>See the commonsBuild.xml Ant build script.
 * @author Rod Johnson
 * @since 13-Mar-2003
 */
public class CommonsAttributesMetadataAutoProxyTests extends AbstractMetadataAutoProxyTests {
	
	static {
		// If we're within an IDE, compile the attributes programmatically
		CommonsAttributeCompilerUtils.compileAttributesIfNecessary("**/autoproxy/metadata/*.java");
	}
	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public CommonsAttributesMetadataAutoProxyTests(String arg0) {
		super(arg0);
	}
	

	protected BeanFactory getBeanFactory() throws IOException {
		// Load from classpath, NOT a file path
		BeanFactory bf = new ClassPathXmlApplicationContext(new String[] {
					"/org/springframework/aop/framework/autoproxy/metadata/commonsAttributes.xml",
					"/org/springframework/aop/framework/autoproxy/metadata/enterpriseServices.xml"});
		return bf;
	}
	
	
}
