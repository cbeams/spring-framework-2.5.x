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

package org.springframework.web.servlet.tags;

import java.beans.PropertyEditorSupport;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.support.BindStatus;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class BindTestSuite extends AbstractTagTests {

	public void testBindErrorsTagWithErrors() throws JspException {
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		errors.reject("test", null, "test");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindErrorsTag tag = new BindErrorsTag();
		tag.setPageContext(pc);
		tag.setName("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Has errors variable", pc.getAttribute(BindErrorsTag.ERRORS_VARIABLE_NAME) == errors);
	}

	public void testBindErrorsTagWithoutErrors() throws JspException {
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindErrorsTag tag = new BindErrorsTag();
		tag.setPageContext(pc);
		tag.setName("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.SKIP_BODY);
		assertTrue("Doesn't have errors variable", pc.getAttribute(BindErrorsTag.ERRORS_VARIABLE_NAME) == null);
	}

	public void testBindErrorsTagWithoutBean() throws JspException {
		PageContext pc = createPageContext();
		BindErrorsTag tag = new BindErrorsTag();
		tag.setPageContext(pc);
		tag.setName("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.SKIP_BODY);
	}

	public void testBindTagWithoutErrors() throws JspException {
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", status.getExpression() == null);
		assertTrue("Correct value", status.getValue() == null);
		assertTrue("Correct displayValue", "".equals(status.getDisplayValue()));
		assertTrue("Correct isError", !status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 0);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 0);
		assertTrue("Correct errorCode", "".equals(status.getErrorCode()));
		assertTrue("Correct errorMessage", "".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessagesAsString", "".equals(status.getErrorMessagesAsString(",")));
	}

	public void testBindTagWithGlobalErrors() throws JspException {
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		errors.reject("code1", null, "message1");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", status.getExpression() == null);
		assertTrue("Correct value", status.getValue() == null);
		assertTrue("Correct displayValue", "".equals(status.getDisplayValue()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 1);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 1);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCode()));
		assertTrue("Correct errorMessage", "message1".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessagesAsString", "message1".equals(status.getErrorMessagesAsString(",")));

		tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.*");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "*".equals(status.getExpression()));
		assertTrue("Correct value", status.getValue() == null);
		assertTrue("Correct displayValue", "".equals(status.getDisplayValue()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 1);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 1);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCode()));
		assertTrue("Correct errorMessage", "message1".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessagesAsString", "message1".equals(status.getErrorMessagesAsString(",")));
	}

	public void testBindStatusGetErrorMessagesAsString() throws JspException {
		// one error (should not include delimiter)
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		errors.reject("code1", null, "message1");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		tag.doStartTag();
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertEquals("Error messages String should be 'message1'",
								 status.getErrorMessagesAsString(","), "message1");

		// two errors
		pc = createPageContext();
		errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		errors.reject("code1", null, "message1");
		errors.reject("code1", null, "message2");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		tag.doStartTag();
		status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertEquals("Error messages String should be 'message1,message2'",
								 status.getErrorMessagesAsString(","), "message1,message2");

		// no errors
		pc = createPageContext();
		errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		tag.doStartTag();
		status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertEquals("Error messages String should be ''",
								 status.getErrorMessagesAsString(","), "");
	}

	public void testBindTagWithFieldErrors() throws JspException {
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		tb.setName("name1");
		BindException errors = new ServletRequestDataBinder(tb, "tb").getErrors();
		errors.rejectValue("name", "code1", "message & 1");
		errors.rejectValue("name", "code2", "message2");
		errors.rejectValue("age", "code2", "message2");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.name");
		tag.setHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "name".equals(status.getExpression()));
		assertTrue("Correct value", "name1".equals(status.getValue()));
		assertTrue("Correct displayValue", "name1".equals(status.getDisplayValue()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 2);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 2);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCode()));
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCodes()[0]));
		assertTrue("Correct errorCode", "code2".equals(status.getErrorCodes()[1]));
		assertTrue("Correct errorMessage", "message &#38; 1".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessage", "message &#38; 1".equals(status.getErrorMessages()[0]));
		assertTrue("Correct errorMessage", "message2".equals(status.getErrorMessages()[1]));
		assertTrue("Correct errorMessagesAsString", "message &#38; 1 - message2".equals(status.getErrorMessagesAsString(" - ")));

		tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.age");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "age".equals(status.getExpression()));
		assertTrue("Correct value", new Integer(0).equals(status.getValue()));
		assertTrue("Correct displayValue", "0".equals(status.getDisplayValue()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 1);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 1);
		assertTrue("Correct errorCode", "code2".equals(status.getErrorCode()));
		assertTrue("Correct errorMessage", "message2".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessagesAsString", "message2".equals(status.getErrorMessagesAsString(" - ")));

		tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.*");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "*".equals(status.getExpression()));
		assertTrue("Correct value", status.getValue() == null);
		assertTrue("Correct displayValue", "".equals(status.getDisplayValue()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 3);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 3);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCode()));
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCodes()[0]));
		assertTrue("Correct errorCode", "code2".equals(status.getErrorCodes()[1]));
		assertTrue("Correct errorCode", "code2".equals(status.getErrorCodes()[2]));
		assertTrue("Correct errorMessage", "message & 1".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessage", "message & 1".equals(status.getErrorMessages()[0]));
		assertTrue("Correct errorMessage", "message2".equals(status.getErrorMessages()[1]));
		assertTrue("Correct errorMessage", "message2".equals(status.getErrorMessages()[2]));
	}

	public void testBindTagWithNestedFieldErrors() throws JspException {
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		tb.setName("name1");
		TestBean spouse = new TestBean();
		spouse.setName("name2");
		tb.setSpouse(spouse);
		BindException errors = new ServletRequestDataBinder(tb, "tb").getErrors();
		errors.rejectValue("spouse.name", "code1", "message1");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		pc.setAttribute("myattr", "tb.spouse.name");
		tag.setPath("${myattr}");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "spouse.name".equals(status.getExpression()));
		assertTrue("Correct value", "name2".equals(status.getValue()));
		assertTrue("Correct displayValue", "name2".equals(status.getDisplayValue()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 1);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 1);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCode()));
		assertTrue("Correct errorMessage", "message1".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessagesAsString", "message1".equals(status.getErrorMessagesAsString(" - ")));
	}

	public void testPropertyExposing() throws JspException {
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		tb.setName("name1");
		BindException errors = new BindException(tb, "tb");
		errors.rejectValue("name", "code1", null, "message & 1");
		errors.rejectValue("name", "code2", null, "message2");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		// test global property (should be null)
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertNull(tag.getProperty());

		// test property set (tb.name)
		tag.release();
		tag.setPageContext(pc);
		tag.setPath("tb.name");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("name", tag.getProperty());
	}

	public void testBindTagWithIndexedProperties() throws JspException {
		PageContext pc = createPageContext();
		IndexedTestBean tb = new IndexedTestBean();
		BindException errors = new ServletRequestDataBinder(tb, "tb").getErrors();
		errors.rejectValue("array[0]", "code1", "message1");
		errors.rejectValue("array[0]", "code2", "message2");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.array[0]");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "array[0]".equals(status.getExpression()));
		assertTrue("Value is TestBean", status.getValue() instanceof TestBean);
		assertTrue("Correct value", "name0".equals(((TestBean) status.getValue()).getName()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 2);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 2);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCodes()[0]));
		assertTrue("Correct errorCode", "code2".equals(status.getErrorCodes()[1]));
		assertTrue("Correct errorMessage", "message1".equals(status.getErrorMessages()[0]));
		assertTrue("Correct errorMessage", "message2".equals(status.getErrorMessages()[1]));
	}

	public void testBindTagWithMappedProperties() throws JspException {
		PageContext pc = createPageContext();
		IndexedTestBean tb = new IndexedTestBean();
		BindException errors = new ServletRequestDataBinder(tb, "tb").getErrors();
		errors.rejectValue("map[key1]", "code1", "message1");
		errors.rejectValue("map[key1]", "code2", "message2");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.map[key1]");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "map[key1]".equals(status.getExpression()));
		assertTrue("Value is TestBean", status.getValue() instanceof TestBean);
		assertTrue("Correct value", "name4".equals(((TestBean) status.getValue()).getName()));
		assertTrue("Correct isError", status.isError());
		assertTrue("Correct errorCodes", status.getErrorCodes().length == 2);
		assertTrue("Correct errorMessages", status.getErrorMessages().length == 2);
		assertTrue("Correct errorCode", "code1".equals(status.getErrorCodes()[0]));
		assertTrue("Correct errorCode", "code2".equals(status.getErrorCodes()[1]));
		assertTrue("Correct errorMessage", "message1".equals(status.getErrorMessages()[0]));
		assertTrue("Correct errorMessage", "message2".equals(status.getErrorMessages()[1]));
	}

	public void testBindTagWithIndexedPropertiesAndCustomEditor() throws JspException {
		PageContext pc = createPageContext();
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new ServletRequestDataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, null, new PropertyEditorSupport() {
			public String getAsText() {
				return "something";
			}
		});
		BindException errors = binder.getErrors();
		errors.rejectValue("array[0]", "code1", "message1");
		errors.rejectValue("array[0]", "code2", "message2");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.array[0]");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertTrue("Correct expression", "array[0]".equals(status.getExpression()));
		// because of the custom editor getValue() should return a String
		assertTrue("Value is TestBean", status.getValue() instanceof String);
		assertTrue("Correct value", "something".equals(status.getValue()));
	}

	public void testBindTagWithFieldButWithoutErrorsInstance() throws JspException {
		PageContext pc = createPageContext();
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.name");
		pc.getRequest().setAttribute("tb", new TestBean("juergen", 99));
		tag.doStartTag();
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertEquals("name", status.getExpression());
		assertEquals("juergen", status.getValue());
	}

	public void testBindTagButWithoutErrorsInstance() throws JspException {
		PageContext pc = createPageContext();
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		pc.getRequest().setAttribute("tb", new TestBean("juergen", 99));
		tag.doStartTag();
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertNull(status.getExpression());
		assertNull(status.getValue());
	}

	public void testBindTagWithoutBean() throws JspException {
		PageContext pc = createPageContext();
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb");
		try {
			tag.doStartTag();
			fail("Should have thrown JspException");
		}
		catch (JspException ex) {
			// expected
		}
	}

}
