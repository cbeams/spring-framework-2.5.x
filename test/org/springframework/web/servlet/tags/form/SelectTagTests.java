/*
 * Copyright 2002-2007 the original author or authors.
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

import java.beans.PropertyEditor;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.springframework.beans.TestBean;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.support.BindStatus;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class SelectTagTests extends AbstractFormTagTests {

	private SelectTag tag;

	private TestBean bean;


	protected void onSetUp() {
		this.tag = new SelectTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}


	public void testEmptyItems() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems(Collections.EMPTY_LIST);

		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertEquals("<select id=\"country\" name=\"country\"></select>", output);
	}

	public void testNullItems() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems(null);

		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertEquals("<select id=\"country\" name=\"country\"></select>", output);
	}

	public void testWithList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems(Country.getCountries());
		assertList(true);
	}

	public void testWithResolvedList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		assertList(true);
	}

	public void testWithOtherValue() throws Exception {
		TestBean tb = getTestBean();
		tb.setCountry("AT");
		this.tag.setPath("country");
		this.tag.setItems(Country.getCountries());
		assertList(false);
	}

	public void testWithNullValue() throws Exception {
		TestBean tb = getTestBean();
		tb.setCountry(null);
		this.tag.setPath("country");
		this.tag.setItems(Country.getCountries());
		assertList(false);
	}

	public void testWithListAndNoLabel() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
		validateOutput(getWriter().toString(), true);
	}

	public void testWithMap() throws Exception {
		this.tag.setPath("sex");
		this.tag.setItems("${sexes}");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

	public void testWithInvalidList() throws Exception {
		this.tag.setPath("country");
		this.tag.setItems("${other}");
		this.tag.setItemValue("isoCode");
		try {
			this.tag.doStartTag();
			fail("Must not be able to use a non-Collection typed value as the value of 'items'");
		}
		catch (JspException expected) {
			String message = expected.getMessage();
			assertTrue(message.indexOf("items") > -1);
			assertTrue(message.indexOf("org.springframework.beans.TestBean") > -1);
		}
	}

	public void testWithNestedOptions() throws Exception {
		this.tag.setPath("country");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);

		BindStatus value = (BindStatus) getPageContext().getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
		assertEquals("Selected country not exposed in page context", "UK", value.getValue());

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		this.tag.doFinally();

		String output = getWriter().toString();
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));
		assertContainsAttribute(output, "name", "country");
	}

	public void testWithStringArray() throws Exception {
		this.tag.setPath("name");
		this.tag.setItems(getNames());
		assertStringArray();
	}

	public void testWithResolvedStringArray() throws Exception {
		this.tag.setPath("name");
		this.tag.setItems("${names}");
		assertStringArray();
	}

	public void testWithIntegerArray() throws Exception {
		this.tag.setPath("someIntegerArray");
		Integer[] array = new Integer[50];
		for (int i = 0; i < array.length; i++) {
			array[i] = new Integer(i);
		}
		this.tag.setItems(array);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals(2, rootElement.elements().size());

		Element selectElement = rootElement.element("select");
		assertEquals("select", selectElement.getName());
		assertEquals("someIntegerArray", selectElement.attribute("name").getValue());

		List children = selectElement.elements();
		assertEquals("Incorrect number of children", array.length, children.size());

		Element e = (Element) selectElement.selectSingleNode("option[text() = '12']");
		assertEquals("'12' node not selected", "selected", e.attribute("selected").getValue());

		e = (Element) selectElement.selectSingleNode("option[text() = '34']");
		assertEquals("'34' node not selected", "selected", e.attribute("selected").getValue());
	}

	public void testWithFloatCustom() throws Exception {
		PropertyEditor propertyEditor = new SimpleFloatEditor();
		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(getTestBean(), COMMAND_NAME);
		errors.getPropertyAccessor().registerCustomEditor(Float.class, propertyEditor);
		exposeBindingResult(errors);

		this.tag.setPath("myFloat");

		Float[] array = new Float[] {
				new Float("12.30"), new Float("12.32"), new Float("12.34"), new Float("12.36"),
				new Float("12.38"), new Float("12.40"), new Float("12.42"), new Float("12.44"),
				new Float("12.46"), new Float("12.48")
		};

		this.tag.setItems(array);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("myFloat", rootElement.attribute("name").getValue());
		List children = rootElement.elements();
		assertEquals("Incorrect number of children", array.length, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[text() = '12.34f']");
		assertEquals("'12.34' node not selected", "selected", e.attribute("selected").getValue());

		e = (Element) rootElement.selectSingleNode("option[text() = '12.32f']");
		assertNull("'12.32' node incorrectly selected", e.attribute("selected"));
	}

	public void testWithMultiList() throws Exception {
		List list = new ArrayList();
		list.add(Country.COUNTRY_UK);
		list.add(Country.COUNTRY_AT);
		this.bean.setSomeList(list);

		this.tag.setPath("someList");
		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals(2, rootElement.elements().size());

		Element selectElement = rootElement.element("select");
		assertEquals("select", selectElement.getName());
		assertEquals("someList", selectElement.attribute("name").getValue());

		List children = selectElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) selectElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "selected", e.attribute("selected").getValue());

		e = (Element) selectElement.selectSingleNode("option[@value = 'AT']");
		assertEquals("AT node not selected", "selected", e.attribute("selected").getValue());
	}

	public void testWithMultiListAndCustomEditor() throws Exception {
		List list = new ArrayList();
		list.add(Country.COUNTRY_UK);
		list.add(Country.COUNTRY_AT);
		this.bean.setSomeList(list);

		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		errors.getPropertyAccessor().registerCustomEditor(List.class, new CustomCollectionEditor(LinkedList.class) {
			public String getAsText() {
				return getValue().toString();
			}
		});
		exposeBindingResult(errors);

		this.tag.setPath("someList");
		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals(2, rootElement.elements().size());

		Element selectElement = rootElement.element("select");
		assertEquals("select", selectElement.getName());
		assertEquals("someList", selectElement.attribute("name").getValue());

		List children = selectElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) selectElement.selectSingleNode("option[@value = 'UK']");
		assertEquals("UK node not selected", "selected", e.attribute("selected").getValue());

		e = (Element) selectElement.selectSingleNode("option[@value = 'AT']");
		assertEquals("AT node not selected", "selected", e.attribute("selected").getValue());
	}

	public void testWithMultiMap() throws Exception {
		Map someMap = new HashMap();
		someMap.put("M", "Male");
		someMap.put("F", "Female");
		this.bean.setSomeMap(someMap);

		this.tag.setPath("someMap");
		this.tag.setItems("${sexes}");

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals(2, rootElement.elements().size());

		Element selectElement = rootElement.element("select");
		assertEquals("select", selectElement.getName());
		assertEquals("someMap", selectElement.attribute("name").getValue());

		List children = selectElement.elements();
		assertEquals("Incorrect number of children", 2, children.size());

		Element e = (Element) selectElement.selectSingleNode("option[@value = 'M']");
		assertEquals("M node not selected", "selected", e.attribute("selected").getValue());

		e = (Element) selectElement.selectSingleNode("option[@value = 'F']");
		assertEquals("F node not selected", "selected", e.attribute("selected").getValue());
	}

	public void testMultiWithEmptyCollection() throws Exception {
		this.bean.setSomeList(new ArrayList());

		this.tag.setPath("someList");
		this.tag.setItems("${countries}");
		this.tag.setItemValue("isoCode");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		output = "<doc>" + output + "</doc>";

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals(2, rootElement.elements().size());

		Element selectElement = rootElement.element("select");
		assertEquals("select", selectElement.getName());
		assertEquals("someList", selectElement.attribute("name").getValue());
		assertEquals("multiple", selectElement.attribute("multiple").getValue());

		List children = selectElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element inputElement = rootElement.element("input");
		assertNotNull(inputElement);
	}


	private void assertStringArray() throws JspException, DocumentException {
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTrue(output.startsWith("<select "));
		assertTrue(output.endsWith("</select>"));

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("name", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[text() = 'Rob']");
		assertEquals("Rob node not selected", "selected", e.attribute("selected").getValue());
	}

	private String[] getNames() {
		return new String[]{"Rod", "Rob", "Juergen", "Adrian"};
	}

	private Map getSexes() {
		Map sexes = new HashMap();
		sexes.put("F", "Female");
		sexes.put("M", "Male");
		return sexes;
	}

	protected void extendRequest(MockHttpServletRequest request) {
		super.extendRequest(request);
		request.setAttribute("countries", Country.getCountries());
		request.setAttribute("sexes", getSexes());
		request.setAttribute("other", new TestBean());
		request.setAttribute("names", getNames());
	}

	private void assertList(boolean selected) throws JspException, DocumentException {
		this.tag.setItemValue("isoCode");
		this.tag.setItemLabel("name");
		this.tag.setSize("5");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		validateOutput(output, selected);
		assertContainsAttribute(output, "size", "5");
	}

	private void validateOutput(String output, boolean selected) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(output));
		Element rootElement = document.getRootElement();
		assertEquals("select", rootElement.getName());
		assertEquals("country", rootElement.attribute("name").getValue());

		List children = rootElement.elements();
		assertEquals("Incorrect number of children", 4, children.size());

		Element e = (Element) rootElement.selectSingleNode("option[@value = 'UK']");
		Attribute selectedAttr = e.attribute("selected");
		if (selected) {
			assertTrue(selectedAttr != null && "selected".equals(selectedAttr.getValue()));
		}
		else {
			assertNull(selectedAttr);
		}
	}

	protected TestBean createTestBean() {
		this.bean = new TestBean();
		this.bean.setName("Rob");
		this.bean.setCountry("UK");
		this.bean.setSex("M");
		this.bean.setMyFloat(new Float("12.34"));
		this.bean.setSomeIntegerArray(new Integer[]{new Integer(12), new Integer(34)});
		return this.bean;
	}

	private TestBean getTestBean() {
		return (TestBean) getPageContext().getRequest().getAttribute(COMMAND_NAME);
	}

}
