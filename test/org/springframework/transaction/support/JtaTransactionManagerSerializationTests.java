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

package org.springframework.transaction.support;

import javax.transaction.UserTransaction;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.jndi.JndiTemplate;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.SerializationTestUtils;

/**
 * 
 * @author Rod Johnson
 * @version $Id: JtaTransactionManagerSerializationTests.java,v 1.1 2004-07-27 09:18:42 johnsonr Exp $
 */
public class JtaTransactionManagerSerializationTests extends MockObjectTestCase {

	public void testSerializable() throws Exception {
		JtaTransactionManager jtam = new JtaTransactionManager();
		
		Mock utMock = new Mock(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utMock.proxy();
		
		Mock jtMock = mock(JndiTemplate.class, "m");
		String userTransactionName = "java:comp/UserTransaction";
		jtMock.expects(once()).method("lookup").with(same(userTransactionName)).will(returnValue(ut));
		
		
		JndiTemplate mockJt = (JndiTemplate) jtMock.proxy();
		jtam.setJndiTemplate(mockJt);
		
		// Make it do lookup
		jtam.afterPropertiesSet();
		
		SimpleNamingContextBuilder jndiEnv = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		jndiEnv.bind(userTransactionName, ut);
		
		JtaTransactionManager jtam2 = (JtaTransactionManager) SerializationTestUtils.serializeAndDeserialize(jtam);
		
		// Should do client-side lookup
		
		assertNotNull("Logger must survive serialization", jtam2.logger);
		assertNotNull("UserTransaction looked up on client", jtam2.getUserTransaction());
		assertNull("TransactionManager didn't survive", jtam2.getTransactionManager());
		jtMock.verify();
	}
}
