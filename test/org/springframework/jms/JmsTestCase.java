/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
