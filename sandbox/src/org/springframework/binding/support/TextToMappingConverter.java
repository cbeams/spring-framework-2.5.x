/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		//format: <sourceAttributeName>[,class][->targetAttributeName[,class]]
		String[] sourceTarget = StringUtils.delimitedListToStringArray((String)source, "->");
		String[] sourceMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[0]);
		String sourceAttributeName = sourceMappingInfo[0];
		String targetAttributeName = sourceMappingInfo[0];
		Class sourceAttributeClass = null;
		Class targetAttributeClass = null;
		if (sourceMappingInfo.length == 2) {
			sourceAttributeClass = (Class)getConversionService().getConversionExecutor(String.class, Class.class).call(
					sourceMappingInfo[1]);
			targetAttributeClass = String.class;
		}
		if (sourceTarget.length == 2) {
			String[] targetMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[1]);
			targetAttributeName = targetMappingInfo[0];
			if (targetMappingInfo.length == 2) {
				targetAttributeClass = (Class)getConversionService().getConversionExecutor(String.class, Class.class)
						.call(targetMappingInfo[1]);
			}
		}
		if (sourceAttributeClass != null) {
			return new Mapping(sourceAttributeName, targetAttributeName, getConversionService().getConversionExecutor(
					sourceAttributeClass, targetAttributeClass));
		}
		else {
			return new Mapping(sourceAttributeName, targetAttributeName);
		}
	}
}