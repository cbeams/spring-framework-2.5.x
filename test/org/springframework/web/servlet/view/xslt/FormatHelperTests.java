/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.xslt;

import java.util.Locale;

import junit.framework.TestCase;
import org.w3c.dom.Element;

/**
 * Test the FormatHelper methods.
 * @author Rod Johnson
 * @author Darren Davison
 * @since 26-Jul-2003
 * @version $Id: FormatHelperTests.java,v 1.5 2003-11-07 15:11:54 jhoeller Exp $
 */
public class FormatHelperTests extends TestCase {

	static long testTime = 1064359582063L; //appx 00:26 on 24/9/2003 in the UK (GMT +1)
		
	/**
	 * test null params for Locale
	 */
	public void testNullParamsForLocale() {
		Element e;
		String s;
		try {
			e = (Element) FormatHelper.dateTimeElement(testTime, null, null);
			e = (Element) FormatHelper.dateTimeElement(testTime, "", null);
			e = (Element) FormatHelper.dateTimeElement(testTime, null, "");						
		} catch (Throwable ex) {
			fail( "Passing null params to dateTimeElement(long, String, String) throws " + ex.getClass().getName());
		}

		try {
			s = FormatHelper.currency(50d, null, null);
			s = FormatHelper.currency(50d, "", null);
			s = FormatHelper.currency(50d, null, "");
		} catch (Throwable ex) {
			fail( "Passing null params to currency(long, String, String) throws " + ex.getClass().getName());
		}
	}
	
	/*
	 * Test for Node dateTimeElement(long, String, String)
	 */
	public void testDateTimeElement() {
		Element e = (Element) FormatHelper.dateTimeElement(testTime, Locale.UK);
		assertTrue(e.getTagName().equals("formatted-date"));
		Element el;
		el = (Element) e.getElementsByTagName("year").item(0);
		assertTrue( "2003".equals(el.getFirstChild().getNodeValue() ));
		el = (Element) e.getElementsByTagName("month").item(0);
		assertTrue( "September".equals(el.getFirstChild().getNodeValue() ));
		el = (Element) e.getElementsByTagName("day-of-week").item(0);
		assertTrue( "Wednesday".equals(el.getFirstChild().getNodeValue() ));
		el = (Element) e.getElementsByTagName("day-of-month").item(0);
		assertTrue( "24".equals(el.getFirstChild().getNodeValue() ));
		/*
		// mysteriously fails on some installation
		el = (Element) e.getElementsByTagName("hours").item(0);
		assertEquals( "12", el.getFirstChild().getNodeValue() );
		*/
		el = (Element) e.getElementsByTagName("minutes").item(0);
		assertTrue( "26".equals(el.getFirstChild().getNodeValue() ));
		el = (Element) e.getElementsByTagName("am-pm").item(0);
		assertTrue( "AM".equals(el.getFirstChild().getNodeValue() ));
		
		// prove a different locale changes the output
		e = (Element) FormatHelper.dateTimeElement(testTime, Locale.FRANCE);
		el = (Element) e.getElementsByTagName("day-of-week").item(0);
		assertTrue( "mercredi".equals(el.getFirstChild().getNodeValue() ));
	}

	public void testCurrency() {
		String s;
		s = FormatHelper.currency( 50.0d, Locale.US);
		assertTrue( "$50.00".equals(s));
		
		// pound sign (#163)
		s = FormatHelper.currency( 50.0d, Locale.UK);
		assertEquals( 163, s.charAt(0));
		assertTrue( "50.00".equals(s.substring(1)));		
		
		// comma replace dec.point
		s = FormatHelper.currency( 50.0d, Locale.FRANCE);
		assertTrue( "50,00".equals(s.substring(0,5)));
		
	}

}
