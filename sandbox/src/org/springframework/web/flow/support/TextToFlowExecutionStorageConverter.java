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
package org.springframework.web.flow.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.execution.ClientContinuationFlowExecutionStorage;
import org.springframework.web.flow.execution.FlowExecutionStorage;
import org.springframework.web.flow.execution.servlet.HttpSessionContinuationFlowExecutionStorage;
import org.springframework.web.flow.execution.servlet.HttpSessionFlowExecutionStorage;

/**
 * Converts a string representation of a FlowExecutionStorage strategy
 * implementation into a valid instance.
 * 
 * @author Keith Donald
 */
public class TextToFlowExecutionStorageConverter extends AbstractConverter {

	public Class[] getSourceClasses() {
		// TODO Auto-generated method stub
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		// TODO Auto-generated method stub
		return new Class[] { FlowExecutionStorage.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedStorage = (String)source;
		if (!StringUtils.hasText(encodedStorage) || encodedStorage.equalsIgnoreCase("default")
				|| encodedStorage.equalsIgnoreCase("session")) {
			return new HttpSessionFlowExecutionStorage();
		}
		else if (encodedStorage.equalsIgnoreCase("sessionContinuation")) {
			return new HttpSessionContinuationFlowExecutionStorage();
		}
		else if (encodedStorage.equalsIgnoreCase("clientContinuation")) {
			return new ClientContinuationFlowExecutionStorage();
		}
		else {
			throw new ConversionException(source, FlowExecutionStorage.class);
		}
	}
}
