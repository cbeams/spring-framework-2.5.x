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

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.mockobjects.servlet.MockPageContext;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;

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
		pc.setAttribute("myattr", "test");
		tag.setCode("${myattr}");
		tag.setHtmlEscape("true");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertTrue("Correct message", "Canadian &#38; test message".equals(message.toString()));
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
		pc.setAttribute("myattr", "test & text");
		tag.setCode("test2");
		tag.setText("${myattr}");
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
		assertTrue("Correct message", "test &#38; text".equals(message.toString()));
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
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext)pc.getRequest().getAttribute(
			DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		ctx.close();
		
		MessageTag tag = new MessageTag();
		tag.setPageContext(pc);
		tag.setCode("test");
		tag.setVar("testvar2");
		tag.doStartTag();
		
		
	}

}
