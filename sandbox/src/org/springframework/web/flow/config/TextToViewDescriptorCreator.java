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
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.convert.support.TextToClass;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.SimpleViewDescriptorCreator;
import org.springframework.web.flow.ViewDescriptorCreator;

/**
 * Converter that converts an encoded string representation of a
 * view descriptor into a <code>ViewDescriptorCreator</code> that will
 * create such a view descriptor.
 * 
 * @see org.springframework.web.flow.ViewDescriptor
 * @see org.springframework.web.flow.ViewDescriptorCreator
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToViewDescriptorCreator extends AbstractConverter {
	
	/**
	 * Prefix used when the encoded view name wants to identify the
	 * class of a ViewDescriptorCreator.
	 */
	public static final String CLASS_PREFIX = "class:";

	/**
	 * Prefix used when the encoded view name wants to specify that
	 * a redirect is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewDescriptorCreator.class } ;
	}

	protected Object doConvert(Object source, Class targetClass) throws ConversionException {
		return createViewDescriptorCreator((String)source);
	}

	/**
	 * Create the default view descriptor creator (factory).
	 * @param encodedView the encoded view name
	 * @return the newly created creator
	 * @throws ConversionException when there are problems decoding the view name
	 * @throws BeansException when there are problems instantiating the creator
	 */
	protected ViewDescriptorCreator createViewDescriptorCreator(String encodedView)
			throws ConversionException, BeansException {
		if (StringUtils.hasText(encodedView)) {
			if (encodedView.startsWith(CLASS_PREFIX)) {
				String className = encodedView.substring(CLASS_PREFIX.length());
				Class clazz = (Class)new TextToClass().convert(className);
				return (ViewDescriptorCreator)BeanUtils.instantiateClass(clazz);
			}
			else if (encodedView.startsWith(REDIRECT_PREFIX)) {
				String viewInfo = encodedView.substring(REDIRECT_PREFIX.length());
				return new RedirectViewDescriptorCreator(viewInfo);
			}
		}
		return new SimpleViewDescriptorCreator(encodedView);
	}
}