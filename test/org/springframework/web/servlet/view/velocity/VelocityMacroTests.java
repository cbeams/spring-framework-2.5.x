/*
 * Copyright 2002-2005 the original author or authors.
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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.DummyMacroRequestContext;

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
				RequestContext rc = (RequestContext) context.get(VelocityView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE);
				BindStatus status = rc.getBindStatus("tb.name");
				assertEquals("name", status.getExpression());
				assertEquals("juergen", status.getValue());
			}
		};
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		vv.setExposeSpringMacroHelpers(true);

		Map model = new HashMap();
		model.put("tb", new TestBean("juergen", 99));
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

	public void testAllMacros() {
		DummyMacroRequestContext rc = new DummyMacroRequestContext();
		HashMap msgMap = new HashMap();
		msgMap.put("hello", "Howdy");
		msgMap.put("world", "Mundo");
		rc.setMsgMap(msgMap);
		rc.setContextPath("/springtest");

		TestBean tb = new TestBean("Darren", 99);
		rc.setCommand(tb);

		HashMap names = new HashMap();
		names.put("Darren", "Darren Davison");
		names.put("John", "John Doe");
		names.put("Fred", "Fred Bloggs");

		try {
			VelocityConfigurer vc = new VelocityConfigurer();
			vc.setPreferFileSystemAccess(false);
			VelocityEngine ve = vc.createVelocityEngine();
			VelocityContext context = new VelocityContext();
			context.put("command", tb);
			context.put("springMacroRequestContext", rc);
			context.put("nameOptionMap", names);

			StringWriter sw = new StringWriter();
			ve.mergeTemplate("org/springframework/web/servlet/view/velocity/test.vm", "UTF-8", context, sw);
			// tokenize output and ignore whitespace
			String output = sw.getBuffer().toString();

			String[] tokens = StringUtils.tokenizeToStringArray(output, "\t\n");

			//for (int i=0; i<tokens.length; i++) System.out.println(tokens[i]);

			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].equals("NAME")) assertEquals("Darren", tokens[i + 1]);
				if (tokens[i].equals("AGE")) assertEquals("99", tokens[i + 1]);
				if (tokens[i].equals("MESSAGE")) assertEquals("Howdy Mundo", tokens[i + 1]);
				if (tokens[i].equals("DEFAULTMESSAGE")) assertEquals("hi planet", tokens[i + 1]);
				if (tokens[i].equals("URL")) assertEquals("/springtest/aftercontext.html", tokens[i + 1]);
				if (tokens[i].equals("FORM1")) assertEquals("<input type=\"text\" name=\"name\" value=\"Darren\" >", tokens[i + 1]);
				if (tokens[i].equals("FORM2")) assertEquals("<input type=\"text\" name=\"name\" value=\"Darren\" class=\"myCssClass\">", tokens[i + 1]);
				if (tokens[i].equals("FORM3")) assertEquals("<textarea name=\"name\" >Darren</textarea>", tokens[i + 1]);
				if (tokens[i].equals("FORM4")) assertEquals("<textarea name=\"name\" rows=10 cols=30>Darren</textarea>", tokens[i + 1]);
				//TODO verify remaining output (fix whitespace)
				if (tokens[i].equals("FORM9")) assertEquals("<input type=\"password\" name=\"name\" value=\"\" >", tokens[i + 1]);
				if (tokens[i].equals("FORM10")) assertEquals("<input type=\"hidden\" name=\"name\" value=\"Darren\" >", tokens[i + 1]);
			}
		}
		catch (Exception e) {
			fail();
		}
	}
}
