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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.web.mock.MockServletContext;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class HtmlEscapeTestSuite extends AbstractTagTest {

	public void testHtmlEscapeTag() throws JspException {
		PageContext pc = createPageContext();
		HtmlEscapeTag tag = new HtmlEscapeTag();
		tag.setPageContext(pc);
		RequestContextAwareTag testTag = new RequestContextAwareTag() {
			public int doStartTagInternal() throws Exception {
				return EVAL_BODY_INCLUDE;
			}
		};
		testTag.setPageContext(pc);

		assertTrue("Correct default", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", !testTag.isHtmlEscape());
		tag.setDefaultHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correctly enabled", HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", testTag.isHtmlEscape());
		tag.setDefaultHtmlEscape("false");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correctly disabled", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", !testTag.isHtmlEscape());

		tag.setDefaultHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		testTag.setHtmlEscape("true");
		assertTrue("Correctly enabled", HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", testTag.isHtmlEscape());
		testTag.setHtmlEscape("false");
		assertTrue("Correctly enabled", HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", !testTag.isHtmlEscape());
		tag.setDefaultHtmlEscape("false");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		testTag.setHtmlEscape("true");
		assertTrue("Correctly disabled", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", testTag.isHtmlEscape());
		testTag.setHtmlEscape("false");
		assertTrue("Correctly disabled", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
		assertTrue("Correctly applied", !testTag.isHtmlEscape());
	}

	public void testHtmlEscapeTagWithContextParamTrue() throws JspException {
		PageContext pc = createPageContext();
		MockServletContext sc = (MockServletContext) pc.getServletContext();
		HtmlEscapeTag tag = new HtmlEscapeTag();
		tag.setPageContext(pc);

		sc.addInitParameter(HtmlEscapeTag.HTML_ESCAPE_CONTEXT_PARAM, "true");
		assertTrue("Correct default", HtmlEscapeTag.isDefaultHtmlEscape(pc));
		tag.setDefaultHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correctly enabled", HtmlEscapeTag.isDefaultHtmlEscape(pc));
		tag.setDefaultHtmlEscape("false");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correctly disabled", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
	}

	public void testHtmlEscapeTagWithContextParamFalse() throws JspException {
		PageContext pc = createPageContext();
		MockServletContext sc = (MockServletContext) pc.getServletContext();
		HtmlEscapeTag tag = new HtmlEscapeTag();
		tag.setPageContext(pc);

		sc.addInitParameter(HtmlEscapeTag.HTML_ESCAPE_CONTEXT_PARAM, "false");
		assertTrue("Correct default", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
		tag.setDefaultHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correctly enabled", HtmlEscapeTag.isDefaultHtmlEscape(pc));
		tag.setDefaultHtmlEscape("false");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correctly disabled", !HtmlEscapeTag.isDefaultHtmlEscape(pc));
	}

}
