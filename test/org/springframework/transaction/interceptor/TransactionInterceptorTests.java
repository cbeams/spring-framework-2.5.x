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

package org.springframework.transaction.interceptor;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Mock object based tests for TransactionInterceptor.
 * @author Rod Johnson
 * @since 16-Mar-2003
*  @version $Id: TransactionInterceptorTests.java,v 1.13 2004-06-30 11:35:01 johnsonr Exp $
 */
public class TransactionInterceptorTests extends AbstractTransactionAspectTests {
	
	/**
	 * Template method to create an advised object given the 
	 * target object and transaction setup.
	 * Creates a TransactionInterceptor and applies it.
	 */
	protected Object advised(Object target, PlatformTransactionManager ptm, TransactionAttributeSource tas) {
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		assertEquals(ptm, ti.getTransactionManager());		
		ti.setTransactionAttributeSource(tas);
		assertEquals(tas, ti.getTransactionAttributeSource());

		ProxyFactory pf = new ProxyFactory(target);
		pf.addAdvice(0, ti);
		return pf.getProxy();
	}

}
