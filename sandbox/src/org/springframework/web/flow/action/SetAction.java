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
package org.springframework.web.flow.action;

import org.springframework.binding.AttributeMapper;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;

/**
 * Maps parameters in an event to attributes <i>set</i> into flow scope.
 * @author Keith Donald
 */
public class SetAction extends AbstractAction {

	private AttributeMapper eventParameterMapper;

	/**
	 * Creates a set action with an initially empty mappings list.
	 */
	public SetAction() {
	}

	/**
	 * Create a set action with the specified mapping.
	 * @param mapping the mapping
	 */
	public SetAction(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Create a set action with the specified mappings.
	 * @param mappings the mappings
	 */
	public SetAction(Mapping[] mappings) {
		setMappings(mappings);
	}

	/**
	 * Set the single mapping for this set action.
	 * @param mapping the mapping
	 */
	public void setMapping(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Set the mappings for this set action.
	 * @param mappings the mappings
	 */
	public void setMappings(Mapping[] mappings) {
		this.eventParameterMapper = new ParameterizableAttributeMapper(mappings);
	}

	/**
	 * Set to completely customize the attribute mapper strategy.
	 * @param mapper The strategy
	 */
	public void setEventParameterMapper(AttributeMapper mapper) {
		this.eventParameterMapper = mapper;
	}

	protected Event doExecuteAction(RequestContext context) throws Exception {
		if (eventParameterMapper != null) {
			this.eventParameterMapper.map(context.getLastEvent(), context.getFlowScope());
		}
		return success();
	}
}