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
package org.springframework.web.flow.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.MutableFlowModel;

/**
 * Maps parameters in the http servlet request to attributes <i>set</i> in the
 * flow model.
 * 
 * @author Keith Donald
 */
public class SetAction extends AbstractAction {

	private AttributeMapper requestParameterMapper;

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
		this.requestParameterMapper = new ParameterizableAttributeMapper(mappings);
	}

	/**
	 * Set to completely customize the attribute mapper strategy.
	 * @param mapper The strategy
	 */
	public void setRequestParameterMapper(AttributeMapper mapper) {
		this.requestParameterMapper = mapper;
	}

	protected String doExecuteAction(HttpServletRequest request, HttpServletResponse response, MutableFlowModel model)
			throws Exception {
		if (requestParameterMapper != null) {
			this.requestParameterMapper.map(new RequestParameterAttributeAccessorAdapter(request), model);
		}
		return success();
	}

	/**
	 * Adapts request parameter access to the attribute accessor interface.
	 * @author Keith Donald
	 */
	public static class RequestParameterAttributeAccessorAdapter implements AttributeAccessor {

		private HttpServletRequest request;

		/**
		 * Create a new request parameter attribute accessor.
		 */
		public RequestParameterAttributeAccessorAdapter(HttpServletRequest request) {
			this.request = request;
		}

		public boolean containsAttribute(String attributeName) {
			return StringUtils.hasText(request.getParameter(attributeName));
		}

		public Object getAttribute(String attributeName) {
			return request.getParameter(attributeName);
		}
	}
}