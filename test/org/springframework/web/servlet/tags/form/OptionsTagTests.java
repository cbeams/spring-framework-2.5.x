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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.support.BindStatus;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.jsp.tagext.Tag;
import java.io.StringReader;
import java.util.List;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class OptionsTagTests extends AbstractHtmlElementTagTests {

	private OptionsTag tag;

	protected void onSetUp() {
		this.tag = new OptionsTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setParent(new SelectTag());
		this.tag.setPageContext(getPageContext());
	}

	public void testWithCollection() throws Exception {
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.country", false));

		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "selected", e.attribute("selected").getValue());
	}
	                                                                                  

	protected void extendRequest(MockHttpServletRequest request) {
		TestBean bean = new TestBean();
		bean.setName("foo");
		bean.setCountry("UK");
		request.setAttribute("testBean", bean);
		request.setAttribute("countries", Country.getCountries());
	}
}
