/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import java.beans.PropertyEditor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.convert.ConverterLocator;
import org.springframework.binding.format.FormatterLocator;
import org.springframework.binding.support.TextToMappingConverter;

/**
 * Specialized registry for type converters.
 * @author Keith Donald
 */
public class DefaultConverterLocator implements ConverterLocator, BeanFactoryPostProcessor, InitializingBean {

	private Map sourceClassConverters = new HashMap();

	private FormatterLocator formatterLocator;

	public DefaultConverterLocator() {

	}

	public void setConverters(Converter[] converters) {
		this.sourceClassConverters = new HashMap(converters.length);
		addConverters(converters);
	}

	public void addConverters(Converter[] converters) {
		for (int i = 0; i < converters.length; i++) {
			addConverter(converters[i]);
		}
	}

	public void addConverter(Converter converter) {
		Class[] sourceClasses = converter.getSourceClasses();
		Class[] targetClasses = converter.getTargetClasses();
		for (int i = 0; i < sourceClasses.length; i++) {
			Class sourceClass = sourceClasses[i];
			Map sourceMap = (Map)this.sourceClassConverters.get(sourceClass);
			if (sourceMap == null) {
				sourceMap = new HashMap();
				this.sourceClassConverters.put(sourceClass, sourceMap);
			}
			for (int j = 0; j < targetClasses.length; j++) {
				Class targetClass = targetClasses[j];
				sourceMap.put(targetClass, converter);
			}
		}
	}

	public void setFormatterLocator(FormatterLocator formatterLocator) {
		this.formatterLocator = formatterLocator;
	}

	public void afterPropertiesSet() {
		addDefaultConverters();
	}

	protected void addDefaultConverters() {
		addConverter(new TextToClassConverter());
		addConverter(new TextToNumberConverter(Short.class, formatterLocator));
		addConverter(new TextToNumberConverter(Integer.class, formatterLocator));
		addConverter(new TextToNumberConverter(Long.class, formatterLocator));
		addConverter(new TextToNumberConverter(Float.class, formatterLocator));
		addConverter(new TextToNumberConverter(Double.class, formatterLocator));
		addConverter(new TextToNumberConverter(BigInteger.class, formatterLocator));
		addConverter(new TextToNumberConverter(BigDecimal.class, formatterLocator));
		addConverter(new TextToMappingConverter(this));
	}

	public Converter getConverter(Class sourceClass, Class targetClass) {
		if (sourceClassConverters == null) {
			return null;
		}
		Map sourceTargetConverters = (Map)sourceClassConverters.get(sourceClass);
		return (Converter)sourceTargetConverters.get(targetClass);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.sourceClassConverters != null) {
			Map fromStringConverters = (Map)sourceClassConverters.get(String.class);
			if (fromStringConverters != null) {
				Iterator it = fromStringConverters.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry)it.next();
					PropertyEditor editor = new ConverterPropertyEditorAdapter((Converter)entry.getValue());
					beanFactory.registerCustomEditor((Class)entry.getKey(), editor);
				}
			}
		}
	}
}