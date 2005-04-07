/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.samples.phonebook.web.flow.action;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.samples.phonebook.domain.Person;
import org.springframework.samples.phonebook.domain.PhoneBook;
import org.springframework.test.JUnitAssertSupport;
import org.springframework.web.flow.Event;

public class GetPersonActionTests extends TestCase {

	/**
	 * JUnit support assertion facility
	 */
	private JUnitAssertSupport asserts = new JUnitAssertSupport();

	/**
	 * Returns a support class for doing additional JUnit assertion operations
	 * not supported out-of-the-box by JUnit 3.8.1.
	 * @return The junit assert support.
	 */
	protected JUnitAssertSupport asserts() {
		return asserts;
	}

	public void testGetPerson() throws Exception {
		MockControl control = MockControl.createControl(PhoneBook.class);
		PhoneBook phoneBook = (PhoneBook)control.getMock();
		phoneBook.getPerson(new Long(1));
		control.setReturnValue(new Person(), 1);
		control.replay();

		GetPersonAction action = new GetPersonAction();
		action.setPhoneBook(phoneBook);
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("id", new Long(1));
		Event result = action.execute(context);
		assertEquals("success", result.getId());
		asserts().assertAttributePresent(context.getRequestScope(), "person");
		control.verify();
	}

	public void testGetPersonDoesNotExist() throws Exception {
		MockControl control = MockControl.createControl(PhoneBook.class);
		PhoneBook phoneBook = (PhoneBook)control.getMock();
		phoneBook.getPerson(new Long(2));
		control.setReturnValue(null, 1);
		control.replay();

		GetPersonAction action = new GetPersonAction();
		action.setPhoneBook(phoneBook);
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("id", new Long(2));
		Event result = action.execute(context);
		assertEquals("error", result.getId());
		asserts().assertAttributeNotPresent(context.getRequestScope(), "person");
		control.verify();
	}
}