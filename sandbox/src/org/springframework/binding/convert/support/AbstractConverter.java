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

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.Converter;

/**
 * Base class for converters -- provided as a convenience to implementors.
 * @author Keith Donald
 */
public abstract class AbstractConverter implements Converter {

	public Object convert(Object source) throws ConversionException {
		return convert(source, getTargetClasses()[0]);
	}
	
	public Object convert(Object source, Class targetClass) throws ConversionException {
		try {
			return doConvert(source, targetClass);
		}
		catch (ConversionException e) {
			throw e;
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract Object doConvert(Object source, Class targetClass) throws Exception;

}