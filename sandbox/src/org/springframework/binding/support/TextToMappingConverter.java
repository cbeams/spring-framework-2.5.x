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
package org.springframework.binding.support;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class TextToMappingConverter extends AbstractConverter {

	private ConversionService conversionService;

	public TextToMappingConverter(ConversionService conversionService) {
		setConversionService(conversionService);
	}

	/**
	 * Set the type converter registry
	 * @param registry the registry
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	protected ConversionService getConversionService() {
		Assert.notNull(this.conversionService, "The converterLocator property was request but is not set");
		return this.conversionService;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Mapping.class };
	}

	protected Object doConvert(Object o, Class targetClass) throws Exception {
		String[] mappingInfo = StringUtils.commaDelimitedListToStringArray((String)o);
		String[] sourceTarget = StringUtils.delimitedListToStringArray(mappingInfo[0], "->");
		if (mappingInfo.length == 2) {
			Class clazz = (Class)getConversionService().getConversionExecutor(String.class, Class.class).call(mappingInfo[1]);
			if (sourceTarget.length == 2) {
				return new Mapping(sourceTarget[0], sourceTarget[1], getConversionService().getConversionExecutor(String.class,
						clazz));
			}
			else {
				return new Mapping(sourceTarget[0], getConversionService().getConversionExecutor(String.class, clazz));
			}
		}
		else {
			if (sourceTarget.length == 2) {
				return new Mapping(sourceTarget[0], sourceTarget[1]);
			}
			else {
				return new Mapping(sourceTarget[0]);
			}
		}
	}
}