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

package org.springframework.web.servlet.view.xmlc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.enhydra.xml.io.OutputOptions;
import org.enhydra.xml.xmlc.XMLObject;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

/**
 * @author Rob Harrop
 */
public class XmlcViewTests extends TestCase {

	private final MockServletContext servletContext = new MockServletContext();
	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private final MockHttpServletResponse response = new MockHttpServletResponse();

	private static final StaticWebApplicationContext context = new StaticWebApplicationContext();

	public void testCreateXMLObject() throws Exception {
		final Map model = new HashMap();

		final AbstractXmlcView view = new AbstractXmlcView() {
			protected XMLObject createXMLObject(Map innerModel, HttpServletRequest innerRequest) {
				assertEquals("Model is not the same", model, innerModel);
				assertEquals("Request is not the same", request, innerRequest);
				return new TestXmlObject();
			}
		};

		renderView(model, view);
	}

	public void testOutput() throws Exception {
		OutputTestView view = new OutputTestView();
		renderView(null, view);

		String output = response.getContentAsString();

		assertTrue("Title wasn't changed", output.indexOf("My New Title") > -1);
		assertTrue("Text wasn't changed", output.indexOf("My New Text") > -1);
	}

	public void testWithCustomOutputOptions() throws Exception {

		OutputOptionsTestView view = new OutputOptionsTestView();
		renderView(null, view);
		assertTrue("getOutputOptions() was not called", view.getOutputOptionsCalled);
	}

	private void renderView(Map model, AbstractXmlcView view) throws Exception {
		context.setServletContext(servletContext);
		view.setApplicationContext(context);
		view.initApplicationContext();
		view.renderMergedOutputModel(model, request, response);
	}


	private static class OutputTestView extends AbstractXmlcView {

		protected XMLObject createXMLObject(Map model, HttpServletRequest request) {
			TestXmlObject xmlObject = new TestXmlObject();
			xmlObject.setTitle("My New Title");
			xmlObject.setTextTest("My New Text");
			return xmlObject;
		}
	}


	private static class OutputOptionsTestView extends AbstractXmlcView {

		public boolean getOutputOptionsCalled = false;

		protected XMLObject createXMLObject(Map model, HttpServletRequest request) throws ServletException {
			return new TestXmlObject();
		}

		protected OutputOptions getOutputOptions() {
			getOutputOptionsCalled = true;
			return new OutputOptions();
		}
	}

}
