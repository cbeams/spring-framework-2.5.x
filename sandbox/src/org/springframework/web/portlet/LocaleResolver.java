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
package org.springframework.web.portlet;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * Interface for web-based locale resolution strategies that allows for
 * both locale resolution via the request and locale modification via
 * request and response.
 *
 * <p>This interface allows for implementations based on request, session,
 * cookies, etc. The default implementation is PortletLocaleResolver,
 * simply using the local managed by the portlet container.
 *
 * <p>Use RequestContext.getLocale() to retrieve the current locale in
 * controllers or views, independent of the actual resolution strategy.
 *
 * <p>Realize that portlet containers typically manage locale as one
 * of their features, so changing the locale through this framework is
 * usually not supported or desirable.
 * 
 * @see org.springframework.web.portlet.i18n.PortletLocaleResolver
 * @see javax.portlet.PortletRequest#getLocale
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 */
public interface LocaleResolver extends org.springframework.web.servlet.LocaleResolver {

    /**
     * Resolve the current locale via the given request.
     * Should return a default locale as fallback in any case.
     * @param request request to be used for resolution
     * @return the current locale
     */
  	Locale resolveLocale(PortletRequest request);

    /**
     * Set the current locale to the given one.
     * @param request request to be used for locale modification
     * @param response response to be used for locale modification
     * @param locale the new locale, or <code>null</code> to clear the locale
	 * @throws UnsupportedOperationException if the LocaleResolver implementation
	 * does not support dynamic changing of the locale
     */
  	void setLocale(PortletRequest request, PortletResponse response, Locale locale);

}
