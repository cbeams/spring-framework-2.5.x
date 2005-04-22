package org.springframework.web.flow.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.execution.ClientContinuationFlowExecutionStorage;
import org.springframework.web.flow.execution.FlowExecutionStorage;
import org.springframework.web.flow.execution.servlet.HttpSessionContinuationFlowExecutionStorage;
import org.springframework.web.flow.execution.servlet.HttpSessionFlowExecutionStorage;

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
