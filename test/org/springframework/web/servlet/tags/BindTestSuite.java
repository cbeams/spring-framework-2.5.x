package org.springframework.web.servlet.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.beans.TestBean;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.mockobjects.servlet.MockPageContext;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class BindTestSuite extends AbstractTagTest {

	public void testBindErrorsTagWithErrors() throws JspException {
		MockPageContext pc = createPageContext();
		ServletRequestDataBinder errors = new ServletRequestDataBinder(new TestBean(), "tb");
		errors.reject("test", null, "test");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindErrorsTag tag = new BindErrorsTag();
		tag.setPageContext(pc);
		tag.setName("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Has errors variable", pc.getAttribute(BindErrorsTag.ERRORS_VARIABLE_NAME) == errors);
	}

	public void testBindErrorsTagWithoutErrors() throws JspException {
		MockPageContext pc = createPageContext();
		ServletRequestDataBinder errors = new ServletRequestDataBinder(new TestBean(), "tb");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindErrorsTag tag = new BindErrorsTag();
		tag.setPageContext(pc);
		tag.setName("tb");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.SKIP_BODY);
		assertTrue("Doesn't have errors variable", pc.getAttribute(BindErrorsTag.ERRORS_VARIABLE_NAME) == null);
	}

	public void testBindErrorsTagWithoutBean() throws JspException {
		MockPageContext pc = createPageContext();
		BindErrorsTag tag = new BindErrorsTag();
		tag.setPageContext(pc);
		tag.setName("tb");
		try {
			tag.doStartTag();
			fail("Should have thrown JspException");
		}
		catch (JspException ex) {
			// expected
		}
	}

	public void testBindTagWithoutErrors() throws JspException {
		MockPageContext pc = createPageContext();
		ServletRequestDataBinder errors = new ServletRequestDataBinder(new TestBean(), "tb");
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
		MockPageContext pc = createPageContext();
		ServletRequestDataBinder errors = new ServletRequestDataBinder(new TestBean(), "tb");
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
	}
	
	public void testBindStatusGetErrorMessagesAsString()
	throws JspException {
		// one error (should not include delimiter)
		MockPageContext pc = createPageContext();
		ServletRequestDataBinder errors = new ServletRequestDataBinder(new TestBean(), "tb");
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
		errors = new ServletRequestDataBinder(new TestBean(), "tb");
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
		errors = new ServletRequestDataBinder(new TestBean(), "tb");
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
		MockPageContext pc = createPageContext();
		TestBean tb = new TestBean();
		tb.setName("name1");
		ServletRequestDataBinder errors = new ServletRequestDataBinder(tb, "tb");
		errors.rejectValue("name", "code1", "message & 1");
		errors.rejectValue("name", "code2", "message2");
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
		assertTrue("Correct errorMessage", "message &amp; 1".equals(status.getErrorMessage()));
		assertTrue("Correct errorMessagesAsString", "message &amp; 1 - message2".equals(status.getErrorMessagesAsString(" - ")));
	}

	public void testBindTagWithNestedFieldErrors() throws JspException {
		MockPageContext pc = createPageContext();
		TestBean tb = new TestBean();
		tb.setName("name1");
		TestBean spouse = new TestBean();
		spouse.setName("name2");
		tb.setSpouse(spouse);
		ServletRequestDataBinder errors = new ServletRequestDataBinder(tb, "tb");
		errors.rejectValue("spouse.name", "code1", "message1");
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);
		BindTag tag = new BindTag();
		tag.setPageContext(pc);
		tag.setPath("tb.spouse.name");
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
		MockPageContext pc = createPageContext();
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


	public void testBindTagWithoutBean() throws JspException {
		MockPageContext pc = createPageContext();
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
