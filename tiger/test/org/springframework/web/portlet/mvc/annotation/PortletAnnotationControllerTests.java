/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.portlet.mvc.annotation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.portlet.MockActionRequest;
import org.springframework.mock.web.portlet.MockActionResponse;
import org.springframework.mock.web.portlet.MockPortletConfig;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.portlet.DispatcherPortlet;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * @author Juergen Hoeller
 * @since 2.5
 */
public class PortletAnnotationControllerTests extends TestCase {

	public void testStandardHandleMethod() throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyController.class));
				wac.refresh();
				return wac;
			}
		};
		portlet.init(new MockPortletConfig());

		MockRenderRequest request = new MockRenderRequest(PortletMode.VIEW);
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("test", response.getContentAsString());
	}

	public void testAdaptedHandleMethods() throws Exception {
		doTestAdaptedHandleMethods(MyAdaptedController.class);
	}

	public void testAdaptedHandleMethods2() throws Exception {
		doTestAdaptedHandleMethods(MyAdaptedController2.class);
	}

	public void testAdaptedHandleMethods3() throws Exception {
		doTestAdaptedHandleMethods(MyAdaptedController3.class);
	}

	public void doTestAdaptedHandleMethods(final Class controllerClass) throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(controllerClass));
				wac.refresh();
				return wac;
			}
		};
		portlet.init(new MockPortletConfig());

		MockActionRequest actionRequest = new MockActionRequest(PortletMode.VIEW);
		MockActionResponse actionResponse = new MockActionResponse();
		portlet.processAction(actionRequest, actionResponse);
		assertEquals("value", actionResponse.getRenderParameter("test"));

		MockRenderRequest request = new MockRenderRequest(PortletMode.EDIT);
		request.addParameter("param1", "value1");
		request.addParameter("param2", "2");
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("test-value1-2", response.getContentAsString());

		request = new MockRenderRequest(PortletMode.HELP);
		request.addParameter("name", "name1");
		request.addParameter("age", "2");
		response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("test-name1-2", response.getContentAsString());

		request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("name", "name1");
		request.addParameter("age", "value2");
		response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("test-name1-typeMismatch", response.getContentAsString());
	}

	public void testFormController() throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyFormController.class));
				wac.refresh();
				return wac;
			}
			protected void render(ModelAndView mv, RenderRequest request, RenderResponse response) throws Exception {
				new TestView().render(mv.getViewName(), mv.getModel(), request, response);
			}
		};
		portlet.init(new MockPortletConfig());

		MockRenderRequest request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("name", "name1");
		request.addParameter("age", "value2");
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myView-name1-typeMismatch-tb1-myValue", response.getContentAsString());
	}

	public void testCommandProvidingFormController() throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyCommandProvidingFormController.class));
				RootBeanDefinition adapterDef = new RootBeanDefinition(AnnotationMethodHandlerAdapter.class);
				adapterDef.getPropertyValues().addPropertyValue("webBindingInitializer", new MyWebBindingInitializer());
				wac.registerBeanDefinition("handlerAdapter", adapterDef);
				wac.refresh();
				return wac;
			}
			protected void render(ModelAndView mv, RenderRequest request, RenderResponse response) throws Exception {
				new TestView().render(mv.getViewName(), mv.getModel(), request, response);
			}
		};
		portlet.init(new MockPortletConfig());

		MockRenderRequest request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("defaultName", "myDefaultName");
		request.addParameter("age", "value2");
		request.addParameter("date", "2007-10-02");
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myView-myDefaultName-typeMismatch-tb1-myOriginalValue", response.getContentAsString());
	}

	public void testBinderInitializingCommandProvidingFormController() throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyBinderInitializingCommandProvidingFormController.class));
				wac.refresh();
				return wac;
			}
			protected void render(ModelAndView mv, RenderRequest request, RenderResponse response) throws Exception {
				new TestView().render(mv.getViewName(), mv.getModel(), request, response);
			}
		};
		portlet.init(new MockPortletConfig());

		MockRenderRequest request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("defaultName", "myDefaultName");
		request.addParameter("age", "value2");
		request.addParameter("date", "2007-10-02");
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myView-myDefaultName-typeMismatch-tb1-myOriginalValue", response.getContentAsString());
	}

	public void testSpecificBinderInitializingCommandProvidingFormController() throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MySpecificBinderInitializingCommandProvidingFormController.class));
				wac.refresh();
				return wac;
			}
			protected void render(ModelAndView mv, RenderRequest request, RenderResponse response) throws Exception {
				new TestView().render(mv.getViewName(), mv.getModel(), request, response);
			}
		};
		portlet.init(new MockPortletConfig());

		MockRenderRequest request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("defaultName", "myDefaultName");
		request.addParameter("age", "value2");
		request.addParameter("date", "2007-10-02");
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myView-myDefaultName-typeMismatch-tb1-myOriginalValue", response.getContentAsString());
	}

	public void testParameterDispatchingController() throws Exception {
		DispatcherPortlet portlet = new DispatcherPortlet() {
			protected ApplicationContext createPortletApplicationContext(ApplicationContext parent) throws BeansException {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyParameterDispatchingController.class));
				wac.refresh();
				return wac;
			}
		};
		portlet.init(new MockPortletConfig());

		MockRenderRequest request = new MockRenderRequest(PortletMode.VIEW);
		MockRenderResponse response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myView", response.getContentAsString());

		request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("view", "other");
		response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myOtherView", response.getContentAsString());

		request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("view", "my");
		request.addParameter("lang", "de");
		response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("myLangView", response.getContentAsString());

		request = new MockRenderRequest(PortletMode.VIEW);
		request.addParameter("surprise", "!");
		response = new MockRenderResponse();
		portlet.render(request, response);
		assertEquals("mySurpriseView", response.getContentAsString());
	}


	@RequestMapping("VIEW")
	private static class MyController extends AbstractController {

		protected ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
			response.getWriter().write("test");
			return null;
		}
	}


	@Controller
	private static class MyAdaptedController {

		@RequestMapping("VIEW")
		public void myHandle(ActionRequest request, ActionResponse response) throws IOException {
			response.setRenderParameter("test", "value");
		}

		@RequestMapping("EDIT")
		public void myHandle(@RequestParam("param1") String p1, @RequestParam("param2") int p2, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + p1 + "-" + p2);
		}

		@RequestMapping("HELP")
		public void myHandle(TestBean tb, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + tb.getAge());
		}

		@RequestMapping("VIEW")
		public void myHandle(TestBean tb, Errors errors, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + errors.getFieldError("age").getCode());
		}
	}


	@Controller
	private static class MyAdaptedController2 {

		@RequestMapping("VIEW")
		public void myHandle(ActionRequest request, ActionResponse response) throws IOException {
			response.setRenderParameter("test", "value");
		}

		@RequestMapping("EDIT")
		public void myHandle(@RequestParam("param1") String p1, int param2, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + p1 + "-" + param2);
		}

		@RequestMapping("HELP")
		public void myHandle(TestBean tb, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + tb.getAge());
		}

		@RequestMapping("VIEW")
		public void myHandle(TestBean tb, Errors errors, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + errors.getFieldError("age").getCode());
		}
	}


	@Controller
	@RequestMapping({"VIEW", "EDIT", "HELP"})
	private static class MyAdaptedController3 {

		@RequestMapping
		public void myHandle(ActionRequest request, ActionResponse response) {
			response.setRenderParameter("test", "value");
		}

		@RequestMapping("EDIT")
		public void myHandle(@RequestParam("param1") String p1, @RequestParam("param2") int p2, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + p1 + "-" + p2);
		}

		@RequestMapping("HELP")
		public void myHandle(TestBean tb, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + tb.getAge());
		}

		@RequestMapping
		public void myHandle(TestBean tb, Errors errors, RenderResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + errors.getFieldError("age").getCode());
		}
	}


	@Controller
	private static class MyFormController {

		@ModelAttribute("testBeans")
		public List<TestBean> getTestBeans() {
			List<TestBean> list = new LinkedList<TestBean>();
			list.add(new TestBean("tb1"));
			list.add(new TestBean("tb2"));
			return list;
		}

		@RequestMapping("VIEW")
		public String myHandle(@ModelAttribute("myCommand") TestBean tb, BindingResult errors, ModelMap model) {
			if (!model.containsKey("myKey")) {
				model.addObject("myKey", "myValue");
			}
			return "myView";
		}
	}


	@Controller
	private static class MyCommandProvidingFormController extends MyFormController {

		@ModelAttribute("myCommand")
		private TestBean createTestBean(@RequestParam String defaultName, Map<String, Object> model) {
			model.put("myKey", "myOriginalValue");
			return new TestBean(defaultName);
		}
	}


	@Controller
	private static class MyBinderInitializingCommandProvidingFormController extends MyCommandProvidingFormController {

		@InitBinder
		private void initBinder(WebDataBinder binder) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setLenient(false);
			binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		}
	}


	@Controller
	private static class MySpecificBinderInitializingCommandProvidingFormController extends MyCommandProvidingFormController {

		@InitBinder("myCommand")
		private void initBinder(WebDataBinder binder) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setLenient(false);
			binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		}
	}


	private static class MyWebBindingInitializer implements WebBindingInitializer {

		public void initBinder(WebDataBinder binder, WebRequest request) {
			assertNotNull(request.getLocale());
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setLenient(false);
			binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		}
	}


	@Controller
	@RequestMapping("VIEW")
	private static class MyParameterDispatchingController {

		@RequestMapping
		public void myHandle(RenderResponse response) throws IOException {
			response.getWriter().write("myView");
		}

		@RequestMapping(params = "view=other")
		public void myOtherHandle(RenderResponse response) throws IOException {
			response.getWriter().write("myOtherView");
		}

		@RequestMapping(params = {"view=my", "lang=de"})
		public void myLangHandle(RenderResponse response) throws IOException {
			response.getWriter().write("myLangView");
		}

		@RequestMapping(params = "surprise")
		public void mySurpriseHandle(RenderResponse response) throws IOException {
			response.getWriter().write("mySurpriseView");
		}
	}


	private static class TestView {

		public void render(String viewName, Map model, RenderRequest request, RenderResponse response) throws Exception {
			TestBean tb = (TestBean) model.get("myCommand");
			if ("myDefaultName".equals(tb.getName())) {
				assertTrue(tb.getDate().getYear() == 107);
			}
			Errors errors = (Errors) model.get(BindingResult.MODEL_KEY_PREFIX + "myCommand");
			if (errors.hasFieldErrors("date")) {
				throw new IllegalStateException();
			}
			List<TestBean> testBeans = (List<TestBean>) model.get("testBeans");
			response.getWriter().write(viewName + "-" + tb.getName() + "-" + errors.getFieldError("age").getCode() +
					"-" + testBeans.get(0).getName() + "-" + model.get("myKey"));
		}
	}

}
