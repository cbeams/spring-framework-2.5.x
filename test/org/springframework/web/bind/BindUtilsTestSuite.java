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

package org.springframework.web.bind;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Juergen Hoeller
 * @since 23.05.2003
 */
public class BindUtilsTestSuite extends TestCase {

	public void testBind() throws ServletException {
		ServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		request.addParameter("name", " myname ");
		request.addParameter("age", "myage");
		request.addParameter("spouse.name", " spousename ");
		request.addParameter("spouse.age", "spouseage");

		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		assertTrue("Name not set", tb.getName() == null);
		assertTrue("Age not set", tb.getAge() == 0);
		assertTrue("Spouse name not set", tb.getSpouse().getName() == null);
		assertTrue("Spouse age not set", tb.getSpouse().getAge() == 0);
		Errors errors = BindUtils.bind(request, tb, "tb");

		assertTrue("Name set", " myname ".equals(tb.getName()));
		assertTrue("No name error", !errors.hasFieldErrors("name"));
		assertTrue("Age not set", tb.getAge() == 0);
		assertTrue("Has age error", errors.hasFieldErrors("age"));
		assertTrue("Correct age error", "typeMismatch".equals(errors.getFieldError("age").getCode()));
		assertTrue("Spouse name set", " spousename ".equals(tb.getSpouse().getName()));
		assertTrue("No spouse name error", !errors.hasFieldErrors("spouse.name"));
		assertTrue("Spouse age not set", tb.getSpouse().getAge() == 0);
		assertTrue("Has spouse age error", errors.hasFieldErrors("spouse.age"));
		assertTrue("Correct spouse age error", "typeMismatch".equals(errors.getFieldError("spouse.age").getCode()));
	}

	public void testBindAndValidate() throws ServletException {
		ServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		request.addParameter("name", " myname ");
		request.addParameter("age", "myage");

		TestBean tb = new TestBean();
		assertTrue("Name not set", tb.getName() == null);
		Errors errors = BindUtils.bindAndValidate(request, tb, "tb", new Validator() {
			public boolean supports(Class clazz) {
				return TestBean.class.isAssignableFrom(clazz);
			}
			public void validate(Object obj, Errors errors) {
				TestBean vtb = (TestBean) obj;
				if (" myname ".equals(vtb.getName())) {
					errors.rejectValue("name", "notMyname", null, "Name may not be myname");
				}
			}
		});

		assertTrue("Name set", " myname ".equals(tb.getName()));
		assertTrue("Has name error", errors.hasFieldErrors("name"));
		assertTrue("Correct name error", "notMyname".equals(errors.getFieldError("name").getCode()));
		assertTrue("Age not set", tb.getAge() == 0);
		assertTrue("Has age error", errors.hasFieldErrors("age"));
		assertTrue("Correct age error", "typeMismatch".equals(errors.getFieldError("age").getCode()));
	}

	public void testBindWithInitializer() throws ServletException {
		ServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		request.addParameter("name", " myname ");
		request.addParameter("age", "myage");

		TestBean tb = new TestBean();
		assertTrue("Name not set", tb.getName() == null);
		assertTrue("Age not set", tb.getAge() == 0);
		Errors errors = BindUtils.bind(request, tb, "tb", new BindInitializer() {
			public void initBinder(ServletRequest request, ServletRequestDataBinder binder) {
				binder.registerCustomEditor(String.class, "name", new StringTrimmerEditor(true));
			}
		});

		assertTrue("Name set", "myname".equals(tb.getName()));
		assertTrue("No name error", !errors.hasFieldErrors("name"));
		assertTrue("Age not set", tb.getAge() == 0);
		assertTrue("Has age error", errors.hasFieldErrors("age"));
		assertTrue("Correct age error", "typeMismatch".equals(errors.getFieldError("age").getCode()));
	}

	public void testBindAndValidateWithInitializer() throws ServletException {
		ServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		request.addParameter("name", " myname ");
		request.addParameter("age", "myage");

		TestBean tb = new TestBean();
		assertTrue("Name not set", tb.getName() == null);
		Errors errors = BindUtils.bindAndValidate(request, tb, "tb",
			new Validator() {
				public boolean supports(Class clazz) {
					return TestBean.class.isAssignableFrom(clazz);
				}
				public void validate(Object obj, Errors errors) {
					TestBean vtb = (TestBean) obj;
					if ("myname".equals(vtb.getName())) {
						errors.rejectValue("name", "notMyname", null, "Name may not be myname");
					}
				}
			},
			new BindInitializer() {
				public void initBinder(ServletRequest request, ServletRequestDataBinder binder) {
					binder.registerCustomEditor(String.class, "name", new StringTrimmerEditor(true));
				}
			}
		);

		assertTrue("Name set", "myname".equals(tb.getName()));
		assertTrue("Has name error", errors.hasFieldErrors("name"));
		assertTrue("Correct name error", "notMyname".equals(errors.getFieldError("name").getCode()));
		assertTrue("Age not set", tb.getAge() == 0);
		assertTrue("Has age error", errors.hasFieldErrors("age"));
		assertTrue("Correct age error", "typeMismatch".equals(errors.getFieldError("age").getCode()));
	}

	public void testBindWithRequiredArrayField() throws ServletException {
		ServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc);
		request.addParameter("stringArray", "alias_1,alias_2");

		TestBean tb = new TestBean();
		BindUtils.bind(request, tb, "tb", new BindInitializer() {
			public void initBinder(ServletRequest request, ServletRequestDataBinder binder) {
				binder.setRequiredFields(new String[] {"stringArray[0]"});
			}
		});

		assertTrue("stringArray length", tb.getStringArray().length == 2);
	}

}
