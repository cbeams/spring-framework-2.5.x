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
package org.springframework.web.flow.config;

import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.ServiceLookupException;

/**
 * Simple helper adapter for flow service locator interface.  For testing.
 * @author Keith Donald
 */
public class FlowServiceLocatorAdapter implements FlowServiceLocator {

	public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Flow getFlow(String flowDefinitionId, Class requiredFlowBuilderImplementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Flow getFlow(Class flowDefinitionImplementationClass) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Action getAction(String actionId) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Action getAction(Class actionImplementationClass) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public FlowAttributeMapper getFlowModelMapper(String flowModelMapperId) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public FlowAttributeMapper getFlowModelMapper(Class flowModelMapperImplementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
}