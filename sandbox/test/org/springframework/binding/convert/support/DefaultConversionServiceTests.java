/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.support.Mapping;
import org.springframework.core.enums.support.ShortCodedLabeledEnum;

/**
 * @author Keith Donald
 */
public class DefaultConversionServiceTests extends TestCase {
	public void testNoConvertersRegistered() {
		DefaultConversionService service = new DefaultConversionService();
		try {
			service.getConversionExecutor(String.class, Integer.class);
			fail("Should have thrown an ise");
		}
		catch (IllegalStateException e) {

		}
	}

	public void testTargetClassNotSupported() {
		DefaultConversionService service = new DefaultConversionService();
		service.afterPropertiesSet();
		try {
			service.getConversionExecutor(String.class, HashMap.class);
			fail("Should have thrown an ise");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testValidConversion() {
		DefaultConversionService service = new DefaultConversionService();
		service.afterPropertiesSet();
		ConversionExecutor executor = service.getConversionExecutor(String.class, Integer.class);
		Integer three = (Integer)executor.execute("3");
		assertEquals(3, three.intValue());
	}

	public void testLabeledEnumConversionNoSuchEnum() {
		DefaultConversionService service = new DefaultConversionService();
		service.afterPropertiesSet();
		service.addConverter(new TextToLabeledEnum(MyEnum.class, service.getFormatterLocator()));
		ConversionExecutor executor = service.getConversionExecutor(String.class, MyEnum.class);
		try {
			MyEnum myEnum = (MyEnum)executor.execute("My Invalid Label");
			fail("Should have failed");
		}
		catch (IllegalArgumentException e) {
		}
	}

	public void testValidLabeledEnumConversion() {
		DefaultConversionService service = new DefaultConversionService();
		service.afterPropertiesSet();
		service.addConverter(new TextToLabeledEnum(MyEnum.class, service.getFormatterLocator()));
		ConversionExecutor executor = service.getConversionExecutor(String.class, MyEnum.class);
		MyEnum myEnum = (MyEnum)executor.execute("My Label 1");
		assertEquals(MyEnum.ONE, myEnum);
	}

	public void testValidMappingConversion() {
		DefaultConversionService service = new DefaultConversionService();
		service.afterPropertiesSet();
		ConversionExecutor executor = service.getConversionExecutor(String.class, Mapping.class);
		
		Mapping mapping = (Mapping)executor.execute("id");
		Map source = new HashMap(1);
		source.put("id", "5");	
		Map target = new HashMap(1);
		mapping = (Mapping)executor.execute("id,java.lang.Long");
		mapping.map(source, target, null);
		assertEquals(new Long(5), target.get("id"));
		
		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id->id");
		mapping.map(source, target, null);
		assertEquals("5", target.get("id"));

		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id->colleagueId,java.lang.Long");
		mapping.map(source, target, null);
		assertEquals(new Long(5), target.get("colleagueId"));

		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id,java.lang.String->colleagueId");
		mapping.map(source, target, null);
		assertEquals("5", target.get("colleagueId"));

		source = new HashMap(1);
		source.put("id", "5");
		target = new HashMap(1);
		mapping = (Mapping)executor.execute("id,java.lang.String->colleagueId,java.lang.Long");
		mapping.map(source, target, null);
		assertEquals(new Long(5), target.get("colleagueId"));

	}

	public static class MyEnum extends ShortCodedLabeledEnum {
		public static MyEnum ONE = new MyEnum(0, "My Label 1");

		public static MyEnum TWO = new MyEnum(1, "My Label 2");

		private MyEnum(int code, String label) {
			super(code, label);
		}
	}
}