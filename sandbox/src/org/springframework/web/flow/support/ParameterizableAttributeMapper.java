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
package org.springframework.web.flow.support;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.web.flow.AttributeAccessor;
import org.springframework.web.flow.AttributeMapper;
import org.springframework.web.flow.AttributeSetter;

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
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName").
 * </p>
 * @author Erwin Vervaet
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class ParameterizableAttributeMapper implements AttributeMapper, Serializable {

	protected static final Log logger = LogFactory.getLog(ParameterizableAttributeMapper.class);

	private Map mappings = Collections.EMPTY_MAP;

	private boolean mapMissingAttributesToNull = false;

	public ParameterizableAttributeMapper() {

	}

	public ParameterizableAttributeMapper(Collection mappings) {
		setMappings(mappings);
	}

	public ParameterizableAttributeMapper(Map mappingsMap) {
		setMappingsMap(mappingsMap);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from one
	 * attributes collection to another. Each list item must be a String, a
	 * List, or a Map. If the list item is a simple String value, the attribute
	 * will be mapped as having the same name in the parent flow and in the sub
	 * flow. If the list item is a Map, each map entry must be a String key
	 * naming the attribute in the parent flow, and a String value naming the
	 * attribute in the child flow. If the list item is itself a List, then that
	 * list is itself evaluated recursively, and must itself contain Strings,
	 * Lists, or Maps.
	 * @param mappings The input mappings
	 */
	public void setMappings(Collection mappings) {
		this.mappings = new HashMap();
		putCollectionMappings(this.mappings, mappings);
	}

	/**
	 * Set the mappings that will be executed when mapping model data from one
	 * attributes collection to another.
	 * @link ParameterizableAttributesMapper#setMappings(List) with a List
	 *       containing one item which is a Map. Each map entry must be a String
	 *       key naming the attribute in the parent flow, and a String value
	 *       naming the attribute in the child flow.
	 * @param inputMappings The input mappings
	 */
	public void setMappingsMap(Map inputMappings) {
		this.mappings = new HashMap(inputMappings);
	}

	/**
	 * Internal worker function to convert given mappingsList to a simple
	 * mappings map.
	 */
	private void putCollectionMappings(Map map, Collection mappingsList) {
		Iterator it = mappingsList.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			if (key instanceof Collection) {
				putCollectionMappings(map, (Collection)key);
			}
			else if (key instanceof Map) {
				Map internalMap = (Map)key;
				// we could just add the map into the other, but want to
				// validate key and value
				// types!
				Iterator itMap = internalMap.entrySet().iterator();
				while (itMap.hasNext()) {
					Map.Entry entry = (Map.Entry)itMap.next();
					Assert.isInstanceOf(String.class, entry.getKey(), "ParameterizableFlowModelMapper key: ");
					Assert.isInstanceOf(String.class, entry.getValue(), "ParameterizableFlowModelMapper value: ");
					map.put(entry.getKey(), entry.getValue());
				}
			}
			else {
				Assert.isInstanceOf(String.class, key, "ParameterizableFlowModelMapper key or value: ");
				map.put(key, key);
			}
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
	public void map(AttributeAccessor from, AttributeSetter to) {
		if (mappings != null) {
			Iterator fromNames = mappings.keySet().iterator();
			while (fromNames.hasNext()) {
				// get source value
				String fromName = (String)fromNames.next();
				int idx = fromName.indexOf('.');
				Object fromValue;
				if (idx != -1) {
					// fromName is something like "beanName.propName"
					String beanName = fromName.substring(0, idx);
					String propName = fromName.substring(idx + 1);
					BeanWrapper bw = createBeanWrapper(from.getAttribute(beanName));
					fromValue = bw.getPropertyValue(propName);
				}
				else {
					fromValue = from.getAttribute(fromName);
				}
				// set target value
				String toName = (String)mappings.get(fromName);
				idx = toName.indexOf('.');
				if (idx != -1) {
					// toName is something like "beanName.propName"
					String beanName = toName.substring(0, idx);
					String propName = toName.substring(idx + 1);

					BeanWrapper bw = createBeanWrapper(to.getAttribute(beanName));
					if (logger.isDebugEnabled()) {
						logger.debug("Mapping bean property attribute from path '" + fromName + "' to path '" + toName
								+ "' with value '" + fromValue + "'");
					}
					bw.setPropertyValue(propName, fromValue);
				}
				else {
					if (fromValue == null && !from.containsAttribute(fromName)) {
						if (isMapMissingAttributesToNull()) {
							if (logger.isDebugEnabled()) {
								logger.debug("No value exists for attribute '" + fromName
										+ "' in the from model - thus, I will map a null value");
							}
							to.setAttribute(toName, null);
						}
						else {
							if (logger.isDebugEnabled()) {
								logger.debug("No value exists for attribute '" + fromName
										+ "' in the from model - thus, I will NOT map a value");
							}
						}
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Mapping attribute from name '" + fromName + "' to name '" + toName
									+ "' with value '" + fromValue + "'");
						}
						to.setAttribute(toName, fromValue);
					}
				}
			}
		}
	}

	/**
	 * Create a new bean wrapper wrapping given object. Can be redefined in
	 * subclasses in case special property editors need to be registered or when
	 * other similar tuning is required.
	 */
	protected BeanWrapper createBeanWrapper(Object obj) {
		return new BeanWrapperImpl(obj);
	}
}