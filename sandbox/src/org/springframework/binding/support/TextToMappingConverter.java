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

/**
 * Converts a text-encoded representation of a <code>Mapping</code> object to a valid instance.
 * @author Keith Donald
 */
public class TextToMappingConverter extends AbstractConverter {

	private ConversionService conversionService;

	public TextToMappingConverter(ConversionService conversionService) {
		setConversionService(conversionService);
	}

	/**
	 * Set the type conversion service
	 * @param conversionService the service
	 */
	public void setConversionService(ConversionService conversionService) {
		Assert.notNull(this.conversionService, "The conversionService property is required");
		this.conversionService = conversionService;
	}

	protected ConversionService getConversionService() {
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
		String[] sourceTarget = StringUtils.delimitedListToStringArray((String) source, "->");
		if (sourceTarget.length == 1) {
			// just target mapping info is specified
			String[] targetMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[0]);
			String sourceAttributeName = targetMappingInfo[0];
			String targetAttributeName = targetMappingInfo[0];
			Class targetAttributeClass = null;
			if (targetMappingInfo.length == 2) {
				targetAttributeClass = (Class) getConversionService().getConversionExecutor(String.class, Class.class)
						.call(targetMappingInfo[1]);
			}
			if (targetAttributeClass != null) {
				return new Mapping(sourceAttributeName, targetAttributeName, getConversionService()
						.getConversionExecutor(String.class, targetAttributeClass));
			}
			else {
				return new Mapping(sourceAttributeName, targetAttributeName);
			}
		}
		else {
			// source and target mapping info is specified
			String[] sourceMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[0]);
			String sourceAttributeName = sourceMappingInfo[0];
			Class sourceAttributeClass = String.class;
			if (sourceMappingInfo.length == 2) {
				sourceAttributeClass = (Class) getConversionService().getConversionExecutor(String.class, Class.class)
						.call(sourceMappingInfo[1]);
			}
			String[] targetMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[1]);
			String targetAttributeName = targetMappingInfo[0];
			Class targetAttributeClass = String.class;
			if (targetMappingInfo.length == 2) {
				targetAttributeClass = (Class) getConversionService().getConversionExecutor(String.class, Class.class)
						.call(targetMappingInfo[1]);
			}
			if (!sourceAttributeClass.equals(targetAttributeClass)) {
				return new Mapping(sourceAttributeName, targetAttributeName, getConversionService()
						.getConversionExecutor(sourceAttributeClass, targetAttributeClass));
			}
			else {
				return new Mapping(sourceAttributeName, targetAttributeName);
			}
		}
	}
}