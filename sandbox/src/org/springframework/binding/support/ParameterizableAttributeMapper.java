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
package org.springframework.binding.support;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.binding.AttributeMapper;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.util.Assert;

/**
 * Generic attributes mapper implementation that allows mappings to be
 * configured programatically or in a Spring application context.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td>mappings</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping <i>input data </i> from a source
 * attributes collection to a target attributes collection. The provided list
 * contains the names of the attributes in the source to pass to the target for
 * access. The same name is used in both the source and target model.</td>
 * </tr>
 * <tr>
 * <td>mappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping <i>input data </i> from a source
 * attributes collection to a target attributes collection. The keys in given
 * map are the names of entries in the source model that will be mapped. The
 * value associated with a key is the name of the entry that will be placed in
 * the target model.</td>
 * </tr>
 * </table>
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propertyPath".
 * </p>
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableAttributeMapper implements AttributeMapper, Serializable {

	private Collection mappings = Collections.EMPTY_SET;

	private boolean mapMissingAttributesToNull = false;

	public ParameterizableAttributeMapper() {

	}

	public ParameterizableAttributeMapper(Mapping mapping) {
		setMappings(new Mapping[] { mapping });
	}

	public ParameterizableAttributeMapper(Mapping[] mappings) {
		setMappings(mappings);
	}

	public ParameterizableAttributeMapper(Collection mappings) {
		setMappingsCollection(mappings);
	}

	public ParameterizableAttributeMapper(Map mappingsMap) {
		setMappingsMap(mappingsMap);
	}

	/**
	 * Set the mappings for this attribute mapper.
	 * @param mappings The mappings
	 */
	public void setMappings(Mapping[] mappings) {
		this.mappings = new HashSet(Arrays.asList(mappings));
	}

	/**
	 * Set the mappings that will be executed when mapping model data from one
	 * attributes collection to another. Each list item must be a String, a
	 * Mapping object, a List, or a Map. If the list item is a simple String
	 * value, the attribute will be mapped as having the same name in the parent
	 * flow and in the sub flow. If the list item is a Map, each map entry must
	 * be a String key naming the attribute in the source model, and a String
	 * value naming the attribute in the target model. If the list item is
	 * itself a List, then that list is itself evaluated recursively, and must
	 * itself contain Strings, Mapping objects, Lists, or Maps.
	 * @param mappings The mappings
	 */
	public void setMappingsCollection(Collection mappings) {
		this.mappings = new HashSet();
		putCollectionMappings(this.mappings, mappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from one
	 * attributes collection to another.
	 * @link ParameterizableAttributesMapper#setMappings(List) with a List
	 *       containing one item which is a Map. Each map entry must be a String
	 *       key naming the attribute in the parent flow, and a String value
	 *       naming the attribute in the child flow.
	 * @param mappingsMap The mappings map
	 */
	public void setMappingsMap(Map mappingsMap) {
		this.mappings = new HashSet();
		putMapMappings(this.mappings, mappingsMap);
	}

	/**
	 * Internal worker function to convert given mappingsList to a simple
	 * mappings map.
	 */
	private void putCollectionMappings(Collection mappings, Collection mappingsList) {
		Iterator it = mappingsList.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (element instanceof Mapping) {
				mappings.add(element);
			}
			else if (element instanceof Collection) {
				putCollectionMappings(mappings, (Collection)element);
			}
			else if (element instanceof Map) {
				putMapMappings(mappings, (Map)element);
			}
			else {
				Assert.isInstanceOf(String.class, element, "ParameterizableFlowModelMapper key or value: ");
				mappings.add(new Mapping((String)element));
			}
		}
	}

	private void putMapMappings(Collection mappings, Map mappingsMap) {
		// we could just add the map into the other, but want to
		// validate key and value types!
		Iterator itMap = mappingsMap.entrySet().iterator();
		while (itMap.hasNext()) {
			Map.Entry entry = (Map.Entry)itMap.next();
			Assert.isInstanceOf(String.class, entry.getKey(), "ParameterizableAttributeMapper key: ");
			Assert.isInstanceOf(String.class, entry.getValue(), "ParameterizableAttributeMapper value: ");
			mappings.add(new Mapping((String)entry.getKey(), (String)entry.getValue()));
		}
	}

	/**
	 * Set whether or not missing attributes in the model should be mapped to a
	 * null value or shouldn't be mapped at all.
	 */
	public void setMapMissingAttributesToNull(boolean toNull) {
		this.mapMissingAttributesToNull = toNull;
	}

	/**
	 * Get whether or not missing attributes in the model should be mapped to a
	 * null value or shouldn't be mapped at all.
	 */
	public boolean isMapMissingAttributesToNull() {
		return this.mapMissingAttributesToNull;
	}

	/**
	 * Map data from one map to another map using specified mappings.
	 */
	public void map(AttributeSource source, MutableAttributeSource target) {
		if (mappings != null) {
			Iterator it = this.mappings.iterator();
			while (it.hasNext()) {
				Mapping mapping = (Mapping)it.next();
				mapping.map(source, target, this.mapMissingAttributesToNull);
			}
		}
	}
	
	public String toString() {
		return new ToStringBuilder(this).append("mappings", mappings).toString();
	}
}