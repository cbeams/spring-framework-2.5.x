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
package org.springframework.web.flow.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.binding.support.Mapping;
import org.springframework.binding.support.ParameterizableAttributeMapper;
import org.springframework.web.flow.FlowAttributeMapper;

/**
 * Generic flow attribute mapper implementation that allows mappings to be
 * configured in a declarative fashion.
 * <p>
 * <b>Exposed configuration properties:</b> <br>
 * <table border="1">
 * <tr>
 * <td>inputMapper</td>
 * <td><i>null</i></td>
 * <td>The AttributesMapper strategy responsible for mapping starting
 * subflow input attributes from a suspending parent flow.</td>
 * </tr>
 * <tr>
 * <td>inputMapping(s)</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping <i>input data</i> from the parent
 * flow to a newly spawned sub flow. Each list item must be a String, a
 * Map or a List. If the list item is a simple String value, the attribute
 * will be mapped as having the same name in the parent flow and in the sub
 * flow. If the list item is a Map, each map entry must be a String key
 * naming the attribute in the parent flow, and a String value naming the
 * attribute in the child flow. If the list item is itself a List, then that
 * list is itself evaluated recursively, and must itself contain Strings,
 * Lists or Maps.</td>
 * </tr>
 * <tr>
 * <td>inputMappingsMap</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping <i>input data</i> from the parent
 * flow to a newly spawned sub flow. The keys in given map are the names
 * of entries in the parent model that will be mapped. The value associated
 * with a key is the name of the target entry that will be placed in the
 * subflow model.</td>
 * </tr>
 * <td>outputMapper</td>
 * <td><i>null</i></td>
 * <td>The AttributesMapper strategy responsible for mapping ending subflow
 * output attributes to a resuming parent flow as output.</td>
 * </tr>
 * <tr>
 * <td>outputMapping(s)</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping subflow <i>output</i> data back to
 * the parent flow (once the subflow ends and the parent flow resumes).
 * Each list item must be a String, a List or a Map. If the list item is
 * a simple String value, the attribute will be mapped as having the same
 * name in the parent flow and in the child flow. If the list item is a Map,
 * each map entry must be a String key naming the attribute in the sub flow,
 * and a String value naming the attribute in the parent flow. If the list
 * item is itself a List, then that list is itself evaluated recursively,
 * and must itself contain Strings, Lists or Maps.</td>
 * </tr>
 * <tr>
 * <td>outputMappingsMap</td>
 * <td><i>empty</i></td>
 * <td>Mappings executed when mapping subflow <i>output</i> data back to the
 * parent flow (once the subflow ends and the parent flow resumes). The keys
 * in given map are the names of entries in the subflow model that will be mapped.
 * The value associated with a key is the name of the target entry that will
 * be placed in the parent flow model.</td>
 * </tr>
 * </table>
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName").
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableFlowAttributeMapper implements FlowAttributeMapper, Serializable {

	protected final Log logger = LogFactory.getLog(getClass());;

	private AttributeMapper inputMapper;

	private AttributeMapper outputMapper;

	/**
	 * Set the input mapping to use when mapping properties in a parent
	 * flow's scope to a sub flow scope. This is a convenience method
	 * for setting a single mapping.
	 * @param mapping the mapping object
	 */
	public void setInputMapping(Mapping mapping) {
		Collection mappings = new ArrayList(1);
		mappings.add(mapping);
		setInputMappings(mappings);
	}

	/**
	 * Set the mappings that will be executed when mapping properties in
	 * a parent flow's scope to a sub flow scope. Each list item must be
	 * a String, a Mapping, a List, or a Map. If the list item is a simple
	 * String value, the attribute will be mapped as having the same name
	 * in the parent flow and in the sub flow. If the list item is a Map,
	 * each map entry must be a String key naming the attribute in the
	 * parent flow, and a String value naming the attribute in the child flow.
	 * If the list item is itself a List, then that list is itself evaluated
	 * recursively, and must itself contain Strings, Lists or Maps.
	 * @param inputMappings the input mappings
	 */
	public void setInputMappings(Collection inputMappings) {
		this.inputMapper = new ParameterizableAttributeMapper(inputMappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data to the
	 * sub flow. This method is provided as a configuration convenience.
	 * @param inputMappings the input mappings
	 * @see #setInputMappings(Collection) with a Collection containing one
	 *      item which is a Map. Each map entry must be a String key naming
	 *      the attribute in the parent flow, and a String value naming the
	 *      attribute in the child flow.
	 */
	public void setInputMappingsMap(Map inputMappings) {
		this.inputMapper = new ParameterizableAttributeMapper(inputMappings);
	}

	/**
	 * Set the AttributesMapper strategy responsible for mapping starting
	 * subflow input attributes from a suspending parent flow.
	 * @param mapper the mapper
	 */
	public void setInputMapper(AttributeMapper mapper) {
		this.inputMapper = mapper;
	}

	/**
	 * Set the output mapping to use when mapping properties in sub flow's
	 * scope back to a resuming parent flow. This is a convenience method for
	 * setting a single mapping.
	 * @param mapping the mapping object
	 */
	public void setOutputMapping(Mapping mapping) {
		Collection mappings = new ArrayList(1);
		mappings.add(mapping);
		setOutputMappings(mappings);
	}
	
	/**
	 * Set the mappings that will be executed when mapping model data from
	 * the sub flow. Each list item must be a String, Mapping, a List, or a
	 * Map. If the list item is a simple String value, the attribute will be
	 * mapped as having the same name in the parent flow and in the child flow.
	 * If the list item is a Map, each map entry must be a String key naming
	 * the attribute in the sub flow, and a String value naming the attribute
	 * in the parent flow. If the list item is itself a List, then that list
	 * is itself evaluated recursively, and must itself contain Strings,
	 * Mappings, Lists, or Maps.
	 * @param outputMappings the output mappings
	 */
	public void setOutputMappings(Collection outputMappings) {
		this.outputMapper = new ParameterizableAttributeMapper(outputMappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from the
	 * sub flow. This method is provided as a configuration convenience.
	 * @param outputMappings the output mappings
	 * @see #setOutputMappings(Collection) with a Collection containing one
	 *      item which is a Map. Each map entry must be a String key naming the
	 *      attribute in the sub flow, and a String value naming the attribute
	 *      in the parent flow.
	 */
	public void setOutputMappingsMap(Map outputMappings) {
		this.outputMapper = new ParameterizableAttributeMapper(outputMappings);
	}

	/**
	 * Set the AttributesMapper strategy responsible for mapping ending subflow
	 * output attributes to a resuming parent flow as output.
	 * @param mapper the mapper
	 */
	public void setOutputMapper(AttributeMapper mapper) {
		this.outputMapper = mapper;
	}

	public Map createSubFlowInputAttributes(AttributeSource parentFlowModel) {
		if (this.inputMapper != null) {
			Map subFlowInputAttributes = new HashMap();
			this.inputMapper.map(parentFlowModel, new MapAttributeSource(subFlowInputAttributes));
			return Collections.unmodifiableMap(subFlowInputAttributes);
		}
		else {
			return Collections.EMPTY_MAP;
		}
	}

	public void mapSubFlowOutputAttributes(AttributeSource subFlowModel, MutableAttributeSource parentFlowModel) {
		if (this.outputMapper != null) {
			this.outputMapper.map(subFlowModel, parentFlowModel);
		}
	}
}