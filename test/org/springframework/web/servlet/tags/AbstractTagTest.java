package org.springframework.web.servlet.tags;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.SimpleWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;

import junit.framework.TestCase;

import com.mockobjects.servlet.MockPageContext;

/**
 * Abstract test for testing tags (provides createPageContext)
 * @author Alef Arendsen
 */
public abstract class AbstractTagTest extends TestCase {
	
	protected MockPageContext createPageContext() {
		MockServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc, "GET", "/test");
		SimpleWebApplicationContext wac = new SimpleWebApplicationContext();
		wac.setServletContext(sc);
		wac.setNamespace("test");
		wac.refresh();
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		LocaleResolver lr = new AcceptHeaderLocaleResolver();
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, lr);
		ThemeResolver tr = new FixedThemeResolver();
		request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, tr);

		MockPageContext pc = new MockPageContext() {
			private Map attributes = new HashMap();

			public void setAttribute(String s, Object o) {
				attributes.put(s, o);
			}

			public Object getAttribute(String s) {
				return attributes.get(s);
			}
		
			public void setAttribute(String key, Object value, int scope) {
				if (scope == PageContext.PAGE_SCOPE) {
					this.setAttribute(key, value);
				} else {
					super.setAttribute(key, value, scope);
				}
			}
		
			public Object findAttribute(String key) {
				if (attributes.containsKey(key)) {
					return attributes.get(key);
				}
				return super.findAttribute(key);
			}
		
			public String toString() {
				Iterator it = attributes.keySet().iterator();
				while (it.hasNext()) {
					System.out.println(it.next());
				}
				return "String";
			}
		};
		pc.setServletContext(sc);
		pc.setRequest(request);
		return pc;
	}


}
