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

package org.springframework.validation;

import java.beans.PropertyEditorSupport;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class ValidationTestSuite extends TestCase {

	public void testBindingNoErrors() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", new Integer(32)));

		binder.bind(pvs);
		binder.close();

		assertTrue("changed name correctly", rod.getName().equals("Rod"));
		assertTrue("changed age correctly", rod.getAge() == 32);

		Map m = binder.getErrors().getModel();
		assertTrue("There is one element in map", m.size() == 2);
		TestBean tb = (TestBean) m.get("person");
		assertTrue("Same object", tb.equals(rod));
	}

	public void testBindingWithErrors() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));
		binder.bind(pvs);

		try {
			binder.close();
			fail("Should have thrown BindException");
		}
		catch (BindException ex) {
			assertTrue("changed name correctly", rod.getName().equals("Rod"));
			//assertTrue("changed age correctly", rod.getAge() == 32);

			Map m = binder.getErrors().getModel();
			//assertTrue("There are 3 element in map", m.size() == 1);
			TestBean tb = (TestBean) m.get("person");
			assertTrue("Same object", tb.equals(rod));

			BindException be = (BindException) m.get(BindException.ERROR_KEY_PREFIX + "person");
			assertTrue("Added itself to map", ex == be);
			assertTrue(be.hasErrors());
			assertTrue("Correct number of errors", be.getErrorCount() == 1);
			assertTrue("Has age errors", be.hasFieldErrors("age"));
			assertTrue("Correct number of age errors", be.getFieldErrorCount("age") == 1);
			assertEquals("32x", binder.getErrors().getFieldValue("age"));
			assertEquals("32x", binder.getErrors().getFieldError("age").getRejectedValue());
			assertEquals(0, tb.getAge());
		}
	}

	public void testBindingWithAllowedFields() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setAllowedFields(new String[] {"name", "myparam"});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));

		binder.bind(pvs);
		binder.close();

		assertTrue("changed name correctly", rod.getName().equals("Rod"));
		assertTrue("did not change age", rod.getAge() == 0);

		Map m = binder.getErrors().getModel();
		assertTrue("There is one element in map", m.size() == 2);
		TestBean tb = (TestBean) m.get("person");
		assertTrue("Same object", tb.equals(rod));
	}

	public void testBindingWithAllowedFieldsUsingAsterisks() throws Exception {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setAllowedFields(new String[] {"nam*", "*ouchy"});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		pvs.addPropertyValue(new PropertyValue("touchy", "Rod"));
		pvs.addPropertyValue(new PropertyValue("age", "32x"));

		binder.bind(pvs);
		binder.close();

		assertTrue("changed name correctly", "Rod".equals(rod.getName()));
		assertTrue("changed touchy correctly", "Rod".equals(rod.getTouchy()));
		assertTrue("did not change age", rod.getAge() == 0);

		Map m = binder.getErrors().getModel();
		assertTrue("There is one element in map", m.size() == 2);
		TestBean tb = (TestBean) m.get("person");
		assertTrue("Same object", tb.equals(rod));
	}
	
	/**
	 * Tests for required field, both null, non-existing and empty strings
	 */
	public void testBindingWithRequiredFields() throws Exception {
		TestBean alef = new TestBean();		
		
		DataBinder binder = new DataBinder(alef, "person");
		binder.setRequiredFields(new String[] {"name", "touchy", "date"});		
		
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("touchy", ""));
		pvs.addPropertyValue(new PropertyValue("name", null));
		
		binder.bind(pvs);
		
		BindException ex = binder.getErrors();
		assertEquals("Wrong amount of errors", 3, ex.getErrorCount());		
		
		/*// for debugging purposes
		assertTrue("Error available for name", ex.getFieldError("name") != null);
		assertTrue("Error available for touchy", ex.getFieldError("touchy") != null);
		assertTrue("Error available for date", ex.getFieldError("date") != null);
		*/
		
		assertEquals("required", ex.getFieldError("touchy").getCode());
		assertEquals("required", ex.getFieldError("name").getCode());
		assertEquals("required", ex.getFieldError("date").getCode());
	}

	public void testCustomEditorForSingleProperty() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, "name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "value"));
		pvs.addPropertyValue(new PropertyValue("touchy", "value"));
		pvs.addPropertyValue(new PropertyValue("spouse.name", "sue"));
		binder.bind(pvs);

		binder.getErrors().rejectValue("name", "someCode", "someMessage");
		binder.getErrors().rejectValue("touchy", "someCode", "someMessage");
		binder.getErrors().rejectValue("spouse.name", "someCode", "someMessage");

		assertEquals("", binder.getErrors().getNestedPath());
		assertEquals("value", binder.getErrors().getFieldValue("name"));
		assertEquals("prefixvalue", binder.getErrors().getFieldError("name").getRejectedValue());
		assertEquals("prefixvalue", tb.getName());
		assertEquals("value", binder.getErrors().getFieldValue("touchy"));
		assertEquals("value", binder.getErrors().getFieldError("touchy").getRejectedValue());
		assertEquals("value", tb.getTouchy());

		assertTrue(binder.getErrors().hasFieldErrors("spouse.*"));
		assertEquals(1, binder.getErrors().getFieldErrorCount("spouse.*"));
		assertEquals("spouse.name", binder.getErrors().getFieldError("spouse.*").getField());
	}

	public void testCustomEditorForPrimitiveProperty() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(int.class, "age", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new Integer(99));
			}
			public String getAsText() {
				return "argh";
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("age", ""));
		binder.bind(pvs);

		assertEquals("argh", binder.getErrors().getFieldValue("age"));
		assertEquals(99, tb.getAge());
	}

	public void testCustomEditorForAllStringProperties() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
			public String getAsText() {
				return ((String) getValue()).substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "value"));
		pvs.addPropertyValue(new PropertyValue("touchy", "value"));
		binder.bind(pvs);

		binder.getErrors().rejectValue("name", "someCode", "someMessage");
		binder.getErrors().rejectValue("touchy", "someCode", "someMessage");

		assertEquals("value", binder.getErrors().getFieldValue("name"));
		assertEquals("prefixvalue", binder.getErrors().getFieldError("name").getRejectedValue());
		assertEquals("prefixvalue", tb.getName());
		assertEquals("value", binder.getErrors().getFieldValue("touchy"));
		assertEquals("prefixvalue", binder.getErrors().getFieldError("touchy").getRejectedValue());
		assertEquals("prefixvalue", tb.getTouchy());
	}


	public void testValidatorNoErrors() {
		TestBean tb = new TestBean();
		tb.setAge(33);
		tb.setName("Rod");
		try {
			tb.setTouchy("Rod");
		}
		catch (Exception e) {
			fail("Should not throw any Exception");
		}
		TestBean tb2 = new TestBean();
		tb2.setAge(34);
		tb.setSpouse(tb2);
		Errors errors = new BindException(tb, "tb");
		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);
		errors.setNestedPath("spouse");
		assertEquals("spouse.", errors.getNestedPath());
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);
		errors.setNestedPath("");
		assertEquals("", errors.getNestedPath());
		assertTrue(!errors.hasErrors());
		assertTrue(!errors.hasGlobalErrors());
		assertTrue(!errors.hasFieldErrors("age"));
		assertTrue(!errors.hasFieldErrors("name"));
	}

	public void testValidatorWithErrors() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		Errors errors = new BindException(tb, "tb");
		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);
		errors.setNestedPath("spouse.");
		assertEquals("spouse.", errors.getNestedPath());
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);
		errors.setNestedPath("");
		assertEquals(6, errors.getErrorCount());
		assertEquals(2, errors.getGlobalErrorCount());
		assertEquals("NAME_TOUCHY_MISMATCH", errors.getGlobalError().getCode());
		assertEquals("NAME_TOUCHY_MISMATCH", ((ObjectError) errors.getGlobalErrors().get(0)).getCode());
		assertEquals("NAME_TOUCHY_MISMATCH.tb", ((ObjectError) errors.getGlobalErrors().get(0)).getCodes()[0]);
		assertEquals("NAME_TOUCHY_MISMATCH", ((ObjectError) errors.getGlobalErrors().get(0)).getCodes()[1]);
		assertEquals("tb", ((ObjectError) errors.getGlobalErrors().get(0)).getObjectName());
		assertEquals("GENERAL_ERROR", ((ObjectError) errors.getGlobalErrors().get(1)).getCode());
		assertEquals("GENERAL_ERROR.tb", ((ObjectError) errors.getGlobalErrors().get(1)).getCodes()[0]);
		assertEquals("GENERAL_ERROR", ((ObjectError) errors.getGlobalErrors().get(1)).getCodes()[1]);
		assertEquals("msg", ((ObjectError) errors.getGlobalErrors().get(1)).getDefaultMessage());
		assertEquals("arg", ((ObjectError) errors.getGlobalErrors().get(1)).getArguments()[0]);
		assertEquals(2, errors.getFieldErrorCount("age"));
		assertEquals("TOO_YOUNG", errors.getFieldError("age").getCode());
		assertEquals("TOO_YOUNG", ((FieldError) errors.getFieldErrors("age").get(0)).getCode());
		assertEquals("tb", ((FieldError) errors.getFieldErrors("age").get(0)).getObjectName());
		assertEquals("age", ((FieldError) errors.getFieldErrors("age").get(0)).getField());
		assertEquals(new Integer(0), ((FieldError) errors.getFieldErrors("age").get(0)).getRejectedValue());
		assertEquals("AGE_NOT_ODD", ((FieldError) errors.getFieldErrors("age").get(1)).getCode());
		assertEquals(1, errors.getFieldErrorCount("name"));
		assertEquals("NOT_ROD", errors.getFieldError("name").getCode());
		assertEquals("NOT_ROD.tb.name", errors.getFieldError("name").getCodes()[0]);
		assertEquals("NOT_ROD.name", errors.getFieldError("name").getCodes()[1]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("name").getCodes()[2]);
		assertEquals("NOT_ROD", errors.getFieldError("name").getCodes()[3]);
		assertEquals("name", ((FieldError) errors.getFieldErrors("name").get(0)).getField());
		assertEquals(null, ((FieldError) errors.getFieldErrors("name").get(0)).getRejectedValue());
		assertEquals(1, errors.getFieldErrorCount("spouse.age"));
		assertEquals("TOO_YOUNG", errors.getFieldError("spouse.age").getCode());
		assertEquals("tb", ((FieldError) errors.getFieldErrors("spouse.age").get(0)).getObjectName());
		assertEquals(new Integer(0), ((FieldError) errors.getFieldErrors("spouse.age").get(0)).getRejectedValue());
	}

	public void testBindingToIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getErrors();
		errors.rejectValue("array[0].name", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key1].name", "NOT_ROD", "are you sure you're not Rod?");

		assertEquals(1, errors.getFieldErrorCount("array[0].name"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0].name").getCode());
		assertEquals("NOT_ROD.tb.array[0].name", errors.getFieldError("array[0].name").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array.name", errors.getFieldError("array[0].name").getCodes()[1]);
		assertEquals("NOT_ROD.array[0].name", errors.getFieldError("array[0].name").getCodes()[2]);
		assertEquals("NOT_ROD.array.name", errors.getFieldError("array[0].name").getCodes()[3]);
		assertEquals("NOT_ROD.name", errors.getFieldError("array[0].name").getCodes()[4]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("array[0].name").getCodes()[5]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0].name").getCodes()[6]);
		assertEquals(1, errors.getFieldErrorCount("map[key1].name"));
		assertEquals("NOT_ROD", errors.getFieldError("map[key1].name").getCode());
		assertEquals("NOT_ROD.tb.map[key1].name", errors.getFieldError("map[key1].name").getCodes()[0]);
		assertEquals("NOT_ROD.tb.map.name", errors.getFieldError("map[key1].name").getCodes()[1]);
		assertEquals("NOT_ROD.map[key1].name", errors.getFieldError("map[key1].name").getCodes()[2]);
		assertEquals("NOT_ROD.map.name", errors.getFieldError("map[key1].name").getCodes()[3]);
		assertEquals("NOT_ROD.name", errors.getFieldError("map[key1].name").getCodes()[4]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("map[key1].name").getCodes()[5]);
		assertEquals("NOT_ROD", errors.getFieldError("map[key1].name").getCodes()[6]);
	}

	public void testBindingToNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].nestedIndexedBean.list[0].name", "a");
		binder.bind(pvs);
		Errors errors = binder.getErrors();
		errors.rejectValue("array[0].nestedIndexedBean.list[0].name", "NOT_ROD", "are you sure you're not Rod?");

		assertEquals(1, errors.getFieldErrorCount("array[0].nestedIndexedBean.list[0].name"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCode());
		assertEquals("NOT_ROD.tb.array[0].nestedIndexedBean.list[0].name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array[0].nestedIndexedBean.list.name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[1]);
		assertEquals("NOT_ROD.tb.array.nestedIndexedBean.list.name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[2]);
		assertEquals("NOT_ROD.array[0].nestedIndexedBean.list[0].name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[3]);
		assertEquals("NOT_ROD.array[0].nestedIndexedBean.list.name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[4]);
		assertEquals("NOT_ROD.array.nestedIndexedBean.list.name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[5]);
		assertEquals("NOT_ROD.name", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[6]);
		assertEquals("NOT_ROD.java.lang.String", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[7]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[8]);
	}

	public void testDirectBindingToIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new TestBean("array" + text, 99));
			}
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getErrors();
		errors.rejectValue("array[0]", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key1]", "NOT_ROD", "are you sure you're not Rod?");

		assertEquals("arraya", errors.getFieldValue("array[0]"));
		assertEquals(1, errors.getFieldErrorCount("array[0]"));
		assertEquals("NOT_ROD", errors.getFieldError("array[0]").getCode());
		assertEquals("NOT_ROD.tb.array[0]", errors.getFieldError("array[0]").getCodes()[0]);
		assertEquals("NOT_ROD.tb.array", errors.getFieldError("array[0]").getCodes()[1]);
		assertEquals("NOT_ROD.array[0]", errors.getFieldError("array[0]").getCodes()[2]);
		assertEquals("NOT_ROD.array", errors.getFieldError("array[0]").getCodes()[3]);
		assertEquals("NOT_ROD.org.springframework.beans.TestBean", errors.getFieldError("array[0]").getCodes()[4]);
		assertEquals("NOT_ROD", errors.getFieldError("array[0]").getCodes()[5]);
		assertEquals(1, errors.getFieldErrorCount("map[key1]"));
		assertEquals("NOT_ROD", errors.getFieldError("map[key1]").getCode());
		assertEquals("NOT_ROD.tb.map[key1]", errors.getFieldError("map[key1]").getCodes()[0]);
		assertEquals("NOT_ROD.tb.map", errors.getFieldError("map[key1]").getCodes()[1]);
		assertEquals("NOT_ROD.map[key1]", errors.getFieldError("map[key1]").getCodes()[2]);
		assertEquals("NOT_ROD.map", errors.getFieldError("map[key1]").getCodes()[3]);
		assertEquals("NOT_ROD.org.springframework.beans.TestBean", errors.getFieldError("map[key1]").getCodes()[4]);
		assertEquals("NOT_ROD", errors.getFieldError("map[key1]").getCodes()[5]);
	}

	public void testBindingErrors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("age", "32x"));
		binder.bind(pvs);
		Errors errors = binder.getErrors();
		FieldError ageError = errors.getFieldError("age");
		assertEquals("typeMismatch", ageError.getCode());

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages1");
		String msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertEquals("Field age did not have correct type", msg);

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages2");
		msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertEquals("Field Age did not have correct type", msg);

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages3");
		msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertEquals("Field Person Age did not have correct type", msg);
	}


	private static class TestBeanValidator implements Validator {

		public boolean supports(Class clazz) {
			return TestBean.class.isAssignableFrom(clazz);
		}

		public void validate(Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb.getAge() < 32) {
				errors.rejectValue("age", "TOO_YOUNG", null, "simply too young");
			}
			if (tb.getAge() % 2 == 0) {
				errors.rejectValue("age", "AGE_NOT_ODD", null, "your age isn't odd");
			}
			if (tb.getName() == null || !tb.getName().equals("Rod")) {
				errors.rejectValue("name", "NOT_ROD", "are you sure you're not Rod?");
			}
			if (tb.getTouchy() == null || !tb.getTouchy().equals(tb.getName())) {
				errors.reject("NAME_TOUCHY_MISMATCH", "name and touchy do not match");
			}
			if (tb.getAge() == 0) {
				errors.reject("GENERAL_ERROR", new String[] {"arg"}, "msg");
			}
		}
	}
	

	private static class SpouseValidator implements Validator {

		public boolean supports(Class clazz) {
			return TestBean.class.isAssignableFrom(clazz);
		}

		public void validate(Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb.getAge() < 32) {
				errors.rejectValue("age", "TOO_YOUNG", null, "simply too young");
			}
		}
	}

}
