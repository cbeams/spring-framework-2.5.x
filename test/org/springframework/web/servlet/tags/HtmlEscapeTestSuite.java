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
