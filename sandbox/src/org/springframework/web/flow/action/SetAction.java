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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.convert.ConverterLocator;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.util.Assert;
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

	private ConverterLocator converterLocator;

	/**
	 * Creates a set action with an initially empty mappings list.
	 */
	public SetAction() {
	}

	/**
	 * Create a set action with the specified string mapping, where the string
	 * is a request parameter name that should be mapped as a string attribute
	 * in the flow model with the same name.
	 * <p>
	 * If the type to convert should be something other than a string, it maybe
	 * be encoded within the mappings argument with a comma delimiter: e.g.
	 * <code>myAttribute,java.lang.Short</code>
	 * @param mapping the string mapping (e.g. "postalCode")
	 */
	public SetAction(String mapping) {
		setMapping(mapping);
	}

	/**
	 * Create a set action with the specified string mappings, where each string
	 * is a request parameter name that should be mapped as a string attribute in
	 * the flow model with the same name.
	 * @param mappings the string mappings
	 */
	public SetAction(String[] mappings) {
		setMappings(mappings);
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
	 * Set the type converter registry
	 * @param registry the registry
	 */
	public void setConverterLocator(ConverterLocator registry) {
		this.converterLocator = registry;
	}

	/**
	 * Set the encoded string mapping.
	 * @param mapping the mapping
	 */
	public void setMapping(String mapping) {
		setMappings(new String[] { mapping });
	}

	/**
	 * Set the encoded string mappings.
	 * @param mappings the mappings
	 */
	public void setMappings(String[] mappings) {
		ClassEditor classEditor = new ClassEditor();
		Mapping[] maps = new Mapping[mappings.length];
		for (int i = 0; i < mappings.length; i++) {
			String[] encodedMapping = StringUtils.commaDelimitedListToStringArray(mappings[i]);
			if (encodedMapping.length == 2) {
				classEditor.setAsText(encodedMapping[1]);
				Class clazz = (Class)classEditor.getValue();
				maps[i] = new Mapping(encodedMapping[0], getConverterLocator().getConverter(String.class, clazz));
			}
			else {
				maps[i] = new Mapping(encodedMapping[0]);
			}
		}
		setMappings(maps);
	}

	protected ConverterLocator getConverterLocator() {
		Assert.notNull(this.converterLocator, "The converterLocator property was request but is not set");
		return this.converterLocator;
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
	 * Set the mappings map.
	 * @param mappingsMap the mappings map
	 */
	public void setMappingsMap(Map mappingsMap) {
		ClassEditor classEditor = new ClassEditor();
		Mapping[] maps = new Mapping[mappingsMap.size()];
		Iterator it = mappingsMap.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String sourceAttributeName = (String)entry.getKey();
			String[] encodedMapping = StringUtils.commaDelimitedListToStringArray((String)entry.getValue());
			if (encodedMapping.length == 2) {
				classEditor.setAsText(encodedMapping[1]);
				Class clazz = (Class)classEditor.getValue();
				maps[i] = new Mapping(sourceAttributeName, encodedMapping[0], getConverterLocator().getConverter(
						String.class, clazz));
			}
			else {
				maps[i] = new Mapping(sourceAttributeName, encodedMapping[0]);
			}
			i++;
		}
		setMappings(maps);
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