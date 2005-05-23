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

import java.util.Collections;
import java.util.Map;

import org.springframework.binding.AttributeMapper;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;

/**
 * Maps properties of the request context.  This action always returns the
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
 * <td>attributeMapper</td>
 * <td><i>null</i></td>
 * <td>The custom mapping strategy used by this action.</td>
 * </tr>
 * </table>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class AttributeMapperAction extends AbstractAction {

	private AttributeMapper attributeMapper;

	/**
	 * Creates a new action with an initially empty mappings list.
	 */
	public AttributeMapperAction() {
	}

	/**
	 * Create a new action with the specified mapping.
	 * @param mapping the mapping
	 */
	public AttributeMapperAction(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Create a new action with the specified mappings.
	 * @param mappings the mappings
	 */
	public AttributeMapperAction(Mapping[] mappings) {
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
		setAttributeMapper(new ParameterizableAttributeMapper(mappings));
	}

	/**
	 * Set to completely customize the attribute mapper strategy.
	 * @param mapper the mapping strategy
	 */
	public void setAttributeMapper(AttributeMapper mapper) {
		this.attributeMapper = mapper;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		if (attributeMapper != null) {
			this.attributeMapper.map(context, context, getMappingContext(context));
		}
		return success();
	}
	
	protected Map getMappingContext(RequestContext context) {
		return Collections.EMPTY_MAP;
	}
}