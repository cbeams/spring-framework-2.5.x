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
import org.springframework.mock.web.MockPageContext;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.BindingResult;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyEditor;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class OptionsTagTests extends AbstractHtmlElementTagTests {

	private static final String COMMAND_NAME = "testBean";

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

	public void testWithCollectionAndCustomEditor() throws Exception {
		PropertyEditor propertyEditor = new SimpleFloatEditor();

		TestBean target = new TestBean();
		target.setMyFloat(new Float("12.34"));

		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(target, COMMAND_NAME);
		errors.getPropertyAccessor().registerCustomEditor(Float.class, propertyEditor);
		exposeBindingResult(errors);

		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.myFloat", false));

		this.tag.setItems("${floats}");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 6, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[text() = '12.34f']");
		assertNotNull("Option node should not be null", e);
		assertEquals("12.34 node not selected", "selected", e.attribute("selected").getValue());

		e = (Element) rootElement.selectSingleNode("option[text() = '12.35f']");
		assertNotNull("Option node should not be null", e);
		assertNull("12.35 node incorrectly selected", e.attribute("selected"));
	}

	protected void extendRequest(MockHttpServletRequest request) {
		TestBean bean = new TestBean();
		bean.setName("foo");
		bean.setCountry("UK");
		bean.setMyFloat(new Float("12.34"));
		request.setAttribute(COMMAND_NAME, bean);
		request.setAttribute("countries", Country.getCountries());

		List floats = new ArrayList();
		floats.add(new Float("12.30"));
		floats.add(new Float("12.31"));
		floats.add(new Float("12.32"));
		floats.add(new Float("12.33"));
		floats.add(new Float("12.34"));
		floats.add(new Float("12.35"));

		request.setAttribute("floats", floats);
	}

	protected void exposeBindingResult(Errors errors) {
		// wrap errors in a Model
		Map model = new HashMap();
		model.put(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, errors);

		// replace the request context with one containing the errors
		MockPageContext pageContext = getPageContext();
		RequestContext context = new RequestContext((HttpServletRequest) pageContext.getRequest(), model);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, context);
	}

}
