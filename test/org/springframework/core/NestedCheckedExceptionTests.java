/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @version $Id: NestedCheckedExceptionTests.java,v 1.2 2004-02-02 11:38:54 jhoeller Exp $
 */
public class NestedCheckedExceptionTests extends TestCase {

	public void testNoRootCause() {
		String mesg = "mesg of mine";
		// Making a class abstract doesn't _really_ prevent instantiation :-)
		NestedCheckedException nce = new NestedCheckedException(mesg) {};
		assertNull(nce.getCause());
		assertEquals(nce.getMessage(), mesg);
		
		// Check PrintStackTrace
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 PrintWriter pw = new PrintWriter(baos);
		 nce.printStackTrace(pw);
		 pw.flush();
		 String stackTrace = new String(baos.toByteArray());
		 assertFalse(stackTrace.indexOf(mesg) == -1);
	}
	
	public void testRootCause() {
		String myMessage = "mesg for this exception";
		String rootCauseMesg = "this is the obscure message of the root cause";
		ServletException rootCause = new ServletException(rootCauseMesg);
		// Making a class abstract doesn't _really_ prevent instantiation :-)
		NestedCheckedException nce = new NestedCheckedException(myMessage, rootCause) {};
		assertEquals(nce.getCause(), rootCause);
		assertTrue(nce.getMessage().indexOf(myMessage) != -1);
		assertTrue(nce.getMessage().indexOf(rootCauseMesg) != -1);
		
		// Check PrintStackTrace
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		nce.printStackTrace(pw);
		pw.flush();
		String stackTrace = new String(baos.toByteArray());
		assertFalse(stackTrace.indexOf(rootCause.getClass().getName()) == -1);
		assertFalse(stackTrace.indexOf(rootCauseMesg) == -1);
	}

}
