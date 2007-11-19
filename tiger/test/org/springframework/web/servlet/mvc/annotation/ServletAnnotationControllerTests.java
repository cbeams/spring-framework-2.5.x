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

package org.springframework.web.servlet.mvc.annotation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.interceptor.SimpleTraceInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;

/**
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
public class ServletAnnotationControllerTests extends TestCase {

	public void testStandardHandleMethod() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyController.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("test", response.getContentAsString());
	}

	public void testProxiedStandardHandleMethod() throws Exception {
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyController.class));
				DefaultAdvisorAutoProxyCreator autoProxyCreator = new DefaultAdvisorAutoProxyCreator();
				autoProxyCreator.setBeanFactory(wac.getBeanFactory());
				wac.getBeanFactory().addBeanPostProcessor(autoProxyCreator);
				wac.getBeanFactory().registerSingleton("advisor", new DefaultPointcutAdvisor(new SimpleTraceInterceptor()));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("test", response.getContentAsString());
	}

	public void testEmptyParameterListHandleMethod() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(EmptyParameterListHandlerMethodController.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/emptyParameterListHandler");
		MockHttpServletResponse response = new MockHttpServletResponse();

		EmptyParameterListHandlerMethodController.called = false;
		servlet.service(request, response);
		assertTrue(EmptyParameterListHandlerMethodController.called);
		assertEquals("", response.getContentAsString());
	}

	public void testAdaptedHandleMethods() throws Exception {
		doTestAdaptedHandleMethods(MyAdaptedController.class);
	}

	public void testAdaptedHandleMethods2() throws Exception {
		doTestAdaptedHandleMethods(MyAdaptedController2.class);
	}

	private void doTestAdaptedHandleMethods(final Class<?> controllerClass) throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(controllerClass));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath1.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("test", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myPath2.do");
		request.addParameter("param1", "value1");
		request.addParameter("param2", "2");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("test-value1-2", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myPath3.do");
		request.addParameter("name", "name1");
		request.addParameter("age", "2");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("test-name1-2", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myPath4.do");
		request.addParameter("name", "name1");
		request.addParameter("age", "value2");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("test-name1-typeMismatch", response.getContentAsString());
	}

	public void testFormController() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyFormController.class));
				wac.registerBeanDefinition("viewResolver", new RootBeanDefinition(TestViewResolver.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("name", "name1");
		request.addParameter("age", "value2");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView-name1-typeMismatch-tb1-myValue", response.getContentAsString());
	}

	public void testProxiedFormController() throws Exception {
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyFormController.class));
				wac.registerBeanDefinition("viewResolver", new RootBeanDefinition(TestViewResolver.class));
				DefaultAdvisorAutoProxyCreator autoProxyCreator = new DefaultAdvisorAutoProxyCreator();
				autoProxyCreator.setBeanFactory(wac.getBeanFactory());
				wac.getBeanFactory().addBeanPostProcessor(autoProxyCreator);
				wac.getBeanFactory().registerSingleton("advisor", new DefaultPointcutAdvisor(new SimpleTraceInterceptor()));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("name", "name1");
		request.addParameter("age", "value2");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView-name1-typeMismatch-tb1-myValue", response.getContentAsString());
	}

	public void testCommandProvidingFormController() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyCommandProvidingFormController.class));
				wac.registerBeanDefinition("viewResolver", new RootBeanDefinition(TestViewResolver.class));
				RootBeanDefinition adapterDef = new RootBeanDefinition(AnnotationMethodHandlerAdapter.class);
				adapterDef.getPropertyValues().addPropertyValue("webBindingInitializer", new MyWebBindingInitializer());
				wac.registerBeanDefinition("handlerAdapter", adapterDef);
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("defaultName", "myDefaultName");
		request.addParameter("age", "value2");
		request.addParameter("date", "2007-10-02");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView-myDefaultName-typeMismatch-tb1-myOriginalValue", response.getContentAsString());
	}

	public void testBinderInitializingCommandProvidingFormController() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyBinderInitializingCommandProvidingFormController.class));
				wac.registerBeanDefinition("viewResolver", new RootBeanDefinition(TestViewResolver.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("defaultName", "myDefaultName");
		request.addParameter("age", "value2");
		request.addParameter("date", "2007-10-02");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView-myDefaultName-typeMismatch-tb1-myOriginalValue", response.getContentAsString());
	}

	public void testSpecificBinderInitializingCommandProvidingFormController() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MySpecificBinderInitializingCommandProvidingFormController.class));
				wac.registerBeanDefinition("viewResolver", new RootBeanDefinition(TestViewResolver.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("defaultName", "myDefaultName");
		request.addParameter("age", "value2");
		request.addParameter("date", "2007-10-02");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView-myDefaultName-typeMismatch-tb1-myOriginalValue", response.getContentAsString());
	}

	public void testParameterDispatchingController() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyParameterDispatchingController.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myPath.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("view", "other");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myOtherView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("view", "my");
		request.addParameter("lang", "de");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myLangView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myPath.do");
		request.addParameter("surprise", "!");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("mySurpriseView", response.getContentAsString());
	}

	public void testMethodNameDispatchingController() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyMethodNameDispatchingController.class));
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myHandle.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myOtherHandle.do");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myOtherView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myLangHandle.do");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myLangView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/mySurpriseHandle.do");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("mySurpriseView", response.getContentAsString());
	}

	public void testMethodNameDispatchingControllerWithSuffix() throws Exception {
		@SuppressWarnings("serial")
		DispatcherServlet servlet = new DispatcherServlet() {
			protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
				GenericWebApplicationContext wac = new GenericWebApplicationContext();
				wac.registerBeanDefinition("controller", new RootBeanDefinition(MyMethodNameDispatchingController.class));
				InternalPathMethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();
				methodNameResolver.setSuffix("Handle");
				RootBeanDefinition adapterDef = new RootBeanDefinition(AnnotationMethodHandlerAdapter.class);
				adapterDef.getPropertyValues().addPropertyValue("methodNameResolver", methodNameResolver);
				wac.registerBeanDefinition("handlerAdapter", adapterDef);
				wac.refresh();
				return wac;
			}
		};
		servlet.init(new MockServletConfig());

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/my.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myOther.do");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myOtherView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/myLang.do");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("myLangView", response.getContentAsString());

		request = new MockHttpServletRequest("GET", "/mySurprise.do");
		response = new MockHttpServletResponse();
		servlet.service(request, response);
		assertEquals("mySurpriseView", response.getContentAsString());
	}


	@RequestMapping("/myPath.do")
	private static class MyController extends AbstractController {

		protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.getWriter().write("test");
			return null;
		}
	}


	@Controller
	private static class MyAdaptedController {

		@RequestMapping("/myPath1.do")
		public void myHandle(HttpServletRequest request, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test");
		}

		@RequestMapping("/myPath2.do")
		public void myHandle(@RequestParam("param1") String p1, @RequestParam("param2") int p2, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test-" + p1 + "-" + p2);
		}

		@RequestMapping("/myPath3.do")
		public void myHandle(TestBean tb, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + tb.getAge());
		}

		@RequestMapping("/myPath4.do")
		public void myHandle(TestBean tb, Errors errors, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + errors.getFieldError("age").getCode());
		}
	}


	@Controller
	@RequestMapping("/*.do")
	private static class MyAdaptedController2 {

		@RequestMapping
		public void myHandle(HttpServletRequest request, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test");
		}

		@RequestMapping("/myPath2.do")
		public void myHandle(@RequestParam("param1") String p1, int param2, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test-" + p1 + "-" + param2);
		}

		@RequestMapping("/myPath3.do")
		public void myHandle(TestBean tb, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + tb.getAge());
		}

		@RequestMapping("/myPath4.*")
		public void myHandle(TestBean tb, Errors errors, HttpServletResponse response) throws IOException  {
			response.getWriter().write("test-" + tb.getName() + "-" + errors.getFieldError("age").getCode());
		}
	}


	@Controller
	public static class MyFormController {

		@ModelAttribute("testBeans")
		public List<TestBean> getTestBeans() {
			List<TestBean> list = new LinkedList<TestBean>();
			list.add(new TestBean("tb1"));
			list.add(new TestBean("tb2"));
			return list;
		}

		@RequestMapping("/myPath.do")
		public String myHandle(@ModelAttribute("myCommand") TestBean tb, BindingResult errors, ModelMap model) {
			if (!model.containsKey("myKey")) {
				model.addAttribute("myKey", "myValue");
			}
			return "myView";
		}
	}


	@Controller
	private static class MyCommandProvidingFormController extends MyFormController {

		@SuppressWarnings("unused")
		@ModelAttribute("myCommand")
		private TestBean createTestBean(@RequestParam String defaultName, Map<String, Object> model) {
			model.put("myKey", "myOriginalValue");
			return new TestBean(defaultName);
		}
	}


	@Controller
	private static class MyBinderInitializingCommandProvidingFormController extends MyCommandProvidingFormController {

		@SuppressWarnings("unused")
		@InitBinder
		private void initBinder(WebDataBinder binder) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setLenient(false);
			binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
		}
	}


	@Controller
	private static class MySpecificBinderInitializingCommandProvidingFormController extends MyCommandProvidingFormController {

		@SuppressWarnings("unused")
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
	@RequestMapping("/myPath.do")
	private static class MyParameterDispatchingController {

		@RequestMapping
		public void myHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("myView");
		}

		@RequestMapping(params = "view=other")
		public void myOtherHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("myOtherView");
		}

		@RequestMapping(method = RequestMethod.GET, params = {"view=my", "lang=de"})
		public void myLangHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("myLangView");
		}

		@RequestMapping(method = {RequestMethod.POST, RequestMethod.GET}, params = "surprise")
		public void mySurpriseHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("mySurpriseView");
		}
	}


	@Controller
	@RequestMapping("/*.do")
	private static class MyMethodNameDispatchingController {

		@RequestMapping
		public void myHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("myView");
		}

		@RequestMapping
		public void myOtherHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("myOtherView");
		}

		@RequestMapping
		public void myLangHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("myLangView");
		}

		@RequestMapping
		public void mySurpriseHandle(HttpServletResponse response) throws IOException {
			response.getWriter().write("mySurpriseView");
		}
	}


	private static class TestViewResolver implements ViewResolver {

		public View resolveViewName(final String viewName, Locale locale) throws Exception {
			return new View() {
				public String getContentType() {
					return null;
				}
				@SuppressWarnings({ "unchecked", "deprecation" })
				public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			};
		}
	}

	@Controller
	private static class EmptyParameterListHandlerMethodController {

		static boolean called;

		@RequestMapping("/emptyParameterListHandler")
		public void emptyParameterListHandler() {
			EmptyParameterListHandlerMethodController.called = true;
		}

		@RequestMapping("/nonEmptyParameterListHandler")
		public void nonEmptyParameterListHandler(HttpServletResponse response) {
		}
	}

}
