package org.springframework.web.servlet.tags;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;

import com.mockobjects.servlet.MockPageContext;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class MessageTestSuite extends AbstractTagTest {

	public void testMessageTagWithCode1() throws JspException {
		MockPageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("test");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "test message".equals(message.toString()));
	}

	public void testMessageTagWithCode2() throws JspException {
		MockPageContext pc = createPageContext();
		MockHttpServletRequest request = (MockHttpServletRequest) pc.getRequest();
		request.addPreferredLocale(Locale.CANADA);
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "Canadian &amp; test message".equals(message.toString()));
	}

	public void testMessageTagWithCodeAndText1() throws JspException {
		MockPageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setText("testtext");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "test message".equals(message.toString()));
	}

	public void testMessageTagWithCodeAndText2() throws JspException {
		MockPageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("test2");
		tag.setText("test & text");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "test & text".equals(message.toString()));
	}

	public void testMessageTagWithText() throws JspException {
		MockPageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		MessageTag tag = new MessageTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setText("test & text");
		tag.setHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "test &amp; text".equals(message.toString()));
	}
	
	public void testMessageWithVarAndScope() throws JspException {
		MockPageContext pc = createPageContext();		
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setText("text & text");
		tag.setVar("testvar");		
		tag.setScope("page");
		tag.doStartTag();
		assertNotNull(pc.getAttribute("testvar"));
		assertEquals(pc.getAttribute("testvar"), "text & text");
		tag.release();
		
		tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar2");
		tag.doStartTag();
		assertNotNull(pc.getAttribute("testvar2"));
		assertEquals(pc.getAttribute("testvar2"), "test message");
		tag.release();
	}

	public void testMessageWithVar() throws JspException {
		MockPageContext pc = createPageContext();		
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setText("text & text");
		tag.setVar("testvar");		
		tag.doStartTag();
		assertNotNull(pc.getAttribute("testvar"));
		assertEquals(pc.getAttribute("testvar"), "text & text");
		tag.release();

		// try to reuse
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar");
			
		tag.doStartTag();
		assertNotNull(pc.getAttribute("testvar"));
		assertEquals(pc.getAttribute("testvar"), "test message");

	}
	
	public void testNullMessageSource() throws JspException {
		MockPageContext pc = createPageContext();
		WebApplicationContext ctx = (WebApplicationContext)pc.getRequest().getAttribute(
			DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		ctx.close();
		
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar2");
		tag.doStartTag();
		
		
	}

}
