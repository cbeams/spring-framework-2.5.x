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
 * Maps parameters in an event to attributes <i>set</i> in flow scope.
 * This action always returns the
 * {@link org.springframework.web.flow.action.AbstractAction#success() success}
 * event.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>Name </b></td>
 * <td><b>Default </b></td>
 * <td><b>Description </b></td>
 * </tr>
 * <tr>
 * <td>mapping(s)</td>
 * <td><i>null</i></td>
 * <td>The mappings executed by this action.</td>
 * </tr>
 * <tr>
 * <td>eventParameterMapper</td>
 * <td><i>null</i></td>
 * <td>The custom mapping strategy used by this action.</td>
 * </tr>
 * </table>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class EventParameterMapperAction extends AbstractAction {

	private AttributeMapper eventParameterMapper;

	/**
	 * Creates a new action with an initially empty mappings list.
	 */
	public EventParameterMapperAction() {
	}

	/**
	 * Create a new action with the specified mapping.
	 * @param mapping the mapping
	 */
	public EventParameterMapperAction(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Create a new action with the specified mappings.
	 * @param mappings the mappings
	 */
	public EventParameterMapperAction(Mapping[] mappings) {
		setMappings(mappings);
	}

	/**
	 * Set the single mapping for this action.
	 * @param mapping the mapping
	 */
	public void setMapping(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Set the mappings for this action.
	 * @param mappings the mappings
	 */
	public void setMappings(Mapping[] mappings) {
		setEventParameterMapper(new ParameterizableAttributeMapper(mappings));
	}

	/**
	 * Set to completely customize the attribute mapper strategy.
	 * @param mapper the mapping strategy
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