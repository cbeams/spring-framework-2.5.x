package org.springframework.web.servlet.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;


import com.mockobjects.servlet.MockPageContext;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class ThemeTestSuite extends AbstractTagTest {

	/**
	 * Constructor for HtmlEscapeTestSuite.
	 * @param arg0
	 */
	public ThemeTestSuite(String name) {
		super(name);
	}	

	public void testThemeTag() throws JspException {
		MockPageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		ThemeTag tag = new ThemeTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("themetest");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "theme test message".equals(message.toString()));
	}

}
