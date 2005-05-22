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

import org.springframework.binding.convert.ConversionService;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.State;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.ViewDescriptorCreator;
import org.springframework.web.flow.execution.ServiceLookupException;

/**
 * Simple helper adapter for the flow service locator interface. For testing.
 * 
 * @author Keith Donald
 */
public class FlowServiceLocatorAdapter implements FlowServiceLocator {
	
	public ConversionService getConversionService() {
		throw new UnsupportedOperationException();
	}

	public Action createAction(Class implementationClass,
			AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow createFlow(AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow createFlow(Class implementationClass, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public FlowAttributeMapper createFlowAttributeMapper(
			Class attributeMapperImplementationClass, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public State createState(Class implementationClass,
			AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Transition createTransition(Class implementationClass,
			AutowireMode autowireMode) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public TransitionCriteria createTransitionCriteria(
			String encodedCriteria, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public ViewDescriptorCreator createViewDescriptorCreator(
			String encodedView, AutowireMode autowireMode)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Action getAction(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Action getAction(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public FlowAttributeMapper getFlowAttributeMapper(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public FlowAttributeMapper getFlowAttributeMapper(String id)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public State getState(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public State getState(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Transition getTransition(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Transition getTransition(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow getFlow(Class implementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
	
	public Flow getFlow(String id, Class requiredFlowBuilderImplementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Flow getFlow(String id) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
}