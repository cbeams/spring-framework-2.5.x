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

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * @author Juergen Hoeller
 * @since 02.05.2003
 */
public class EscapedErrorsTestSuite extends TestCase {

	public void testEscapedErrors() {
		TestBean tb = new TestBean();
		tb.setName("empty &");

		Errors errors = new EscapedErrors(new BindException(tb, "tb"));
		errors.rejectValue("name", "NAME_EMPTY &", null, "message: &");
		errors.rejectValue("age", "AGE_NOT_SET <tag>", null, "message: <tag>");
		errors.rejectValue("age", "AGE_NOT_32 <tag>", null, "message: <tag>");
		errors.reject("GENERAL_ERROR \" '", null, "message: \" '");

		assertTrue("Correct errors flag", errors.hasErrors());
		assertTrue("Correct number of errors", errors.getErrorCount() == 4);
		assertTrue("Correct object name", "tb".equals(errors.getObjectName()));

		assertTrue("Correct global errors flag", errors.hasGlobalErrors());
		assertTrue("Correct number of global errors", errors.getGlobalErrorCount() == 1);
		ObjectError globalError = errors.getGlobalError();
		assertTrue("Global error message escaped", "message: &#34; '".equals(globalError.getDefaultMessage()));
		assertTrue("Global error code not escaped", "GENERAL_ERROR \" '".equals(globalError.getCode()));
		ObjectError globalErrorInList = (ObjectError) errors.getGlobalErrors().get(0);
		assertTrue("Same global error in list", globalError.getDefaultMessage().equals(globalErrorInList.getDefaultMessage()));
		ObjectError globalErrorInAllList = (ObjectError) errors.getAllErrors().get(3);
		assertTrue("Same global error in list", globalError.getDefaultMessage().equals(globalErrorInAllList.getDefaultMessage()));

		assertTrue("Correct name errors flag", errors.hasFieldErrors("name"));
		assertTrue("Correct number of name errors", errors.getFieldErrorCount("name") == 1);
		FieldError nameError = errors.getFieldError("name");
		assertTrue("Name error message escaped", "message: &#38;".equals(nameError.getDefaultMessage()));
		assertTrue("Name error code not escaped", "NAME_EMPTY &".equals(nameError.getCode()));
		assertTrue("Name value escaped", "empty &#38;".equals(errors.getFieldValue("name")));
		FieldError nameErrorInList = (FieldError) errors.getFieldErrors("name").get(0);
		assertTrue("Same name error in list", nameError.getDefaultMessage().equals(nameErrorInList.getDefaultMessage()));

		assertTrue("Correct age errors flag", errors.hasFieldErrors("age"));
		assertTrue("Correct number of age errors", errors.getFieldErrorCount("age") == 2);
		FieldError ageError = errors.getFieldError("age");
		assertTrue("Age error message escaped", "message: &#60;tag&#62;".equals(ageError.getDefaultMessage()));
		assertTrue("Age error code not escaped", "AGE_NOT_SET <tag>".equals(ageError.getCode()));
		assertTrue("Age value not escaped", (new Integer(0)).equals(errors.getFieldValue("age")));
		FieldError ageErrorInList = (FieldError) errors.getFieldErrors("age").get(0);
		assertTrue("Same name error in list", ageError.getDefaultMessage().equals(ageErrorInList.getDefaultMessage()));
		FieldError ageError2 = (FieldError) errors.getFieldErrors("age").get(1);
		assertTrue("Age error 2 message escaped", "message: &#60;tag&#62;".equals(ageError2.getDefaultMessage()));
		assertTrue("Age error 2 code not escaped", "AGE_NOT_32 <tag>".equals(ageError2.getCode()));
	}

}
