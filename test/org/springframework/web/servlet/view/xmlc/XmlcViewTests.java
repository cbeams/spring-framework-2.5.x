
package org.springframework.web.servlet.view.xmlc;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.support.StaticWebApplicationContext;

import org.enhydra.xml.xmlc.servlet.XMLCContext;
import org.enhydra.xml.xmlc.XMLObject;
import org.enhydra.xml.io.OutputOptions;

/**
 * @author robh
 */
public class XmlcViewTests extends TestCase {

	private final MockServletContext servletContext = new MockServletContext();

	private final MockHttpServletRequest request = new MockHttpServletRequest();

	private final MockHttpServletResponse response = new MockHttpServletResponse();

	private final XMLCContext xmlcContext = XMLCContext.getContext(servletContext);

	private static final StaticWebApplicationContext context = new StaticWebApplicationContext();

	public void testCreateXMLObject() throws Exception {

		final Map model = new HashMap();

		final AbstractXmlcView view = new AbstractXmlcView() {
			protected XMLObject createXMLObject(Map innerModel, HttpServletRequest innerRequest,
					HttpServletResponse innerResponse, XMLCContext innerContext) throws ServletException {
				assertEquals("Model is not the same", model, innerModel);
				assertEquals("Request is not the same", request, innerRequest);
				assertEquals("Response is not the same", response, innerResponse);
				assertEquals("XMLCContext is not the same", xmlcContext, innerContext);

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

		protected XMLObject createXMLObject(Map model, HttpServletRequest request, HttpServletResponse response, XMLCContext context) throws ServletException {
			TestXmlObject xmlObject = new TestXmlObject();
			xmlObject.setTitle("My New Title");
			xmlObject.setTextTest("My New Text");
			return xmlObject;
		}
	}

	private static class OutputOptionsTestView extends AbstractXmlcView {

		public boolean getOutputOptionsCalled = false;

		protected XMLObject createXMLObject(Map model, HttpServletRequest request, HttpServletResponse response, XMLCContext context) throws ServletException {
			return new TestXmlObject();
		}

		protected OutputOptions getOutputOptions() {
			getOutputOptionsCalled = true;
			return new OutputOptions();
		}
	}


}
