/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.xslt;

import java.util.Locale;

import junit.framework.TestCase;

import org.w3c.dom.Element;

/**
 * 
 * @author Rod Johnson
 * @since 26-Jul-2003
 * @version $Id: FormatHelperTests.java,v 1.1.1.1 2003-08-14 16:21:30 trisberg Exp $
 */
public class FormatHelperTests extends TestCase {

	/**
	 * Constructor for FormatHelperTests.
	 * @param arg0
	 */
	public FormatHelperTests(String arg0) {
		super(arg0);
	}

	/*
	 * Test for Node dateTimeElement(long, String, String)
	 */
	public void testUkDateTimeElement() {
		long t = System.currentTimeMillis();
		Element e = (Element) FormatHelper.dateTimeElement(t, Locale.UK);
		assertTrue(e.getTagName().equals("formatted-date"));
		Element monthEle = (Element) e.getElementsByTagName("month").item(0);
		// TODO finish this test case
	}

	

}
