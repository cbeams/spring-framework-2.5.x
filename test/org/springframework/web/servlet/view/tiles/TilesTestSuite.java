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

package org.springframework.web.servlet.view.tiles;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import junit.framework.TestCase;
import org.apache.struts.taglib.tiles.ComponentConstants;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.PathAttribute;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * @author Alef Arendsen
 */
public class TilesTestSuite extends TestCase {

	protected WebApplicationContext prepareWebApplicationContext() throws Exception {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		MockServletContext sc = new MockServletContext("/org/springframework/web/servlet/view/tiles/");
		wac.setServletContext(sc);
		wac.refresh();

		TilesConfigurer tc = new TilesConfigurer();
		tc.setDefinitions(new String[] {"tiles-test.xml"});
		tc.setValidateDefinitions(true);
		tc.setApplicationContext(wac);
		tc.afterPropertiesSet();

		return wac;
	}

	public void testTilesView() throws Exception {
		WebApplicationContext wac = prepareWebApplicationContext();

		InternalResourceViewResolver irvr = new InternalResourceViewResolver();
		irvr.setApplicationContext(wac);
		irvr.setViewClass(TilesView.class);
		View view = irvr.resolveViewName("testTile", new Locale("nl", ""));

		MockHttpServletRequest request = new MockHttpServletRequest(wac.getServletContext());
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		view.render(new HashMap(), request, response);
		assertEquals("/WEB-INF/jsp/layout.jsp", response.getForwardedUrl());
		ComponentContext cc = (ComponentContext) request.getAttribute(ComponentConstants.COMPONENT_CONTEXT);
		assertNotNull(cc);
		PathAttribute attr = (PathAttribute) cc.getAttribute("content");
		assertEquals("/WEB-INF/jsp/content.jsp", attr.getValue());

		view.render(new HashMap(), request, response);
		assertEquals("/WEB-INF/jsp/layout.jsp", response.getForwardedUrl());
		cc = (ComponentContext) request.getAttribute(ComponentConstants.COMPONENT_CONTEXT);
		assertNotNull(cc);
		attr = (PathAttribute) cc.getAttribute("content");
		assertEquals("/WEB-INF/jsp/content.jsp", attr.getValue());
	}

	public void testTilesJstlView() throws Exception {
		WebApplicationContext wac = prepareWebApplicationContext();

		InternalResourceViewResolver irvr = new InternalResourceViewResolver();
		irvr.setApplicationContext(wac);
		irvr.setViewClass(TilesJstlView.class);
		View view = irvr.resolveViewName("testTile", new Locale("nl", ""));

		MockHttpServletRequest request = new MockHttpServletRequest(wac.getServletContext());
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		view.render(new HashMap(), request, response);
		assertEquals("/WEB-INF/jsp/layout.jsp", response.getForwardedUrl());
		ComponentContext cc = (ComponentContext) request.getAttribute(ComponentConstants.COMPONENT_CONTEXT);
		assertNotNull(cc);
		PathAttribute attr = (PathAttribute) cc.getAttribute("content");
		assertEquals("/WEB-INF/jsp/content.jsp", attr.getValue());

		assertTrue("Correct JSTL attributes", request.getAttribute(Config.FMT_LOCALIZATION_CONTEXT) instanceof LocalizationContext);
		assertTrue("Correct JSTL attributes", request.getLocale().equals(request.getAttribute(Config.FMT_LOCALE)));
		assertTrue("Correct JSTL attributes", request.getAttribute(Config.FMT_LOCALIZATION_CONTEXT + JstlUtils.REQUEST_SCOPE_SUFFIX) instanceof LocalizationContext);
		assertTrue("Correct JSTL attributes", request.getLocale().equals(request.getAttribute(Config.FMT_LOCALE + JstlUtils.REQUEST_SCOPE_SUFFIX)));
	}

	public void testTilesViewWithController() throws Exception {
		WebApplicationContext wac = prepareWebApplicationContext();

		InternalResourceViewResolver irvr = new InternalResourceViewResolver();
		irvr.setApplicationContext(wac);
		irvr.setViewClass(TilesView.class);
		View view = irvr.resolveViewName("testTileWithController", new Locale("nl", ""));

		MockHttpServletRequest request = new MockHttpServletRequest(wac.getServletContext());
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		view.render(new HashMap(), request, response);
		assertEquals("/WEB-INF/jsp/layout.jsp", response.getForwardedUrl());
		ComponentContext cc = (ComponentContext) request.getAttribute(ComponentConstants.COMPONENT_CONTEXT);
		assertNotNull(cc);
		PathAttribute attr = (PathAttribute) cc.getAttribute("content");
		assertEquals("/WEB-INF/jsp/otherContent.jsp", attr.getValue());
		assertEquals("testVal", request.getAttribute("testAttr"));

		view.render(new HashMap(), request, response);
		assertEquals("/WEB-INF/jsp/layout.jsp", response.getForwardedUrl());
		cc = (ComponentContext) request.getAttribute(ComponentConstants.COMPONENT_CONTEXT);
		assertNotNull(cc);
		attr = (PathAttribute) cc.getAttribute("content");
		assertEquals("/WEB-INF/jsp/otherContent.jsp", attr.getValue());
		assertEquals("testVal", request.getAttribute("testAttr"));
	}

}
