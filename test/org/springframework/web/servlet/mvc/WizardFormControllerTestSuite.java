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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @since 29.04.2003
 */
public class WizardFormControllerTestSuite extends TestCase {

	public void testNoDirtyPageChange() throws Exception {
		AbstractWizardFormController wizard = createWizard();
		wizard.setAllowDirtyBack(false);
		wizard.setAllowDirtyForward(false);
		wizard.setPageAttribute("currentPage");
		HttpSession session = performRequest(wizard, null, null, 0, null, 0, "currentPage");

		Properties params = new Properties();
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1", "value");
		performRequest(wizard, session, null, 0, null, 0, "currentPage");
		// not allowed to go to 1

		params.clear();
		params.setProperty("name", "myname");
		params.setProperty(AbstractWizardFormController.PARAM_PAGE, "0");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1", "value");
		performRequest(wizard, session, params, 1, "myname", 0, "currentPage");
		// name set -> now allowed to go to 1

		params.clear();
		params.setProperty("name", "myname");
		performRequest(wizard, session, params, 1, "myname", 0, "currentPage");
		// name set -> now allowed to go to 1

		params.clear();
		params.setProperty("name", "myname");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1.x", "value");
		performRequest(wizard, session, params, 1, "myname", 0, "currentPage");
		// name set -> now allowed to go to 1

		params.clear();
		params.setProperty("name", "myname");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1.y", "value");
		performRequest(wizard, session, params, 1, "myname", 0, "currentPage");
		// name set -> now allowed to go to 1

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "0", "value");
		performRequest(wizard, session, params, 1, "myname", 0, "currentPage");
		// not allowed to go to 0

		params.clear();
		params.setProperty("age", "32");
		params.setProperty(AbstractWizardFormController.PARAM_PAGE, "1");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "0", "value");
		performRequest(wizard, session, params, 0, "myname", 32, "currentPage");
		// age set -> now allowed to go to 0

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_FINISH, "value");
		performRequest(wizard, session, params, -1, "myname", 32, null);

		assertTrue(session.getAttribute(wizard.getFormSessionAttributeName()) == null);
		assertTrue(session.getAttribute(wizard.getPageSessionAttributeName()) == null);
	}

	public void testDirtyBack() throws Exception {
		AbstractWizardFormController wizard = createWizard();
		wizard.setAllowDirtyBack(true);
		wizard.setAllowDirtyForward(false);
		HttpSession session = performRequest(wizard, null, null, 0, null, 0, null);

		Properties params = new Properties();
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1", "value");
		performRequest(wizard, session, params, 0, null, 0, null);
		// not allowed to go to 1

		params.clear();
		params.setProperty("name", "myname");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1", "value");
		performRequest(wizard, session, params, 1, "myname", 0, null);
		// name set -> now allowed to go to 1

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "0", "value");
		performRequest(wizard, session, params, 0, "myname", 0, null);
		// dirty back -> allowed to go to 0

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_FINISH, "value");
		performRequest(wizard, session, params, 1, "myname", 0, null);
		// finish while dirty -> show dirty page (1)

		params.clear();
		params.setProperty("age", "32");
		params.setProperty(AbstractWizardFormController.PARAM_FINISH, "value");
		performRequest(wizard, session, params, -1, "myname", 32, null);
		// age set -> now allowed to finish
	}

	public void testDirtyForward() throws Exception {
		AbstractWizardFormController wizard = createWizard();
		wizard.setAllowDirtyBack(false);
		wizard.setAllowDirtyForward(true);
		HttpSession session = performRequest(wizard, null, null, 0, null, 0, null);

		Properties params = new Properties();
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1", "value");
		performRequest(wizard, session, params, 1, null, 0, null);
		// dirty forward -> allowed to go to 1

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "0", "value");
		performRequest(wizard, session, params, 1, null, 0, null);
		// not allowed to go to 0

		params.clear();
		params.setProperty("age", "32");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "0", "value");
		performRequest(wizard, session, params, 0, null, 32, null);
		// age set -> now allowed to go to 0

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_FINISH, "value");
		performRequest(wizard, session, params, 0, null, 32, null);
		// finish while dirty -> show dirty page (0)

		params.clear();
		params.setProperty("name", "myname");
		params.setProperty(AbstractWizardFormController.PARAM_FINISH + ".x", "value");
		performRequest(wizard, session, params, -1, "myname", 32, null);
		// name set -> now allowed to finish
	}

	public void testAbort() throws Exception {
		AbstractWizardFormController wizard = createWizard();
		HttpSession session = performRequest(wizard, null, null, 0, null, 0, null);
		Properties params = new Properties();
		params.setProperty(AbstractWizardFormController.PARAM_CANCEL, "value");
		performRequest(wizard, session, params, -2, null, 0, null);

		assertTrue(session.getAttribute(wizard.getFormSessionAttributeName()) == null);
		assertTrue(session.getAttribute(wizard.getPageSessionAttributeName()) == null);

		session = performRequest(wizard, null, null, 0, null, 0, null);
		params = new Properties();
		params.setProperty(AbstractWizardFormController.PARAM_CANCEL + ".y", "value");
		performRequest(wizard, session, params, -2, null, 0, null);
	}

	public void testInvalidSubmit() throws Exception {
		AbstractWizardFormController wizard = createWizard();
		wizard.setAllowDirtyBack(false);
		wizard.setAllowDirtyForward(false);
		wizard.setPageAttribute("currentPage");
		HttpSession session = performRequest(wizard, null, null, 0, null, 0, "currentPage");

		Properties params = new Properties();
		params.setProperty("name", "myname");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "1", "value");
		performRequest(wizard, session, params, 1, "myname", 0, "currentPage");

		params.clear();
		params.setProperty("age", "32");
		params.setProperty(AbstractWizardFormController.PARAM_TARGET + "0", "value");
		performRequest(wizard, session, params, 0, "myname", 32, "currentPage");

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_FINISH, "value");
		performRequest(wizard, session, params, -1, "myname", 32, null);

		params.clear();
		params.setProperty(AbstractWizardFormController.PARAM_FINISH, "value");
		performRequest(wizard, session, params, 0, null, 0, "currentPage");
		// returned to initial page of new wizard form
	}

	private AbstractWizardFormController createWizard() {
		AbstractWizardFormController wizard = new TestWizardController(TestBean.class, "tb");
		wizard.setPages(new String[] {"page0", "page1"});
		return wizard;
	}

	private HttpSession performRequest(Controller wizard, HttpSession session, Properties params,
	                                   int target, String name, int age, String pageAttr) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest((params != null ? "POST" : "GET"), "/wizard");
		if (params != null) {
			for (Iterator it = params.keySet().iterator(); it.hasNext();) {
				String param = (String) it.next();
				request.addParameter(param, params.getProperty(param));
			}
		}
		request.setSession(session);
		request.setAttribute("target", new Integer(target));
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = wizard.handleRequest(request, response);
		if (target >= 0) {
			assertTrue("Page " + target + " returned", ("page" + target).equals(mv.getViewName()));
			if (pageAttr != null) {
				assertTrue("Page attribute set", (new Integer(target)).equals(mv.getModel().get(pageAttr)));
				assertTrue("Correct model size", mv.getModel().size() == 3);
			}
			else {
				assertTrue("Correct model size", mv.getModel().size() == 2);
			}
		}
		else if (target == -1) {
			assertTrue("Success target returned", "success".equals(mv.getViewName()));
			assertTrue("Correct model size", mv.getModel().size() == 1);
		}
		else if (target == -2) {
			assertTrue("Abort view returned", "abort".equals(mv.getViewName()));
			assertTrue("Correct model size", mv.getModel().size() == 1);
		}
		TestBean tb = (TestBean) mv.getModel().get("tb");
		assertTrue("Has model", tb != null);
		assertTrue("Name is " + name, (tb.getName() == name || name.equals(tb.getName())));
		assertTrue("Age is " + age, tb.getAge() == age);
		return request.getSession(false);
	}


	private class TestWizardController extends AbstractWizardFormController {

		public TestWizardController(Class commandClass, String beanName) {
			setCommandClass(commandClass);
			setCommandName(beanName);
		}

		protected Map referenceData(HttpServletRequest request, int page) throws Exception {
			assertEquals(request.getAttribute("target"), new Integer(page));
			return super.referenceData(request, page);
		}

		protected void validatePage(Object command, Errors errors, int page) {
			TestBean tb = (TestBean) command;
			switch (page) {
				case 0:
					if (tb.getName() == null) {
						errors.rejectValue("name", "NAME_REQUIRED", null, "Name is required");
					}
					break;
				case 1:
					if (tb.getAge() == 0) {
						errors.rejectValue("age", "AGE_REQUIRED", null, "Age is required");
					}
					break;
			  default:
					throw new IllegalArgumentException("Invalid page number");
			}
		}

		protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response,
		                                     Object command, BindException errors)
		    throws ServletException, IOException {
			assertTrue(getCurrentPage(request) == 0 || getCurrentPage(request) == 1);
			return new ModelAndView("success", getCommandName(), command);
		}

		protected ModelAndView processCancel(HttpServletRequest request, HttpServletResponse response,
		                                    Object command, BindException errors)
		    throws ServletException, IOException {
			assertTrue(getCurrentPage(request) == 0 || getCurrentPage(request) == 1);
			return new ModelAndView("abort", getCommandName(), command);
		}
	}

}
