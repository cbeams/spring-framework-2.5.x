/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import javax.ejb.EJBException;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.beans.FatalBeanException;

/**
 * 
 * @author Rod Johnson
 * @since 09-Apr-2003
 * @version $Revision: 1.2 $
 */
public class RollbackRuleTests extends TestCase {

	/**
	 * Constructor for RollbackRuleTests.
	 * @param arg0
	 */
	public RollbackRuleTests(String arg0) {
		super(arg0);
	}

	public void testFoundImmediatelyWithString() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Exception");
		assertTrue(rr.getDepth(new Exception()) == 0);
	}
	
	public void testFoundImmediatelyWithClass() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute(Exception.class);
		assertTrue(rr.getDepth(new Exception()) == 0);
	}
	
	public void testNotFound() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("javax.servlet.ServletException");
		assertTrue(rr.getDepth(new EJBException()) == -1);
	}
	
	public void testAncestry() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Exception");
		// Exception -> Runtime -> EJBException
		assertTrue(rr.getDepth(new EJBException()) == 2);
	}
	
	
	public void testAlwaysTrue() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Throwable");
		// Exception -> Runtime -> EJBException
		assertTrue(rr.getDepth(new EJBException()) > 0);
		assertTrue(rr.getDepth(new ServletException()) > 0);
		assertTrue(rr.getDepth(new FatalBeanException(null,null)) > 0);
		assertTrue(rr.getDepth(new RuntimeException()) > 0);
	}
	
	public void testConstructorArgMustBeAThrowableClass() {
		try {
			new RollbackRuleAttribute(StringBuffer.class);
			fail("Can't construct a RollbackRuleAttribute without a throwable");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}

}
