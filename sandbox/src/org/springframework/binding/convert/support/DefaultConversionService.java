/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.binding.convert.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.format.FormatterLocator;
import org.springframework.binding.format.support.ThreadLocalFormatterLocator;
import org.springframework.binding.support.TextToMappingConverter;

/**
 * Default, local implementation of a conversion service.
 * <p>
 * Acts as bean factory post processor, registering property editor adapters for
 * each supported conversion with a <code>java.lang.String sourceClass</code>.
 * This makes for very convenient use with the Spring container.
 * @author Keith Donald
 */
public class DefaultConversionService implements ConversionService, BeanFactoryPostProcessor, InitializingBean {

	private Map sourceClassConverters = new HashMap();

	private FormatterLocator formatterLocator = new ThreadLocalFormatterLocator();

	public DefaultConversionService() {

	}

	public void setFormatterLocator(FormatterLocator formatterLocator) {
		this.formatterLocator = formatterLocator;
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

	public void afterPropertiesSet() {
		addDefaultConverters();
	}

	protected void addDefaultConverters() {
		addConverter(new TextToClassConverter());
		addConverter(new TextToNumberConverter(getFormatterLocator()));
		addConverter(new TextToMappingConverter(this));
	}

	protected FormatterLocator getFormatterLocator() {
		return formatterLocator;
	}

	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass) {
		if (this.sourceClassConverters == null || this.sourceClassConverters.isEmpty()) {
			throw new IllegalStateException("No converters have been added to this service's registry");
		}
		int modifiers = targetClass.getModifiers();
		if (Modifier.isAbstract(modifiers)) {
			throw new IllegalArgumentException("Target class for conversion must not be abstract");
		}
		if (Modifier.isInterface(modifiers)) {
			throw new IllegalArgumentException("Target class for conversion must not be an interface");
		}
		Map sourceTargetConverters = (Map)findConvertersForSource(sourceClass);
		Converter converter = (Converter)sourceTargetConverters.get(targetClass);
		if (converter != null) {
			return new ConversionExecutor(converter, targetClass);
		}
		else {
			throw new IllegalArgumentException("No converter registered to convert from sourceClass '" + sourceClass
					+ "' to target class '" + targetClass + "'");
		}
	}

	protected Map findConvertersForSource(Class sourceClass) {
		LinkedList classQueue = new LinkedList();
		classQueue.addFirst(sourceClass);
		while (!classQueue.isEmpty()) {
			sourceClass = (Class)classQueue.removeLast();
			Map sourceTargetConverters = (Map)sourceClassConverters.get(sourceClass);
			if (sourceTargetConverters != null && !sourceTargetConverters.isEmpty()) {
				return sourceTargetConverters;
			}
			if (!sourceClass.isInterface() && (sourceClass.getSuperclass() != null)) {
				classQueue.addFirst(sourceClass.getSuperclass());
			}
			// queue up source class's implemented interfaces.
			Class[] interfaces = sourceClass.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				classQueue.addFirst(interfaces[i]);
			}
		}
		throw new IllegalArgumentException("No converters registered to convert from sourceClass '" + sourceClass
				+ "' (including any of its superclasses or interfaces)");
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.sourceClassConverters != null) {
			Map sourceStringConverters = (Map)this.sourceClassConverters.get(String.class);
			if (sourceStringConverters != null) {
				Iterator it = sourceStringConverters.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry)it.next();
					Class targetClass = (Class)entry.getKey();
					PropertyEditor editor = new ConverterPropertyEditorAdapter(new ConversionExecutor((Converter)entry
							.getValue(), targetClass));
					beanFactory.registerCustomEditor(targetClass, editor);
				}
			}
		}
	}
}