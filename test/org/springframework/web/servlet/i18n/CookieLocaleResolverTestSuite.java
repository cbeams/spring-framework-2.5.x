
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

package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/**
 * @author alef
 *
 */
public class CookieLocaleResolverTestSuite extends TestCase {

	/**
	 * Constructor for CookieLocaleResolverTestSuite.
	 * @param arg0
	 */
	public CookieLocaleResolverTestSuite(String arg0) {
		super(arg0);
	}

	public void testSetCookieName() {
		CookieLocaleResolver resolver = getCookieLocaleResolver();
		assertEquals(CookieLocaleResolver.DEFAULT_COOKIE_NAME, resolver.getCookieName());
		
		// yup, koekje is the Dutch name for Cookie ;-)
		resolver.setCookieName("LanguageKoekje");
		assertEquals(resolver.getCookieName(), "LanguageKoekje");		
	}

	public void testGetCookiePath() {
		CookieLocaleResolver resolver = getCookieLocaleResolver();
		assertEquals(CookieLocaleResolver.DEFAULT_COOKIE_PATH, resolver.getCookiePath());
		
		// yup, koekje is the Dutch name for Cookie ;-)
		resolver.setCookiePath("LanguageKoekje");
		assertEquals(resolver.getCookiePath(), "LanguageKoekje");		
	}

	public void testSetCookieMaxAge() {
		CookieLocaleResolver resolver = getCookieLocaleResolver();
		assertEquals(CookieLocaleResolver.DEFAULT_COOKIE_MAX_AGE, resolver.getCookieMaxAge());
		
		resolver.setCookieMaxAge(123456);
		assertEquals(resolver.getCookieMaxAge(), 123456);		
	}

	public void testResolveLocale() {
		MockServletContext context = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(context);
				
		Cookie c = new Cookie("LanguageKoek", "nl");		
		request.setCookies(new Cookie[] {c});
		
		CookieLocaleResolver resolver = getCookieLocaleResolver();
		resolver.setCookieName("LanguageKoek");
		Locale loc = resolver.resolveLocale(request);
		//System.out.println(loc.getLanguage());
		assertEquals(loc.getLanguage(), "nl");
	}

	public void testSetLocale() {
		MockServletContext context = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(context);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		CookieLocaleResolver resolver = getCookieLocaleResolver();
		resolver.setCookieName("LanguageKoek");
		resolver.setLocale(request, response, new Locale("nl"));
		
		Cookie[] cookies = response.getCookies();
		Cookie found = null;
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals("LanguageKoek")) {
				found = cookies[i];
				break;
			}
		}
		assertNotNull(found);
		//System.out.println("found '" + found.getValue() + "'");
		
		context = new MockServletContext();
		request = new MockHttpServletRequest(context);
		
		request.setCookies(new Cookie[] {found});

		resolver = getCookieLocaleResolver();
		resolver.setCookieName("LanguageKoek");
		Locale loc = resolver.resolveLocale(request);
		//System.out.println(loc.getLanguage());
		assertEquals(loc.getLanguage(), "nl");
	}
	
	private CookieLocaleResolver getCookieLocaleResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver();
		return resolver;
	}

}
