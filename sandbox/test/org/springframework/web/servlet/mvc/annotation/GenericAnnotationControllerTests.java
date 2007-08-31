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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Mark Fisher
 */
public class GenericAnnotationControllerTests extends TestCase {

	public void testSetupAccountForm() throws Exception {
		GenericAnnotationController gac = new AccountController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setMethod("GET");
		request.setContextPath("new");
		ModelAndView mav = gac.handleRequest(request, response);
		assertEquals("new", mav.getViewName());
		Account account = (Account) mav.getModel().get("account");
		assertNotNull(account);
		assertEquals("123", account.getId());
	}

	public void testPostAccountForm() throws Exception {
		GenericAnnotationController gac = new AccountController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setMethod("POST");
		request.setContextPath("new");
		request.setParameter("id", "12345");
		ModelAndView mav = gac.handleRequest(request, response);
		assertEquals("new", mav.getViewName());
		String confirmation = (String) mav.getModel().get("confirmation");
		assertNotNull(confirmation);
		assertEquals("created:12345", confirmation);
	}

	public void testValidation() throws Exception {
		GenericAnnotationController gac = new AccountController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setMethod("POST");
		request.setContextPath("new");
		request.setParameter("id", "12");
		ModelAndView mav = gac.handleRequest(request, response);
		Errors errors = (Errors) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + "target");
		assertEquals("account.id.minlength", errors.getGlobalError().getCode());
	}


	private static class AccountController extends GenericAnnotationController {

		@Get("new")
		public Account setupAccountForm(Account account) {
			account.setId("123");
			return account;
		}

		@Post("new")
		public Map<String, String> createAccount(Account account) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("confirmation", "created:" + account.getId());
			return map;
		}

		@Validate
		public void validateAccount(Account account, Errors errors) {
			if (account.getId().length() < 3) {
				errors.reject("account.id.minlength");
			}
		}
	}


	private static class Account {

		private String id;

		public void setId(String id) {
			this.id = id;
		}

		public String getId() {
			return this.id;
		}
	}

}
