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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.AttributeAccessor;
import org.springframework.web.flow.AttributeMapper;
import org.springframework.web.flow.MutableFlowModel;
import org.springframework.web.flow.support.ParameterizableAttributeMapper;

/**
 * Maps parameters in the http servlet request to attributes in the flow model.
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
	 * Create a set action with the specified string mappings, where each string
	 * is a request parameter name that should be mapped as an attribute in the
	 * flow model.
	 * @param mappings the string mappings
	 */
	public SetAction(String[] mappings) {
		setRequestParameterMappings(Arrays.asList(mappings));
	}

	/**
	 * Create a set action with the specified mappings, where each collection
	 * element is a request parameter name that should be mapped as an attribute
	 * in the flow model.
	 * @param mappings the mappings
	 */
	public SetAction(Collection mappings) {
		setRequestParameterMappings(mappings);
	}

	/**
	 * Create a set action with the specified mappings, where each map entry
	 * element is a request parameter name that should be mapped as an attribute
	 * with a particular name in the flow model.
	 * @param mappingsMap the mappings map
	 */
	public SetAction(Map mappingsMap) {
		setRequestParameterMappingsMap(mappingsMap);
	}

	public void setRequestParameterMappings(Collection mappings) {
		this.requestParameterMapper = new ParameterizableAttributeMapper(mappings);
	}

	public void setRequestParameterMappingsMap(Map mappings) {
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
			this.requestParameterMapper.map(new RequestParameterAccessor(request), model);
		}
		return success();
	}

	public static class RequestParameterAccessor implements AttributeAccessor {
		private HttpServletRequest request;

		public RequestParameterAccessor(HttpServletRequest request) {
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