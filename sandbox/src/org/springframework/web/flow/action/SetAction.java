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

import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.TypeConverter;
import org.springframework.binding.TypeConverterRegistry;
import org.springframework.binding.TypeConverters;
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

	private TypeConverterRegistry typeConverterRegistry;

	/**
	 * Creates a set action with an initially empty mappings list.
	 */
	public SetAction() {

	}

	/**
	 * Create a set action with the specified string mapping, where the string
	 * is a request parameter name that should be mapped as a string attribute
	 * in the flow model with the same name.
	 * 
	 * If the type to convert should be something other than a string, it maybe
	 * be encoded within the mappings argument with a comma delimiter: e.g.
	 * <code>myAttribute,java.lang.Short</code>
	 * @param mapping the string mapping (e.g. "postalCode");
	 */
	public SetAction(String mapping) {
		setMapping(mapping);
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
		setMapping(mapping, valueTypeConverter);
	}

	/**
	 * Create a set action with the specified mapping.
	 * @param mapping the mapping
	 */
	public SetAction(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	/**
	 * Create a set action with the specified string mappings, where each string
	 * is a request parameter name that should be mapped as a strin attribute in
	 * the flow model with the same name.
	 * @param mappings the string mappings
	 */
	public SetAction(String[] mappings) {
		setMappings(mappings);
	}

	/**
	 * Create a set action with the specified string mappings, where each string
	 * is a request parameter name that should be mapped as an attribute in the
	 * flow model, applying the specified type converters.
	 * @param mappings the string mappings
	 * @param valueTypeConverters the type converters
	 */
	public SetAction(String[] mappings, TypeConverter[] valueTypeConverters) {
		setMappings(mappings, valueTypeConverters);
	}

	/**
	 * Create a set action with the specified mappings.
	 * @param mappings The mappings
	 */
	public SetAction(Mapping[] mappings) {
		setMappings(mappings);
	}

	public void setMapping(String mapping) {
		setMappings(new String[] { mapping });
	}

	public void setMapping(String mapping, TypeConverter valueTypeConverter) {
		setMappings(new String[] { mapping }, new TypeConverter[] { valueTypeConverter });
	}

	public void setMappings(String[] mappings) {
		ClassEditor classEditor = new ClassEditor();
		Mapping[] maps = new Mapping[mappings.length];
		for (int i = 0; i < mappings.length; i++) {
			String[] encodedMapping = StringUtils.commaDelimitedListToStringArray(mappings[i]);
			if (encodedMapping.length == 2) {
				classEditor.setAsText(encodedMapping[i]);
				Class clazz = (Class)classEditor.getValue();
				maps[i] = new Mapping(encodedMapping[0], getTypeConverterRegistry().getTypeConverter(clazz));
			}
			else {
				maps[i] = new Mapping(mappings[i]);
			}
		}
		setMappings(maps);
	}

	public void setTypeConverterRegistry(TypeConverterRegistry registry) {
		this.typeConverterRegistry = registry;
	}

	protected TypeConverterRegistry getTypeConverterRegistry() {
		synchronized (this) {
			if (this.typeConverterRegistry == null) {
				this.typeConverterRegistry = TypeConverters.instance();
			}
		}
		return this.typeConverterRegistry;
	}

	public void setMappings(String[] mappings, TypeConverter[] valueTypeConverters) {
		Mapping[] maps = new Mapping[mappings.length];
		for (int i = 0; i < mappings.length; i++) {
			maps[i] = new Mapping(mappings[i], valueTypeConverters[i]);
		}
		setMappings(maps);
	}

	/**
	 * @param mappings
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