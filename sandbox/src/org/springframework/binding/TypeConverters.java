/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.binding.formatters.NumberFormatter;
import org.springframework.core.io.Resource;

/**
 * Registry for type converters
 * @author Keith Donald
 */
public class TypeConverters implements TypeConverterRegistry {
	private static TypeConverters instance;

	private BeanFactory typeConverterRegistry;

	public static synchronized void load(TypeConverters newInstance) {
		instance = newInstance;
	}

	public static synchronized TypeConverters instance() {
		if (instance == null) {
			instance = new TypeConverters();
		}
		return instance;
	}

	public TypeConverters() {
		initDefaultRegistry();
	}

	protected void initDefaultRegistry() {
		ConfigurableListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerSingleton(Integer.class.getName(), new NumberFormatter(Integer.class));
		factory.registerSingleton(Short.class.getName(), new NumberFormatter(Short.class));
		factory.registerSingleton(Double.class.getName(), new NumberFormatter(Double.class));
		factory.registerSingleton(Long.class.getName(), new NumberFormatter(Long.class));
		this.typeConverterRegistry = factory;
	}

	public TypeConverters(Resource xmlTypeConverterDefinitions) {
		this.typeConverterRegistry = new XmlBeanFactory(xmlTypeConverterDefinitions);
	}

	public TypeConverters(BeanFactory converterRegistry) {
		this.typeConverterRegistry = converterRegistry;
	}

	public Formatter getFormatter(String name) {
		return (Formatter)typeConverterRegistry.getBean(name, Formatter.class);
	}

	public Formatter getFormatter(Class clazz) {
		return (Formatter)typeConverterRegistry.getBean(clazz.getName(), Formatter.class);
	}

	public TypeConverter getTypeConverter(String name) {
		return (TypeConverter)typeConverterRegistry.getBean(name, TypeConverter.class);
	}

	public TypeConverter getTypeConverter(Class clazz) {
		return (TypeConverter)typeConverterRegistry.getBean(clazz.getName(), TypeConverter.class);
	}
}