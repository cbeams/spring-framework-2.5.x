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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.springframework.beans.TestBean;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class TransformTestSuite extends AbstractTagTests {

	public void testTransformTagCorrectBehavior() throws JspException {
		// first set up the pagecontext and the bean
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(tb, "tb");
		CustomDateEditor l = new CustomDateEditor(df, true);
		binder.registerCustomEditor(Date.class, l);
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", binder.getErrors());

		// execute the bind tag using the date property
		BindTag bind = new BindTag();
		bind.setPageContext(pc);
		bind.setPath("tb.date");
		bind.doStartTag();

		// transform stuff
		TransformTag transform = new TransformTag();
		transform.setPageContext(pc);
		pc.setAttribute("date", tb.getDate());
		transform.setParent(bind);
		transform.setValue("${date}");
		transform.setVar("theDate");
		transform.doStartTag();

		assertNotNull(pc.getAttribute("theDate"));
		assertEquals(pc.getAttribute("theDate"), df.format(tb.getDate()));

		// try another time, this time using Strings
		bind = new BindTag();
		bind.setPageContext(pc);
		bind.setPath("tb.name");
		bind.doStartTag();

		transform = new TransformTag();
		transform.setPageContext(pc);
		pc.setAttribute("string", "name");
		transform.setValue("${string}");
		transform.setParent(bind);
		transform.setVar("theString");
		transform.doStartTag();

		assertNotNull(pc.getAttribute("theString"));
		assertEquals(pc.getAttribute("theString"), "name");
	}

	public void testTransformTagWithHtmlEscape() throws JspException {
		// first set up the pagecontext and the bean
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(tb, "tb");
		CustomDateEditor l = new CustomDateEditor(df, true);
		binder.registerCustomEditor(Date.class, l);
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", binder.getErrors());

		// try another time, this time using Strings
		BindTag bind = new BindTag();
		bind.setPageContext(pc);
		bind.setPath("tb.name");
		bind.doStartTag();

		TransformTag transform = new TransformTag();
		transform.setPageContext(pc);
		pc.setAttribute("string", "na<me");
		transform.setValue("${string}");
		transform.setParent(bind);
		transform.setVar("theString");
		transform.setHtmlEscape("true");
		transform.doStartTag();

		assertNotNull(pc.getAttribute("theString"));
		assertEquals(pc.getAttribute("theString"), "na&#60;me");
	}

	public void testTransformTagOutsideBindTag() throws JspException {
		// first set up the pagecontext and the bean
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(tb, "tb");
		CustomDateEditor l = new CustomDateEditor(df, true);
		binder.registerCustomEditor(Date.class, l);
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", binder.getErrors());

		// now try to execute the tag outside a bindtag
		TransformTag transform = new TransformTag();
		transform.setPageContext(pc);
		transform.setVar("var");
		transform.setValue("bla");
		try {
			transform.doStartTag();
			fail("Tag can be executed outside BindTag");
		}
		catch (JspException e) {
			// this is ok!
		}

		// now try to execute the tag outside a bindtag, but inside a messageTag
		MessageTag message = new MessageTag();
		message.setPageContext(pc);
		transform = new TransformTag();
		transform.setPageContext(pc);
		transform.setVar("var");
		transform.setValue("bla");
		transform.setParent(message);
		try {
			transform.doStartTag();
			fail("Tag can be executed outside BindTag and inside messagtag");
		}
		catch (JspException e) {
			// this is ok!
		}
	}

	public void testTransformTagNonExistingValue() throws JspException {
		// first set up the pagecontext and the bean
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(tb, "tb");
		CustomDateEditor l = new CustomDateEditor(df, true);
		binder.registerCustomEditor(Date.class, l);
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", binder.getErrors());

		// try with non-existing value
		BindTag bind = new BindTag();
		bind.setPageContext(pc);
		bind.setPath("tb.name");
		bind.doStartTag();

		TransformTag transform = new TransformTag();
		transform.setPageContext(pc);
		transform.setValue("${string2}");
		transform.setParent(bind);
		transform.setVar("theString2");
		transform.doStartTag();

		assertNull(pc.getAttribute("theString2"));
	}

	public void testSettingOfScope() throws JspException {
		// first set up the pagecontext and the bean
		PageContext pc = createPageContext();
		TestBean tb = new TestBean();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		ServletRequestDataBinder binder = new ServletRequestDataBinder(tb, "tb");
		CustomDateEditor l = new CustomDateEditor(df, true);
		binder.registerCustomEditor(Date.class, l);
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", binder.getErrors());

		// execute the bind tag using the date property
		BindTag bind = new BindTag();
		bind.setPageContext(pc);
		bind.setPath("tb.date");
		bind.doStartTag();

		// transform stuff
		TransformTag transform = new TransformTag();
		transform.setPageContext(pc);
		pc.setAttribute("date", tb.getDate());
		transform.setParent(bind);
		transform.setValue("${date}");
		transform.setVar("theDate");
		transform.setScope("page");
		transform.doStartTag();

		transform.release();

		assertNotNull(pc.getAttribute("theDate"));
		assertEquals(pc.getAttribute("theDate"), df.format(tb.getDate()));

		// try another time, this time using Strings
		bind = new BindTag();
		bind.setPageContext(pc);
		bind.setPath("tb.name");
		bind.doStartTag();

		transform = new TransformTag();
		transform.setPageContext(pc);
		pc.setAttribute("string", "name");
		pc.setAttribute("scopy", "page");
		transform.setValue("${string}");
		transform.setParent(bind);
		transform.setVar("theString");
		transform.setScope("${scopy}");
		transform.doStartTag();

		transform.release();

		assertNotNull(pc.getAttribute("theString"));
		assertEquals(pc.getAttribute("theString"), "name");
	}

}
