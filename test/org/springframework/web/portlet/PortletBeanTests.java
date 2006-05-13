package org.springframework.web.portlet;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;

import junit.framework.TestCase;

import org.springframework.mock.web.portlet.MockPortletConfig;
import org.springframework.mock.web.portlet.MockPortletContext;

public class PortletBeanTests extends TestCase {

	public void testInitParameterSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testValue = "testValue";
		portletConfig.addInitParameter("testParam", testValue);
		TestPortletBean portletBean = new TestPortletBean();
		assertNull(portletBean.getTestParam());
		portletBean.init(portletConfig);
		assertNotNull(portletBean.getTestParam());
		assertEquals(testValue, portletBean.getTestParam());
	}
	
	public void testInitParameterNotSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		TestPortletBean portletBean = new TestPortletBean();
		assertNull(portletBean.getTestParam());
		portletBean.init(portletConfig);
		assertNull(portletBean.getTestParam());
	}

	public void testMultipleInitParametersSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testValue = "testValue";
		String anotherValue = "anotherValue";
		portletConfig.addInitParameter("testParam", testValue);
		portletConfig.addInitParameter("anotherParam", anotherValue);
		portletConfig.addInitParameter("unknownParam", "unknownValue");
		TestPortletBean portletBean = new TestPortletBean();
		assertNull(portletBean.getTestParam());
		assertNull(portletBean.getAnotherParam());
		portletBean.init(portletConfig);
		assertNotNull(portletBean.getTestParam());
		assertNotNull(portletBean.getAnotherParam());
		assertEquals(testValue, portletBean.getTestParam());
		assertEquals(anotherValue, portletBean.getAnotherParam());
	}

	public void testMultipleInitParametersOnlyOneSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testValue = "testValue";
		portletConfig.addInitParameter("testParam", testValue);
		portletConfig.addInitParameter("unknownParam", "unknownValue");
		TestPortletBean portletBean = new TestPortletBean();
		assertNull(portletBean.getTestParam());
		assertNull(portletBean.getAnotherParam());
		portletBean.init(portletConfig);
		assertNotNull(portletBean.getTestParam());
		assertEquals(testValue, portletBean.getTestParam());
		assertNull(portletBean.getAnotherParam());
	}

	public void testRequiredInitParameterSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testParam = "testParam";
		String testValue = "testValue";
		portletConfig.addInitParameter(testParam, testValue);
		TestPortletBean portletBean = new TestPortletBean();
		portletBean.addRequiredProperty(testParam);
		assertNull(portletBean.getTestParam());
		portletBean.init(portletConfig);
		assertNotNull(portletBean.getTestParam());
		assertEquals(testValue, portletBean.getTestParam());
	}

	public void testRequiredInitParameterNotSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testParam = "testParam";
		TestPortletBean portletBean = new TestPortletBean();
		portletBean.addRequiredProperty(testParam);
		assertNull(portletBean.getTestParam());
		try {
			portletBean.init(portletConfig);
			fail("should have thrown PortletException");
		}
		catch (PortletException ex) {
			// expected
		}
	}
	
	public void testRequiredInitParameterNotSetOtherParameterNotSet() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testParam = "testParam";
		String testValue = "testValue";
		portletConfig.addInitParameter(testParam, testValue);
		TestPortletBean portletBean = new TestPortletBean();
		portletBean.addRequiredProperty("anotherParam");
		assertNull(portletBean.getTestParam());
		try {
			portletBean.init(portletConfig);
			fail("should have thrown PortletException");
		}
		catch (PortletException ex) {
			// expected
		}
		assertNull(portletBean.getTestParam());
	}

	public void testUnknownRequiredInitParameter() throws Exception {
		PortletContext portletContext = new MockPortletContext();
		MockPortletConfig portletConfig = new MockPortletConfig(portletContext);
		String testParam = "testParam";
		String testValue = "testValue";
		portletConfig.addInitParameter(testParam, testValue);
		TestPortletBean portletBean = new TestPortletBean();
		portletBean.addRequiredProperty("unknownParam");
		assertNull(portletBean.getTestParam());
		try {
			portletBean.init(portletConfig);
			fail("should have thrown PortletException");
		}
		catch (PortletException ex) {
			// expected
		}
		assertNull(portletBean.getTestParam());
	}

	private static class TestPortletBean extends PortletBean {
		
		private String testParam; 
		private String anotherParam;
		
		public void setTestParam(String value) {
			this.testParam = value;
		}
		
		public String getTestParam() {
			return this.testParam;
		}
		
		public void setAnotherParam(String value) {
			this.anotherParam = value;
		}
		
		public String getAnotherParam() {
			return this.anotherParam;
		}
	}
}
