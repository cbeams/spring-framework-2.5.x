/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.convert.ConverterLocator;
import org.springframework.core.io.Resource;

/**
 * Specialized registry for type converters, backed by a Spring BeanFactory.
 * @author Keith Donald
 */
public class BeanFactoryConverterLocator implements ConverterLocator, BeanFactoryAware {
	private BeanFactory converterBeanFactory;

	public BeanFactoryConverterLocator() {

	}

	public BeanFactoryConverterLocator(Resource xmlTypeConverterDefinitions) {
		this.converterBeanFactory = new XmlBeanFactory(xmlTypeConverterDefinitions);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (converterBeanFactory == null) {
			this.converterBeanFactory = beanFactory;
		}
	}

	public Converter getConverter(String name) {
		return (Converter)converterBeanFactory.getBean(name, Converter.class);
	}

	public Converter getConverter(Class sourceClass, Class targetClass) {
		throw new UnsupportedOperationException();
	}

	public Converter getConverter(Class clazz) {
		return (Converter)converterBeanFactory.getBean(clazz.getName(), Converter.class);
	}
}