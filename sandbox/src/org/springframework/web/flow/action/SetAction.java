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

import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.TypeConverter;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.MutableFlowModel;

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
	 * Create a set action with the specified string mapping, where the string
	 * is a request parameter name that should be mapped as a string attribute
	 * in the flow model with the same name.
	 * @param mapping the string mapping (e.g. "postalCode");
	 */
	public SetAction(String mapping) {
		this(new String[] { mapping });
	}

	/**
	 * Create a set action with the specified string mapping, where the string
	 * is a request parameter name that should be mapped as an attribute in the
	 * flow model, converted by the specified type converter.
	 * @param mapping the string mapping
	 * @param valueTypeConverter the type converter to apply to the source
	 *        attribute value
	 */
	public SetAction(String mapping, TypeConverter valueTypeConverter) {
		this(new Mapping(mapping, valueTypeConverter));
	}

	/**
	 * Create a set action with the specified mapping.
	 * @param mapping the mapping
	 */
	public SetAction(Mapping mapping) {
		this(new Mapping[] { mapping });
	}

	/**
	 * Create a set action with the specified string mappings, where each string
	 * is a request parameter name that should be mapped as a strin attribute in
	 * the flow model with the same name.
	 * @param mappings the string mappings
	 */
	public SetAction(String[] mappings) {
		setRequestParameterMappings(Arrays.asList(mappings));
	}

	/**
	 * Create a set action with the specified string mappings, where each string
	 * is a request parameter name that should be mapped as an attribute in the
	 * flow model, applying the specified type converters.
	 * @param mappings the string mappings
	 * @param valueTypeConverters the type converters
	 */
	public SetAction(String[] mappings, TypeConverter[] valueTypeConverters) {
		Mapping[] maps = new Mapping[mappings.length];
		for (int i = 0; i < mappings.length; i++) {
			maps[i] = new Mapping(mappings[i], valueTypeConverters[i]);
		}
		setRequestParameterMappings(maps);
	}

	/**
	 * Create a set action with the specified mappings.
	 * @param mappings The mappings
	 */
	public SetAction(Mapping[] mappings) {
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

	/**
	 * @param mappings
	 */
	public void setRequestParameterMappings(Mapping[] mappings) {
		this.requestParameterMapper = new ParameterizableAttributeMapper(mappings);
	}

	/**
	 * @param mappings
	 */
	public void setRequestParameterMappings(Collection mappings) {
		this.requestParameterMapper = new ParameterizableAttributeMapper(mappings);
	}

	/**
	 * @param mappings
	 */
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
			this.requestParameterMapper.map(new RequestParameterAccessorAdapter(request), model);
		}
		return success();
	}

	/**
	 * Adapts request parameter access to the attribute accessor interface.
	 * @author Keith Donald
	 */
	public static class RequestParameterAccessorAdapter implements AttributeAccessor {
		private HttpServletRequest request;

		public RequestParameterAccessorAdapter(HttpServletRequest request) {
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