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

package org.springframework.web.servlet.mvc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class FormControllerTestSuite extends TestCase {
	
	public void testReferenceDataOnForm() throws Exception {
		String formView = "f";
		String successView = "s";
		
		RefController mc = new RefController();
		mc.setFormView(formView);
		mc.setCommandName("tb");
		mc.setSuccessView(successView);
		mc.refDataCount = 0;
		
		HttpServletRequest request = new MockHttpServletRequest("GET", "/welcome.html");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(formView));
		
		assertTrue("refDataCount == 1", mc.refDataCount == 1);
		
		TestBean person = (TestBean) mv.getModel().get(mc.getCommandName());
		int[] numbers = (int[]) mv.getModel().get(mc.NUMBERS_ATT);
		assertTrue("model is non null", person != null);
		assertTrue("numbers is non null", numbers != null);
	}

	public void testReferenceDataOnResubmit() throws Exception {
		String formView = "f";
		String successView = "s";
		
		RefController mc = new RefController();
		mc.setFormView(formView);
		mc.setCommandName("tb");
		mc.setSuccessView(successView);
		mc.refDataCount = 0;
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/welcome.html");
		request.addParameter("age", "23x");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(formView));
		assertTrue("has errors", mv.getModel().get(BindException.ERROR_KEY_PREFIX + mc.getCommandName()) != null);
		
		assertTrue("refDataCount == 1", mc.refDataCount == 1);
		
		TestBean person = (TestBean) mv.getModel().get(mc.getCommandName());
		int[] numbers = (int[]) mv.getModel().get(mc.NUMBERS_ATT);
		assertTrue("model is non null", person != null);
		assertTrue("numbers is non null", numbers != null);
	}

	public void testForm() throws Exception {
		String formView = "f";
		String successView = "s";
		
		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/welcome.html");
		request.addParameter("name", "rod");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(formView));
		
		TestBean person = (TestBean) mv.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
		assertTrue("bean age default ok", person.getAge() == TestController.DEFAULT_AGE);
		assertTrue("name not set", person.getName() == null);
	}

	public void testBindOnNewForm() throws Exception {
		String formView = "f";
		String successView = "s";

		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		mc.setBindOnNewForm(true);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/welcome.html");
		request.addParameter("name", "rod");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(formView));

		TestBean person = (TestBean) mv.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
		assertTrue("bean age default ok", person.getAge() == TestController.DEFAULT_AGE);
		assertTrue("name set", "rod".equals(person.getName()));
	}

	public void testSubmitNoErrors() throws Exception {
		String formView = "f";
		String successView = "s";
		
		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		
		String name = "Rod";
		int age = 32;

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/welcome.html");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);

		assertEquals("returned correct view name", successView, mv.getViewName());
		TestBean person = (TestBean) mv.getModel().get(TestController.BEAN_NAME);
		Errors errors = (Errors) mv.getModel().get(BindException.ERROR_KEY_PREFIX + TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
		assertTrue("errors is non null", errors != null);
		assertTrue("bean name bound ok", person.getName().equals(name));
		assertTrue("bean age bound ok", person.getAge() == age);
	}
	
	public void testSubmitWithCustomOnSubmit() throws Exception {
		String formView = "f";

		TestControllerWithCustomOnSubmit mc = new TestControllerWithCustomOnSubmit();
		mc.setFormView(formView);

		String name = "Rod";
		int age = 32;

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/welcome.html");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertEquals("returned correct view name", "mySuccess", mv.getViewName());
		assertTrue("no model", mv.getModel().isEmpty());
	}

	public void testSubmitPassedByValidator() throws Exception {
		String formView = "f";
		String successView = "s";
		
		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		mc.setValidator(new TestValidator());
		
		String name = "Roderick Johnson";
		int age = 32;

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/welcome.html");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name: expected '" + successView + "', not '" + mv.getViewName() + "'",
			mv.getViewName().equals(successView));
		
		TestBean person = (TestBean) mv.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
		assertTrue("bean name bound ok", person.getName().equals(name));
		assertTrue("bean age bound ok", person.getAge() == age);
	}
	
	public void testSubmit1Mismatch() throws Exception {
		String formView = "fred";
		String successView = "tony";
		
		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		
		String name = "Rod";
		String age = "xxx";

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo.html");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name: expected '" + formView + "', not '" + mv.getViewName() + "'",
		mv.getViewName().equals(formView));
		
		TestBean person = (TestBean) mv.getModel().get(mc.getCommandName());
		assertTrue("model is non null", person != null);
		assertTrue("bean name bound ok", person.getName().equals(name));
		assertTrue("bean age is default", person.getAge() == TestController.DEFAULT_AGE);
		Errors errors = (Errors) mv.getModel().get(BindException.ERROR_KEY_PREFIX + mc.getCommandName());
		assertTrue("errors returned in model", errors != null);
		assertTrue("One error", errors.getErrorCount() == 1);
		FieldError fe = errors.getFieldError("age");
		assertTrue("Saved invalid value", fe.getRejectedValue().equals(age));
		assertTrue("Correct field", fe.getField().equals("age"));
	}
	
	public void testSubmit1Mismatch1Invalidated() throws Exception {
		String formView = "fred";
		String successView = "tony";
		
		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		mc.setValidators(new Validator[] {new TestValidator(), new TestValidator2()});

		String name = "Rod";
		// will be rejected
		String age = "xxx";
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo.html");
		request.addParameter("name", name);
		request.addParameter("age", "" + age);
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name: expected '" + formView + "', not '" + mv.getViewName() + "'",
		mv.getViewName().equals(formView));
		
		TestBean person = (TestBean) mv.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
		
		// yes, but it was rejected after binding by the validator
		assertTrue("bean name bound ok", person.getName().equals(name));
		assertTrue("bean age is default", person.getAge() == TestController.DEFAULT_AGE);
		Errors errors = (Errors) mv.getModel().get(BindException.ERROR_KEY_PREFIX + mc.getCommandName());
		assertTrue("errors returned in model", errors != null);
		assertTrue("3 errors", errors.getErrorCount() == 3);
		FieldError fe = errors.getFieldError("age");
		assertTrue("Saved invalid value", fe.getRejectedValue().equals(age));
		assertTrue("Correct field", fe.getField().equals("age"));
		
		// raised by first validator
		fe = errors.getFieldError("name");
		assertTrue("Saved invalid value", fe.getRejectedValue().equals(name));
		assertTrue("Correct field", fe.getField().equals("name"));
		assertTrue("Correct validation code: expected '" +TestValidator.TOOSHORT + "', not '" 
		+ fe.getCode() + "'", fe.getCode().equals(TestValidator.TOOSHORT));

		// raised by second validator
		ObjectError oe = errors.getGlobalError();
		assertEquals("test", oe.getCode());
		assertEquals("testmessage", oe.getDefaultMessage());
	}

	public void testSessionController() throws Exception {
		String formView = "f";
		String successView = "s";

		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		mc.setSessionForm(true);

		// first request: GET form
		HttpServletRequest request1 = new MockHttpServletRequest("GET", "/welcome.html");
		HttpServletResponse response1 = new MockHttpServletResponse();
		ModelAndView mv1 = mc.handleRequest(request1, response1);
		assertTrue("returned correct view name", mv1.getViewName().equals(formView));
		TestBean person = (TestBean) mv1.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
		assertTrue("Bean age default ok", person.getAge() == TestController.DEFAULT_AGE);

		// second request, using same session: POST submit
		MockHttpServletRequest request2 = new MockHttpServletRequest("POST", "/welcome.html");
		request2.setSession(request1.getSession(false));
		HttpServletResponse response2 = new MockHttpServletResponse();
		ModelAndView mv2 = mc.handleRequest(request2, response2);
		assertTrue("returned correct view name", mv2.getViewName().equals(successView));
		TestBean person2 = (TestBean) mv2.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is same object", person == person2);
	}

	public void testDefaultInvalidSubmit() throws Exception {
		String formView = "f";
		String successView = "s";

		TestController mc = new TestController();
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		mc.setSessionForm(true);

		// invalid request: POST submit
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/welcome.html");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name", mv.getViewName().equals(successView));
		TestBean person = (TestBean) mv.getModel().get(TestController.BEAN_NAME);
		assertTrue("model is non null", person != null);
	}

	public void testSpecialInvalidSubmit() throws Exception {
		String formView = "f";
		String successView = "s";

		TestController mc = new TestController() {
			protected ModelAndView handleInvalidSubmit(HttpServletRequest request, HttpServletResponse response)
			    throws ServletException, IOException {
				throw new ServletException("invalid submit");
			}
		};
		mc.setFormView(formView);
		mc.setSuccessView(successView);
		mc.setSessionForm(true);

		// invalid request: POST submit
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/welcome.html");
		HttpServletResponse response = new MockHttpServletResponse();
		try {
			mc.handleRequest(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}
	}

	public void testSubmitWithIndexedProperties() throws Exception {
		String formView = "fred";
		String successView = "tony";

		SimpleFormController mc = new SimpleFormController();
		mc.setCommandClass(IndexedTestBean.class);
		mc.setFormView(formView);
		mc.setSuccessView(successView);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo.html");
		request.addParameter("array[0].name", "name3");
		request.addParameter("array[1].age", "name2");
		request.addParameter("list[0].name", "name1");
		request.addParameter("list[1].age", "name0");
		request.addParameter("list[2]", "listobj");
		request.addParameter("map[key1]", "mapobj1");
		request.addParameter("map[key3]", "mapobj2");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name: expected '" + formView + "', not '" + mv.getViewName() + "'",
		mv.getViewName().equals(formView));

		IndexedTestBean bean = (IndexedTestBean) mv.getModel().get(mc.getCommandName());
		assertTrue("model is non null", bean != null);
		assertEquals("name3", bean.getArray()[0].getName());
		assertEquals("name1", ((TestBean) bean.getList().get(0)).getName());
		Errors errors = (Errors) mv.getModel().get(BindException.ERROR_KEY_PREFIX + mc.getCommandName());
		assertTrue("errors returned in model", errors != null);
		assertTrue("2 errors", errors.getErrorCount() == 2);
		FieldError fe1 = errors.getFieldError("array[1].age");
		assertTrue("Saved invalid value", fe1.getRejectedValue().equals("name2"));
		assertTrue("Correct field", fe1.getField().equals("array[1].age"));
		FieldError fe2 = errors.getFieldError("list[1].age");
		assertTrue("Saved invalid value", fe2.getRejectedValue().equals("name0"));
		assertTrue("Correct field", fe2.getField().equals("list[1].age"));

		assertEquals("listobj", bean.getList().get(2));
		assertEquals("mapobj1", bean.getMap().get("key1"));
		assertEquals("mapobj2", bean.getMap().get("key3"));
	}

	public void testFormChangeRequest() throws Exception {
		String formView = "fred";
		String successView = "tony";

		TestController mc = new TestController() {
			protected boolean isFormChangeRequest(HttpServletRequest request) {
				return (request.getParameter("formChange") != null);
			}
		};
		mc.setFormView(formView);
		mc.setSuccessView(successView);

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/foo.html");
		request.addParameter("name", "Rod");
		request.addParameter("age", "99");
		request.addParameter("formChange", "true");
		HttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = mc.handleRequest(request, response);
		assertTrue("returned correct view name: expected '" + formView + "', not '" + mv.getViewName() + "'",
		mv.getViewName().equals(formView));

		TestBean person = (TestBean) mv.getModel().get(mc.getCommandName());
		assertTrue("model is non null", person != null);
		assertTrue("bean name bound ok", person.getName().equals("Rod"));
		assertTrue("bean age is 99", person.getAge() == 99);
		Errors errors = (Errors) mv.getModel().get(BindException.ERROR_KEY_PREFIX + mc.getCommandName());
		assertTrue("errors returned in model", errors != null);
		assertTrue("No errors", errors.getErrorCount() == 0);
	}


	private static class TestValidator implements Validator {

		public static String TOOSHORT = "tooshort";

		public boolean supports(Class clazz) { return true; }

		public void validate(Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb.getName() == null || "".equals(tb.getName()))
				errors.rejectValue("name", "needname", null, "need name");
			else if (tb.getName().length() < 5)
				errors.rejectValue("name", TOOSHORT, null, "need full name");
		}
	}


	private static class TestValidator2 implements Validator {

		public static String TOOSHORT = "tooshort";

		public boolean supports(Class clazz) { return true; }

		public void validate(Object obj, Errors errors) {
			errors.reject("test", "testmessage");
		}
	}


	private static class TestController extends SimpleFormController {
		
		public static String BEAN_NAME = "person";
		
		public static int DEFAULT_AGE = 52;
		
		public TestController() {
			setCommandClass(TestBean.class);
			setCommandName(BEAN_NAME);
		}
		
		protected Object formBackingObject(HttpServletRequest request) throws ServletException {
			TestBean person = new TestBean();
			person.setAge(DEFAULT_AGE);
			return person;
		}
	}

	public void testFormControllerInWebApplicationContext() {
		StaticWebApplicationContext ctx = new StaticWebApplicationContext();
		ctx.setServletContext(new MockServletContext());
		RefController mc = new RefController();
		mc.setApplicationContext(ctx);
		try {
			mc.invokeWebSpecificStuff();
		}
		catch (IllegalStateException ex) {
			fail("Shouldn't have thrown exception: " + ex.getMessage());
		}
	}

	public void testFormControllerInNonWebApplicationContext() {
		StaticApplicationContext ctx = new StaticApplicationContext();
		RefController mc = new RefController();
		mc.setApplicationContext(ctx);
		try {
			mc.invokeWebSpecificStuff();
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}


	private static class TestControllerWithCustomOnSubmit extends TestController {

		protected ModelAndView onSubmit(Object command) throws Exception {
			return new ModelAndView("mySuccess");
		}
	}


	private static class RefController extends SimpleFormController {
		
		final String NUMBERS_ATT = "NUMBERS";
		
		static final int[] NUMBERS = { 1, 2, 3, 4 };
		
		int refDataCount;
		
		public RefController() {
			setCommandClass(TestBean.class);
		}
		
		protected Map referenceData(HttpServletRequest request) {
			++refDataCount;
			Map m = new HashMap();
			m.put(NUMBERS_ATT, NUMBERS);
			return m;
		}

		public void invokeWebSpecificStuff() {
			getTempDir();
		}
	}

}

