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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.format.FormatterLocator;
import org.springframework.binding.support.TextToMappingConverter;

/**
 * Specialized registry for type converters.
 * @author Keith Donald
 */
public class DefaultConversionService implements ConversionService, BeanFactoryPostProcessor, InitializingBean {

	private Map sourceClassConverters = new HashMap();

	private FormatterLocator formatterLocator;

	public DefaultConversionService() {

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
		addConverter(new TextToNumberConverter(formatterLocator));
		addConverter(new TextToCodedEnumConverter());
		addConverter(new TextToMappingConverter(this));
	}

	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass) {
		if (sourceClassConverters == null) {
			return null;
		}
		Map sourceTargetConverters = (Map)sourceClassConverters.get(sourceClass);
		Converter converter = (Converter)sourceTargetConverters.get(targetClass);
		if (converter != null) {
			return new ConversionExecutor(converter, targetClass);
		}
		return null;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.sourceClassConverters != null) {
			Map sourceStringConverters = (Map)sourceClassConverters.get(String.class);
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