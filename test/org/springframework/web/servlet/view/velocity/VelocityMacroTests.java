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

package org.springframework.web.servlet.view.velocity;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.velocity.VelocityViewTests.TestVelocityEngine;

/**
 * @author Darren Davison
 * @since 18.06.2004
 */
public class VelocityMacroTests extends TestCase {

	private final String templateName = "test.vm";

	private StaticWebApplicationContext wac;

	private HttpServletRequest request;

	private HttpServletResponse expectedResponse;


	public void setUp() throws Exception {
		wac = new StaticWebApplicationContext();
		wac.setServletContext(new MockServletContext());

		final Template expectedTemplate = new Template();
		VelocityConfig vc = new VelocityConfig() {
			public VelocityEngine getVelocityEngine() {
				return new TestVelocityEngine(templateName, expectedTemplate);
			}
		};
		wac.getDefaultListableBeanFactory().registerSingleton("velocityConfigurer", vc);
		wac.refresh();

		request = new MockHttpServletRequest();
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, new FixedThemeResolver());
		expectedResponse = new MockHttpServletResponse();
	}

	public void testExposeSpringMacroHelpers() throws Exception {
		VelocityView vv = new VelocityView() {
			protected void mergeTemplate(Template template, Context context, HttpServletResponse response) {
				assertTrue(context.get(VelocityView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE) instanceof RequestContext);
			}
		};
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		vv.setExposeSpringMacroHelpers(true);

		Map model = new HashMap();
		vv.render(model, request, expectedResponse);
	}

	public void testSpringMacroRequestContextAttributeUsed() {
		final String helperTool = "wrongType";

		VelocityView vv = new VelocityView() {
			protected void mergeTemplate(Template template, Context context, HttpServletResponse response) {
				fail();
			}
		};
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		vv.setExposeSpringMacroHelpers(true);

		Map model = new HashMap();
		model.put(VelocityView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE, helperTool);

		try {
			vv.render(model, request, expectedResponse);
		}
		catch (Exception ex) {
			assertTrue(ex instanceof ServletException);
			assertTrue(ex.getMessage().indexOf(VelocityView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE) > -1);
		}
	}

}
