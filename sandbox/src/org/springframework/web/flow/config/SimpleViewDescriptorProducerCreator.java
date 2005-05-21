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
package org.springframework.web.flow.config;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.TextToClassConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.SimpleViewDescriptorProducer;
import org.springframework.web.flow.ViewDescriptorProducer;

/**
 * Simple default implementation of a factory that creates view descriptor
 * producers.
 * <p>
 * If the encoded view name starts with the "class:" prefix, the specified
 * <code>ViewDescriptorProducer</code> class will be instantiated. Otherwise
 * a <code>SimpleViewDescriptorProducer</code> will be used to directly
 * produce a <code>ViewDescriptor</code> based on the encoded view name.
 * 
 * @see org.springframework.web.flow.ViewDescriptorProducer
 * @see org.springframework.web.flow.ViewDescriptor
 * @see org.springframework.web.flow.SimpleViewDescriptorProducer
 * 
 * @author Erwin Vervaet
 */
public class SimpleViewDescriptorProducerCreator implements ViewDescriptorProducerCreator {
	
	/**
	 * Prefix used when the encoded view name wants to identify the
	 * class of a ViewDescriptorProducer.
	 */
	public static final String CLASS_PREFIX = "class:";

	public ViewDescriptorProducer create(String encodedView) {
		return createDefaultViewDescriptorProducer(encodedView);
	}
	
	/**
	 * Create the default view descriptor producer (factory)
	 * @param encodedView the encoded view name
	 * @return the newly created producer
	 * @throws ConversionException when there are problems decoding the view name
	 * @throws BeansException when there are problems instantiating the producer
	 */
	protected ViewDescriptorProducer createDefaultViewDescriptorProducer(String encodedView)
			throws ConversionException, BeansException {
		if (StringUtils.hasText(encodedView) && encodedView.startsWith(CLASS_PREFIX)) {
			String producerClassName = encodedView.substring(CLASS_PREFIX.length());
			Class producerClass = (Class)new TextToClassConverter().convert(producerClassName);
			return (ViewDescriptorProducer)BeanUtils.instantiateClass(producerClass);
		}
		else {
			return new SimpleViewDescriptorProducer(encodedView);
		}
	}

}
