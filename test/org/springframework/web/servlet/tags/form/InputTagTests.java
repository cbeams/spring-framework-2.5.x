/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.servlet.tags.form;

import org.springframework.beans.TestBean;

import javax.servlet.jsp.tagext.Tag;
import java.io.StringWriter;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class InputTagTests extends AbstractFormTagTests {

	private InputTag tag;

	protected void onSetUp() {
		// set up tag instance
		this.tag = createTag(getWriter());
		this.tag.setPageContext(getPageContext());
	}

	public void testSimpleBind() throws Exception {
		this.tag.setPath("name");

		assertEquals(Tag.EVAL_PAGE, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());
		assertContainsAttribute(output, "value", "Rob");
	}

	public void testComplexBind() throws Exception {
		this.tag.setPath("spouse.name");

		assertEquals(Tag.EVAL_PAGE, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());
		assertContainsAttribute(output, "value", "Sally");
	}

	public void testWithAllAttributes() throws Exception {
		String title = "aTitle";
		String id = "123";
		String cssClass = "textfield";
		String cssStyle = "width:10px";
		String lang = "en";
		String dir = "ltr";
		String tabindex = "2";
		String onclick = "doClick()";
		String ondblclick = "doDblclick()";
		String onkeydown = "doKeydown()";
		String onkeypress = "doKeypress()";
		String onkeyup = "doKeyup()";
		String onmousedown = "doMouseDown()";
		String onmousemove = "doMouseMove()";
		String onmouseout = "doMouseOut()";
		String onmouseover = "doMouseOver()";
		String onmouseup = "doMouseUp()";
		String onfocus = "doFocus()";
		String onblur = "doBlur()";
		String onchange = "doChange()";
		String accesskey = "a";
		String maxlength = "12";
		String alt = "Some text";
		String onselect = "doSelect()";

		this.tag.setId(id);
		this.tag.setPath("name");
		this.tag.setCssClass(cssClass);
		this.tag.setCssStyle(cssStyle);
		this.tag.setTitle(title);
		this.tag.setLang(lang);
		this.tag.setDir(dir);
		this.tag.setTabindex(tabindex);
		this.tag.setOnclick(onclick);
		this.tag.setOndblclick(ondblclick);
		this.tag.setOnkeydown(onkeydown);
		this.tag.setOnkeypress(onkeypress);
		this.tag.setOnkeyup(onkeyup);
		this.tag.setOnmousedown(onmousedown);
		this.tag.setOnmousemove(onmousemove);
		this.tag.setOnmouseout(onmouseout);
		this.tag.setOnmouseover(onmouseover);
		this.tag.setOnmouseup(onmouseup);
		this.tag.setOnfocus(onfocus);
		this.tag.setOnblur(onblur);
		this.tag.setOnchange(onchange);
		this.tag.setAccesskey(accesskey);
		this.tag.setMaxlength(maxlength);
		this.tag.setAlt(alt);
		this.tag.setOnselect(onselect);

		assertEquals(Tag.EVAL_PAGE, this.tag.doStartTag());
		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());

		assertContainsAttribute(output, "id", id);
		assertContainsAttribute(output, "value", "Rob");
		assertContainsAttribute(output, "class", cssClass);
		assertContainsAttribute(output, "style", cssStyle);
		assertContainsAttribute(output, "title", title);
		assertContainsAttribute(output, "lang", lang);
		assertContainsAttribute(output, "dir", dir);
		assertContainsAttribute(output, "tabindex", tabindex);
		assertContainsAttribute(output, "onclick", onclick);
		assertContainsAttribute(output, "ondblclick", ondblclick);
		assertContainsAttribute(output, "onkeydown", onkeydown);
		assertContainsAttribute(output, "onkeypress", onkeypress);
		assertContainsAttribute(output, "onkeyup", onkeyup);
		assertContainsAttribute(output, "onmousedown", onmousedown);
		assertContainsAttribute(output, "onmousemove", onmousemove);
		assertContainsAttribute(output, "onmouseout", onmouseout);
		assertContainsAttribute(output, "onmouseover", onmouseover);
		assertContainsAttribute(output, "onmouseup", onmouseup);
		assertContainsAttribute(output, "onfocus", onfocus);
		assertContainsAttribute(output, "onblur", onblur);
		assertContainsAttribute(output, "onchange", onchange);
		assertContainsAttribute(output, "accesskey", accesskey);
		assertContainsAttribute(output, "maxlength", maxlength);
		assertContainsAttribute(output, "alt", alt);
		assertContainsAttribute(output, "onselect", onselect);
	}

	private void assertTagClosed(String output) {
		assertTrue("Tag not closed properly", output.endsWith("/>"));
	}

	private void assertTagOpened(String output) {
		assertTrue("Tag not opened properly", output.startsWith("<input "));
	}

	protected TestBean createTestBean() {
		// set up test data
		TestBean rob = new TestBean();
		rob.setName("Rob");

		TestBean sally = new TestBean();
		sally.setName("Sally");
		rob.setSpouse(sally);

		return rob;
	}

	protected InputTag createTag(final StringWriter writer) {
		return new InputTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(writer);
			}
		};
	}

	protected String getType() {
		return "text";
	}

}
