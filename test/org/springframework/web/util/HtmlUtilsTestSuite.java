/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.springframework.web.util;

import junit.framework.TestCase;

/**
 * @author alef
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HtmlUtilsTestSuite extends TestCase {

	/**
	 * Constructor for HtmlUtilsTestSuite.
	 * @param arg0
	 */
	public HtmlUtilsTestSuite(String arg0) {
		super(arg0);
	}

	public void testHtmlEscape() {
		
		String unescaped = "\"This is a quote";
		
		String escaped = HtmlUtils.htmlEscape(unescaped);
		
		if (escaped.startsWith("&#")) {
			assertEquals("&#34;This is a quote",escaped);
		} else {
			assertEquals("&quot;This is a quote",escaped);
		}
	}

	public void testHtmlUnescape() {
		String escaped = "&quot;This is a quote";		
		
		String unescaped = HtmlUtils.htmlUnescape(escaped);
		
		assertEquals(unescaped, "\"This is a quote");
		
	}

}
