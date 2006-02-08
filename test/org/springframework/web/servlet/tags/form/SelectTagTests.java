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
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.StringReader;

/**
 * @author Rob Harrop
 */
public class SelectTagTests extends AbstractFormTagTests {

	private SelectTag tag;

	protected void onSetUp() {
		this.tag = new SelectTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWithList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		this.tag.setItemKey("isoCode");
		this.tag.setItemValue("name");
		this.tag.setSize("5");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		validateOutput(output);
		assertContainsAttribute(output, "size", "5");
	}

	public void testWithListAndNoLabel() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		this.tag.setItemKey("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		validateOutput(getWriter().toString());
	}

	public void testWithMap() throws Exception {
		this.tag.setPath("sex");
		this.tag.setItems("${sexes}");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		System.out.println(getWriter());

	}

	public void testWithListAndNoValue() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		try {
			this.tag.doStartTag();
			fail("Should not be able to start tag with only 'items' specified");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	public void testWithInvalidList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${other}");
		this.tag.setItemKey("isoCode");
		try {
			this.tag.doStartTag();
			fail("Should not be able to use a non-Collection typed value as the value of 'items'.");
		}
		catch (JspException e) {
			String message = e.getMessage();
			assertTrue(message.indexOf("'items'") > -1);
			assertTrue(message.indexOf("'java.util.Collection'") > -1);
		}
	}

	public void testWithNestedOptions() throws Exception {
		this.tag.setPath("country");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);

		String value = (String) getPageContext().getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
		assertEquals("Selected country not exposed in page context", "UK", value);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		this.tag.doFinally();

		String output = getWriter().toString();
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));
		assertContainsAttribute(output, "name", "country");
	}

	private List getCountries() {
		List countries = new ArrayList();
		countries.add(new Country("AT", "Austria"));
		countries.add(new Country("NL", "Netherlands"));
		countries.add(new Country("UK", "United Kingdom"));
		countries.add(new Country("US", "United States"));
		return countries;
	}

	private Map getSexes() {
		Map sexes = new HashMap();
		sexes.put("F", "Female");
		sexes.put("M", "Male");
		return sexes;
	}

	protected void extendRequest(MockHttpServletRequest request) {
		super.extendRequest(request);
		request.setAttribute("countries", getCountries());
		request.setAttribute("sexes", getSexes());
		request.setAttribute("other", new TestBean());
	}

		private void validateOutput(String output) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("country", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "true", e.attribute("selected").getValue());
	}
	
	protected TestBean createTestBean() {
		TestBean bean = new TestBean();
		bean.setCountry("UK");
		bean.setSex("M");
		return bean;
	}

	public static class Country {

		private String isoCode;

		private String name;

		public Country(String isoCode, String name) {
			this.isoCode = isoCode;
			this.name = name;
		}

		public String getIsoCode() {
			return isoCode;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return this.name + "(" + this.isoCode + ")";
		}
	}
}
