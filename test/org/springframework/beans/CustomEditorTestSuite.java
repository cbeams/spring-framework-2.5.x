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

package org.springframework.beans;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

/**
 * @author Juergen Hoeller
 * @since 10.06.2003
 */
public class CustomEditorTestSuite extends TestCase {

	public void testComplexObject() {
		TestBean t = new TestBean();
		String newName = "Rod";
		String tbString = "Kerry_34";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.registerCustomEditor(ITestBean.class, null, new TestBeanEditor());
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("age", new Integer(55)));
			pvs.addPropertyValue(new PropertyValue("name", newName));
			pvs.addPropertyValue(new PropertyValue("touchy", "valid"));
			pvs.addPropertyValue(new PropertyValue("spouse", tbString));
			bw.setPropertyValues(pvs);
			assertTrue("spouse is non-null", t.getSpouse() != null);
			assertTrue("spouse name is Kerry and age is 34", t.getSpouse().getName().equals("Kerry") && t.getSpouse().getAge() == 34);
			//assertTrue("Event source is correct", l.getEventCount() == 3);
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid: " + ex.getMessage());
		}
	}

	public void testCustomEditorForSingleProperty() {
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(String.class, "name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
		});
		try {
			bw.setPropertyValue("name", "value");
			bw.setPropertyValue("touchy", "value");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertEquals("prefixvalue", bw.getPropertyValue("name"));
		assertEquals("prefixvalue", tb.getName());
		assertEquals("value", bw.getPropertyValue("touchy"));
		assertEquals("value", tb.getTouchy());
	}

	public void testCustomEditorForAllStringProperties() {
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
		});
		try {
			bw.setPropertyValue("name", "value");
			bw.setPropertyValue("touchy", "value");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertEquals("prefixvalue", bw.getPropertyValue("name"));
		assertEquals("prefixvalue", tb.getName());
		assertEquals("prefixvalue", bw.getPropertyValue("touchy"));
		assertEquals("prefixvalue", tb.getTouchy());
	}

	public void testCustomEditorForSingleNestedProperty() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(String.class, "spouse.name", new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
		});
		try {
			bw.setPropertyValue("spouse.name", "value");
			bw.setPropertyValue("touchy", "value");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertEquals("prefixvalue", bw.getPropertyValue("spouse.name"));
		assertEquals("prefixvalue", tb.getSpouse().getName());
		assertEquals("value", bw.getPropertyValue("touchy"));
		assertEquals("value", tb.getTouchy());
	}

	public void testCustomEditorForAllNestedStringProperties() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
		});
		try {
			bw.setPropertyValue("spouse.name", "value");
			bw.setPropertyValue("touchy", "value");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertEquals("prefixvalue", bw.getPropertyValue("spouse.name"));
		assertEquals("prefixvalue", tb.getSpouse().getName());
		assertEquals("prefixvalue", bw.getPropertyValue("touchy"));
		assertEquals("prefixvalue", tb.getTouchy());
	}

	public void testBooleanPrimitiveEditor() {
		BooleanTestBean tb = new BooleanTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);

		try {
			bw.setPropertyValue("bool1", "true");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool1 value", Boolean.TRUE.equals(bw.getPropertyValue("bool1")));
		assertTrue("Correct bool1 value", tb.isBool1());

		try {
			bw.setPropertyValue("bool1", "false");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool1 value", Boolean.FALSE.equals(bw.getPropertyValue("bool1")));
		assertTrue("Correct bool1 value", !tb.isBool1());

		try {
			bw.setPropertyValue("bool1", "argh");
		}
		catch (BeansException ex) {
			// expected
			return;
		}
		fail("Should have thrown BeansException");
	}

	public void testBooleanObjectEditorWithAllowEmpty() {
		BooleanTestBean tb = new BooleanTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(Boolean.class, null, new CustomBooleanEditor(true));

		try {
			bw.setPropertyValue("bool2", "true");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", Boolean.TRUE.equals(bw.getPropertyValue("bool2")));
		assertTrue("Correct bool2 value", tb.getBool2().booleanValue());

		try {
			bw.setPropertyValue("bool2", "false");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", Boolean.FALSE.equals(bw.getPropertyValue("bool2")));
		assertTrue("Correct bool2 value", !tb.getBool2().booleanValue());

		try {
			bw.setPropertyValue("bool2", "");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", bw.getPropertyValue("bool2") == null);
		assertTrue("Correct bool2 value", tb.getBool2() == null);
	}

	public void testBooleanObjectEditorWithoutAllowEmpty() {
		BooleanTestBean tb = new BooleanTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(Boolean.class, null, new CustomBooleanEditor(false));

		try {
			bw.setPropertyValue("bool2", "true");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", Boolean.TRUE.equals(bw.getPropertyValue("bool2")));
		assertTrue("Correct bool2 value", tb.getBool2().booleanValue());

		try {
			bw.setPropertyValue("bool2", "false");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", Boolean.FALSE.equals(bw.getPropertyValue("bool2")));
		assertTrue("Correct bool2 value", !tb.getBool2().booleanValue());

		try {
			bw.setPropertyValue("bool2", "");
		}
		catch (BeansException ex) {
			// expected
			assertTrue("Correct bool2 value", bw.getPropertyValue("bool2") != null);
			assertTrue("Correct bool2 value", tb.getBool2() != null);
			return;
		}
		fail("Should have throw BeansException");
	}

	public void testNumberEditorWithoutAllowEmpty() {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
		NumberTestBean tb = new NumberTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(short.class, null, new CustomNumberEditor(Short.class, nf, false));
		bw.registerCustomEditor(Short.class, null, new CustomNumberEditor(Short.class, nf, false));
		bw.registerCustomEditor(int.class, null, new CustomNumberEditor(Short.class, nf, false));
		bw.registerCustomEditor(Integer.class, null, new CustomNumberEditor(Integer.class, nf, false));
		bw.registerCustomEditor(long.class, null, new CustomNumberEditor(Long.class, nf, false));
		bw.registerCustomEditor(Long.class, null, new CustomNumberEditor(Long.class, nf, false));
		bw.registerCustomEditor(BigInteger.class, null, new CustomNumberEditor(BigInteger.class, nf, false));
		bw.registerCustomEditor(float.class, null, new CustomNumberEditor(Float.class, nf, false));
		bw.registerCustomEditor(Float.class, null, new CustomNumberEditor(Float.class, nf, false));
		bw.registerCustomEditor(double.class, null, new CustomNumberEditor(Double.class, nf, false));
		bw.registerCustomEditor(Double.class, null, new CustomNumberEditor(Double.class, nf, false));
		bw.registerCustomEditor(BigDecimal.class, null, new CustomNumberEditor(BigDecimal.class, nf, false));

		try {
			bw.setPropertyValue("short1", "1");
			bw.setPropertyValue("short2", "2");
			bw.setPropertyValue("int1", "7");
			bw.setPropertyValue("int2", "8");
			bw.setPropertyValue("long1", "5");
			bw.setPropertyValue("long2", "6");
			bw.setPropertyValue("bigInteger", "3");
			bw.setPropertyValue("float1", "7,1");
			bw.setPropertyValue("float2", "8,1");
			bw.setPropertyValue("double1", "5,1");
			bw.setPropertyValue("double2", "6,1");
			bw.setPropertyValue("bigDecimal", "4");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}

		assertTrue("Correct short1 value", new Short("1").equals(bw.getPropertyValue("short1")));
		assertTrue("Correct short1 value", tb.getShort1() == 1);
		assertTrue("Correct short2 value", new Short("2").equals(bw.getPropertyValue("short2")));
		assertTrue("Correct short2 value", new Short("2").equals(tb.getShort2()));
		assertTrue("Correct int1 value", new Integer("7").equals(bw.getPropertyValue("int1")));
		assertTrue("Correct int1 value", tb.getInt1() == 7);
		assertTrue("Correct int2 value", new Integer("8").equals(bw.getPropertyValue("int2")));
		assertTrue("Correct int2 value", new Integer("8").equals(tb.getInt2()));
		assertTrue("Correct long1 value", new Long("5").equals(bw.getPropertyValue("long1")));
		assertTrue("Correct long1 value", tb.getLong1() == 5);
		assertTrue("Correct long2 value", new Long("6").equals(bw.getPropertyValue("long2")));
		assertTrue("Correct long2 value", new Long("6").equals(tb.getLong2()));
		assertTrue("Correct bigInteger value", new BigInteger("3").equals(bw.getPropertyValue("bigInteger")));
		assertTrue("Correct bigInteger value", new BigInteger("3").equals(tb.getBigInteger()));
		assertTrue("Correct float1 value", new Float("7.1").equals(bw.getPropertyValue("float1")));
		assertTrue("Correct float1 value", new Float("7.1").equals(new Float(tb.getFloat1())));
		assertTrue("Correct float2 value", new Float("8.1").equals(bw.getPropertyValue("float2")));
		assertTrue("Correct float2 value", new Float("8.1").equals(tb.getFloat2()));
		assertTrue("Correct double1 value", new Double("5.1").equals(bw.getPropertyValue("double1")));
		assertTrue("Correct double1 value", tb.getDouble1() == 5.1);
		assertTrue("Correct double2 value", new Double("6.1").equals(bw.getPropertyValue("double2")));
		assertTrue("Correct double2 value", new Double("6.1").equals(tb.getDouble2()));
		assertTrue("Correct bigDecimal value", new BigDecimal("4.0").equals(bw.getPropertyValue("bigDecimal")));
		assertTrue("Correct bigDecimal value", new BigDecimal("4.0").equals(tb.getBigDecimal()));
	}

	public void testNumberEditorsWithAllowEmpty() {
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
		NumberTestBean tb = new NumberTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(long.class, null, new CustomNumberEditor(Long.class, nf, true));
		bw.registerCustomEditor(Long.class, null, new CustomNumberEditor(Long.class, nf, true));

		try {
			bw.setPropertyValue("long1", "5");
			bw.setPropertyValue("long2", "6");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct long1 value", new Long("5").equals(bw.getPropertyValue("long1")));
		assertTrue("Correct long1 value", tb.getLong1() == 5);
		assertTrue("Correct long2 value", new Long("6").equals(bw.getPropertyValue("long2")));
		assertTrue("Correct long2 value", new Long("6").equals(tb.getLong2()));

		try {
			bw.setPropertyValue("long2", "");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct long2 value", bw.getPropertyValue("long2") == null);
		assertTrue("Correct long2 value", tb.getLong2() == null);

		try {
			bw.setPropertyValue("long1", "");
		}
		catch (BeansException ex) {
			// expected
			assertTrue("Correct long1 value", new Long("5").equals(bw.getPropertyValue("long1")));
			assertTrue("Correct long1 value", tb.getLong1() == 5);
			return;
		}
		fail("Should have thrown BeansException");
	}

	public void testCustomBooleanEditor() {
		CustomBooleanEditor editor = new CustomBooleanEditor(false);
		editor.setAsText("true");
		assertEquals(Boolean.TRUE, editor.getValue());
		assertEquals("true", editor.getAsText());
		editor.setAsText("false");
		assertEquals(Boolean.FALSE, editor.getValue());
		assertEquals("false", editor.getAsText());
		editor.setValue(null);
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
	}

	public void testCustomBooleanEditorWithEmptyAsNull() {
		CustomBooleanEditor editor = new CustomBooleanEditor(true);
		editor.setAsText("true");
		assertEquals(Boolean.TRUE, editor.getValue());
		assertEquals("true", editor.getAsText());
		editor.setAsText("false");
		assertEquals(Boolean.FALSE, editor.getValue());
		assertEquals("false", editor.getAsText());
		editor.setValue(null);
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
	}

	public void testCustomDateEditor() {
		CustomDateEditor editor = new CustomDateEditor(null, false);
		editor.setValue(null);
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
	}

	public void testCustomDateEditorWithEmptyAsNull() {
		CustomDateEditor editor = new CustomDateEditor(null, true);
		editor.setValue(null);
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
	}

	public void testCustomNumberEditor() {
		CustomNumberEditor editor = new CustomNumberEditor(Integer.class, false);
		editor.setAsText("5");
		assertEquals(new Integer(5), editor.getValue());
		assertEquals("5", editor.getAsText());
		editor.setValue(null);
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
	}

	public void testCustomNumberEditorWithEmptyAsNull() {
		CustomNumberEditor editor = new CustomNumberEditor(Integer.class, true);
		editor.setAsText("5");
		assertEquals(new Integer(5), editor.getValue());
		assertEquals("5", editor.getAsText());
		editor.setAsText("");
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
		editor.setValue(null);
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
	}

	public void testStringTrimmerEditor() {
		StringTrimmerEditor editor = new StringTrimmerEditor(false);
		editor.setAsText("test");
		assertEquals("test", editor.getValue());
		assertEquals("test", editor.getAsText());
		editor.setAsText(" test ");
		assertEquals("test", editor.getValue());
		assertEquals("test", editor.getAsText());
		editor.setAsText("");
		assertEquals("", editor.getValue());
		assertEquals("", editor.getAsText());
		editor.setValue(null);
		assertEquals("", editor.getAsText());
	}

	public void testStringTrimmerEditorWithEmptyAsNull() {
		StringTrimmerEditor editor = new StringTrimmerEditor(true);
		editor.setAsText("test");
		assertEquals("test", editor.getValue());
		assertEquals("test", editor.getAsText());
		editor.setAsText(" test ");
		assertEquals("test", editor.getValue());
		assertEquals("test", editor.getAsText());
		editor.setAsText("");
		assertEquals(null, editor.getValue());
		assertEquals("", editor.getAsText());
		editor.setValue(null);
		assertEquals("", editor.getAsText());
	}


	private static class TestBeanEditor extends PropertyEditorSupport {

		public void setAsText(String text) {
			TestBean tb = new TestBean();
			StringTokenizer st = new StringTokenizer(text, "_");
			tb.setName(st.nextToken());
			tb.setAge(Integer.parseInt(st.nextToken()));
			setValue(tb);
		}
	}

}
