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

package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.theme.FixedThemeResolver;

/**
 * @author Juergen Hoeller
 * @since 18.06.2003
 */
public class ViewResolverTestSuite extends TestCase {

	public void testBeanNameViewResolver() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		MutablePropertyValues pvs1 = new MutablePropertyValues();
		pvs1.addPropertyValue(new PropertyValue("url", "/example1.jsp"));
		wac.registerSingleton("example1", InternalResourceView.class, pvs1);
		MutablePropertyValues pvs2 = new MutablePropertyValues();
		pvs2.addPropertyValue(new PropertyValue("url", "/example2.jsp"));
		wac.registerSingleton("example2", JstlView.class, pvs2);
		BeanNameViewResolver vr = new BeanNameViewResolver();
		vr.setApplicationContext(wac);
		wac.refresh();

		View view = vr.resolveViewName("example1", Locale.getDefault());
		assertTrue("Correct view class", InternalResourceView.class.equals(view.getClass()));
		assertTrue("Correct URL", "/example1.jsp".equals(((InternalResourceView) view).getUrl()));

		view = vr.resolveViewName("example2", Locale.getDefault());
		assertTrue("Correct view class", JstlView.class.equals(view.getClass()));
		assertTrue("Correct URL", "/example2.jsp".equals(((InternalResourceView) view).getUrl()));
	}

	public void testInternalResourceViewResolverWithoutPrefixes() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		InternalResourceViewResolver vr = new InternalResourceViewResolver();
		vr.setApplicationContext(wac);
		vr.setContentType("myContentType");
		vr.setRequestContextAttribute("rc");

		View view = vr.resolveViewName("example1", Locale.getDefault());
		assertTrue("Correct view class", InternalResourceView.class.equals(view.getClass()));
		assertTrue("Correct URL", "example1".equals(((InternalResourceView) view).getUrl()));
		assertTrue("Correct contentType", "myContentType".equals(((InternalResourceView) view).getContentType()));

		view = vr.resolveViewName("example2", Locale.getDefault());
		assertTrue("Correct view class", InternalResourceView.class.equals(view.getClass()));
		assertTrue("Correct URL", "example2".equals(((InternalResourceView) view).getUrl()));
		assertTrue("Correct contentType", "myContentType".equals(((InternalResourceView) view).getContentType()));

		HttpServletRequest request = new MockHttpServletRequest(wac.getServletContext());
		HttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, new FixedThemeResolver());
		Map model = new HashMap();
		TestBean tb = new TestBean();
		model.put("tb", tb);
		view.render(model, request, response);
		assertTrue("Correct tb attribute", tb.equals(request.getAttribute("tb")));
		assertTrue("Correct rc attribute", request.getAttribute("rc") instanceof RequestContext);
	}

	public void testInternalResourceViewResolverWithPrefixes() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		InternalResourceViewResolver vr = new InternalResourceViewResolver();
		vr.setPrefix("/WEB-INF/");
		vr.setSuffix(".jsp");
		vr.setApplicationContext(wac);

		View view = vr.resolveViewName("example1", Locale.getDefault());
		assertTrue("Correct view class", InternalResourceView.class.equals(view.getClass()));
		assertTrue("Correct URL", "/WEB-INF/example1.jsp".equals(((InternalResourceView) view).getUrl()));

		view = vr.resolveViewName("example2", Locale.getDefault());
		assertTrue("Correct view class", InternalResourceView.class.equals(view.getClass()));
		assertTrue("Correct URL", "/WEB-INF/example2.jsp".equals(((InternalResourceView) view).getUrl()));
	}

	public void testInternalResourceViewResolverWithJstl() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		InternalResourceViewResolver vr = new InternalResourceViewResolver();
		vr.setViewClass(JstlView.class);
		vr.setApplicationContext(wac);

		View view = vr.resolveViewName("example1", Locale.getDefault());
		assertTrue("Correct view class", JstlView.class.equals(view.getClass()));
		assertTrue("Correct URL", "example1".equals(((InternalResourceView) view).getUrl()));

		view = vr.resolveViewName("example2", Locale.getDefault());
		assertTrue("Correct view class", JstlView.class.equals(view.getClass()));
		assertTrue("Correct URL", "example2".equals(((InternalResourceView) view).getUrl()));

		ServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		Locale locale = !Locale.GERMAN.equals(Locale.getDefault()) ? Locale.GERMAN : Locale.ENGLISH;
		request.addPreferredLocale(locale);
		HttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		Map model = new HashMap();
		TestBean tb = new TestBean();
		model.put("tb", tb);
		view.render(model, request, response);
		assertTrue("Correct tb attribute", tb.equals(request.getAttribute("tb")));
		assertTrue("Correct rc attribute", request.getAttribute("rc") == null);
		assertTrue("Correct JSTL attributes", request.getAttribute(Config.FMT_LOCALIZATION_CONTEXT) instanceof LocalizationContext);
		assertTrue("Correct JSTL attributes", locale.equals(request.getAttribute(Config.FMT_LOCALE)));
		assertTrue("Correct JSTL attributes", request.getAttribute(Config.FMT_LOCALIZATION_CONTEXT + JstlUtils.REQUEST_SCOPE_SUFFIX) instanceof LocalizationContext);
		assertTrue("Correct JSTL attributes", locale.equals(request.getAttribute(Config.FMT_LOCALE + JstlUtils.REQUEST_SCOPE_SUFFIX)));
	}

	public void testXmlViewResolver() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.registerSingleton("testBean", TestBean.class, null);
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		TestBean testBean = (TestBean) wac.getBean("testBean");
		XmlViewResolver vr = new XmlViewResolver();
		vr.setLocation(new ClassPathResource("org/springframework/web/servlet/view/views.xml"));
		vr.setApplicationContext(wac);

		View view1 = vr.resolveViewName("example1", Locale.getDefault());
		assertTrue("Correct view class", TestView.class.equals(view1.getClass()));
		assertTrue("Correct URL", "/example1.jsp".equals(((InternalResourceView) view1).getUrl()));

		View view2 = vr.resolveViewName("example2", Locale.getDefault());
		assertTrue("Correct view class", JstlView.class.equals(view2.getClass()));
		assertTrue("Correct URL", "/example2new.jsp".equals(((InternalResourceView) view2).getUrl()));

		ServletContext sc = new MockServletContext();
		Map model = new HashMap();
		TestBean tb = new TestBean();
		model.put("tb", tb);

		HttpServletRequest request = new MockHttpServletRequest(sc);
		HttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, new FixedThemeResolver());
		view1.render(model, request, response);
		assertTrue("Correct tb attribute", tb.equals(request.getAttribute("tb")));
		assertTrue("Correct test1 attribute", "testvalue1".equals(request.getAttribute("test1")));
		assertTrue("Correct test2 attribute", testBean.equals(request.getAttribute("test2")));

		request = new MockHttpServletRequest(sc);
		response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, new FixedThemeResolver());
		view2.render(model, request, response);
		assertTrue("Correct tb attribute", tb.equals(request.getAttribute("tb")));
		assertTrue("Correct test1 attribute", "testvalue1".equals(request.getAttribute("test1")));
		assertTrue("Correct test2 attribute", "testvalue2".equals(request.getAttribute("test2")));
	}

	public void testXmlViewResolverDefaultLocation() {
		StaticWebApplicationContext wac = new StaticWebApplicationContext() {
			protected Resource getResourceByPath(String path) {
				assertTrue("Correct default location", XmlViewResolver.DEFAULT_LOCATION.equals(path));
				return super.getResourceByPath(path);
			}
		};
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		XmlViewResolver vr = new XmlViewResolver();
		try {
			vr.setApplicationContext(wac);
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testXmlViewResolverWithoutCache() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext() {
			protected Resource getResourceByPath(String path) {
				assertTrue("Correct default location", XmlViewResolver.DEFAULT_LOCATION.equals(path));
				return super.getResourceByPath(path);
			}
		};
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		XmlViewResolver vr = new XmlViewResolver();
		vr.setCache(false);
		try {
			vr.setApplicationContext(wac);
		}
		catch (ApplicationContextException ex) {
			fail("Should not have thrown ApplicationContextException: " + ex.getMessage());
		}
		try {
			vr.resolveViewName("example1", Locale.getDefault());
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testCacheRemoval() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());
		wac.refresh();
		InternalResourceViewResolver vr = new InternalResourceViewResolver();
		vr.setViewClass(JstlView.class);
		vr.setApplicationContext(wac);
	
		View view = vr.resolveViewName("example1", Locale.getDefault());		
		View cached = vr.resolveViewName("example1", Locale.getDefault());
		if (view != cached) {
			fail("Caching doesn't work");
		}
		
		((AbstractCachingViewResolver)vr).removeFromCache("example1", Locale.getDefault());
		cached = vr.resolveViewName("example1", Locale.getDefault());
		if (view == cached) {
			// the chance of having the same reference (hashCode) twice if negligable).
			fail("View wasn't removed from cache");
		}
	}

	public static class TestView extends InternalResourceView {

		public void setLocation(Resource location) {
			if (!(location instanceof ServletContextResource)) {
				throw new IllegalArgumentException("Expecting ClassPathResource, not " + location.getClass().getName());
			}
		}
	}

}
