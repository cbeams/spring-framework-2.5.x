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
package org.springframework.jms;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.easymock.MockControl;

import junit.framework.TestCase;

/**
 * Helper class to put common functionality for JMS 1.0.2 and 1.1 test cases.
 * @author Andre Biryukov
 * @author Mark Pollack
 */
public class JmsTestCase extends TestCase {

	protected static Context mockJndiContext;
	
	protected MockControl mockJndiControl;
	
	public JmsTestCase(String name)
	{
		super(name);
	}
	
	static {
		try {
			NamingManager
				.setInitialContextFactoryBuilder(
					new InitialContextFactoryBuilder() {
				public InitialContextFactory createInitialContextFactory(Hashtable hashtable)
					throws NamingException {
					return new InitialContextFactory() {
						public Context getInitialContext(Hashtable hashtable)
							throws NamingException {
							return mockJndiContext;
						}
					};
				}
			});
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
}
