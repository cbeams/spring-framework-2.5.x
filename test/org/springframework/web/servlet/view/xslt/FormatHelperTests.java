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

package org.springframework.web.servlet.view.xslt;

import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;
import org.w3c.dom.Element;

/**
 * Test the FormatHelper methods.
 * @author Rod Johnson
 * @author Darren Davison
 * @since 26-Jul-2003
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
	
	public void testDateTimeElement() {
		// save the current default TZ
		TimeZone curr = TimeZone.getDefault();
		// force the right TZ to ensure parsed Date works
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

		Element e = (Element) FormatHelper.dateTimeElement(testTime, Locale.UK);
		assertTrue(e.getTagName().equals("formatted-date"));
		Element el;
		el = (Element) e.getElementsByTagName("year").item(0);
		assertTrue("2003".equals(el.getFirstChild().getNodeValue()));
		el = (Element) e.getElementsByTagName("month").item(0);
		assertTrue("September".equals(el.getFirstChild().getNodeValue()));
		el = (Element) e.getElementsByTagName("day-of-week").item(0);
		assertTrue("Wednesday".equals(el.getFirstChild().getNodeValue()));
		el = (Element) e.getElementsByTagName("day-of-month").item(0);
		assertTrue("24".equals(el.getFirstChild().getNodeValue()));
		el = (Element) e.getElementsByTagName("hours").item(0);
		assertEquals( "12", el.getFirstChild().getNodeValue() );
		el = (Element) e.getElementsByTagName("minutes").item(0);
		assertTrue("26".equals(el.getFirstChild().getNodeValue()));
		el = (Element) e.getElementsByTagName("am-pm").item(0);
		assertTrue("AM".equals(el.getFirstChild().getNodeValue()));

		// prove a different locale changes the output
		e = (Element) FormatHelper.dateTimeElement(testTime, Locale.FRANCE);
		el = (Element) e.getElementsByTagName("day-of-week").item(0);
		assertTrue("mercredi".equals(el.getFirstChild().getNodeValue()));

		// reset TZ in case later tests have a problem
		TimeZone.setDefault(curr);
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
