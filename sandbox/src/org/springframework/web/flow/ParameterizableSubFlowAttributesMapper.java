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
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

/**
 * Simple attributes mapper that allows mappings to be configured in the Spring
 * application context.
 * 
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td>toMappingsList</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping parent flow data <i>to </i> a newly
 * spawned sub flow. The provided list contains the names of the attributes in
 * the parent to pass to the subflow for access. The same name is used in both
 * parent flow and sub flow model.</td>
 * </tr>
 * <tr>
 * <td>toMappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping parent flow data <i>to </i> a newly
 * spawned sub flow. The keys in given map are the names of entries in the
 * parent model that will be mapped. The value associated with a key is the name
 * of the target entry that will be placed in the subflow model.</td>
 * </tr>
 * <tr>
 * <td>fromMappingsList</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping subflow data <i>from </i> the subflow
 * back to the parent flow (once the subflow ends and the parent flow resumes).
 * The provided list contains the names of the attributes in the subflow to pass
 * to the parent for access. The same name is used in both parent flow and sub
 * flow model.</td>
 * </tr>
 * <tr>
 * <td>fromMappingsMap</td>
 * <td>empty</td>
 * <td>Mappings executed when mapping subflow flow data <i>from </i> the
 * subflow back to the parent flow (once the subflow ends and the parent flow
 * resumes). The keys in given map are the names of entries in the parent model
 * that will be mapped. The value associated with a key is the name of the
 * target entry that will be placed in the subflow model.</td>
 * </tr>
 * </table>
 * 
 * <p>
 * The mappings defined using the above configuration properties fully support
 * bean property access. So an entry name in a mapping can either be "beanName"
 * or "beanName.propName". Nested property values are also supported
 * ("beanName.propName.propName").
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class ParameterizableSubFlowAttributesMapper implements SubFlowAttributesMapper, Serializable {

	protected final Log logger = LogFactory.getLog(getClass());;

	private Map toMappings = Collections.EMPTY_MAP;

	private Map fromMappings = Collections.EMPTY_MAP;

	private boolean mapMissingAttributesToNull = false;

	/**
	 * Set the mappings that will be executed when mapping model data <i>to </i>
	 * a sub flow. All keys in given list will be mapped.
	 */
	public void setToMappingsList(List toMappingsList) {
		this.toMappings = new HashMap(toMappingsList.size());
		putListMappings(this.toMappings, toMappingsList);
	}

	private void putListMappings(Map map, List mappingsList) {
		Iterator it = mappingsList.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			if (key instanceof List) {
				putListMappings(map, (List)key);
			}
			else {
				Assert.isInstanceOf(String.class, key);
				map.put(key, key);
			}
		}
	}

	/**
	 * Set the mappings that will be executed when mapping model data <i>to </i>
	 * the sub flow. The keys are the names in the parent flow model, the
	 * corresponding values are the names in the sub flow model.
	 */
	public void setToMappingsMap(Map toMappings) {
		this.toMappings = new HashMap(toMappings);
	}

	public void setToMappingsMaps(Map[] toMappings) {
		this.toMappings = new HashMap();
		for (int i = 0; i < toMappings.length; i++) {
			this.toMappings.putAll(toMappings[i]);
		}
	}

	/**
	 * Set the mappings that will be executed when mapping model data <i>from
	 * </i> the sub flow. All keys in given list will be mapped.
	 */
	public void setFromMappingsList(List fromMappingsList) {
		this.fromMappings = new HashMap(fromMappingsList.size());
		putListMappings(this.fromMappings, fromMappingsList);
	}

	/**
	 * Set the mappings that will be executed when mapping model data <i>from
	 * </i> a sub flow. The keys are the names in the sub flow model, the
	 * corresponding values are the names in the parent flow model.
	 */
	public void setFromMappingsMap(Map fromMappings) {
		this.fromMappings = new HashMap(fromMappings);
	}

	public void setFromMappingsMaps(Map[] fromMappings) {
		this.fromMappings = new HashMap();
		for (int i = 0; i < fromMappings.length; i++) {
			this.fromMappings.putAll(fromMappings[i]);
		}
	}

	public void setMapMapMissingAttributesToNull(boolean toNull) {
		this.mapMissingAttributesToNull = toNull;
	}

	public boolean isMapMissingAttributesToNull() {
		return this.mapMissingAttributesToNull;
	}

	public Map createSpawnedSubFlowAttributesMap(AttributesAccessor parentFlowAttributes) {
		Map subFlowAttributes = new HashMap();
		map(parentFlowAttributes, new MapAttributesAccessorAdapter(subFlowAttributes), toMappings);
		return Collections.unmodifiableMap(subFlowAttributes);
	}

	public void mapToResumingParentFlow(AttributesAccessor subFlowAttributes,
			MutableAttributesAccessor parentFlowAttributes) {
		map(subFlowAttributes, parentFlowAttributes, fromMappings);
	}

	/**
	 * Map data from one map to another map using specified mappings.
	 */
	protected void map(AttributesAccessor from, MutableAttributesAccessor to, Map mappings) {
		if (mappings != null) {
			Iterator fromNames = mappings.keySet().iterator();
			while (fromNames.hasNext()) {
				//get source value
				String fromName = (String)fromNames.next();
				int idx = fromName.indexOf('.');
				Object fromValue;
				if (idx != -1) {
					//fromName is something like "beanName.propName"
					String beanName = fromName.substring(0, idx);
					String propName = fromName.substring(idx + 1);

					BeanWrapper bw = createBeanWrapper(from.getAttribute(beanName));
					fromValue = bw.getPropertyValue(propName);
				}
				else {
					fromValue = from.getAttribute(fromName);
					if (fromValue == null) {
						if (isMapMissingAttributesToNull()) {
							if (logger.isDebugEnabled()) {
								logger.debug("No value exists for attribute '" + fromName
										+ "' in the from model map - thus, I will map a null value");
							}
						}
						else {
							if (logger.isDebugEnabled()) {
								logger.debug("No value exists for attribute '" + fromName
										+ "' in the from model map - thus, I will NOT map a value");
							}
							return;
						}
					}
				}

				//set target value
				String toName = (String)mappings.get(fromName);
				idx = toName.indexOf('.');
				if (idx != -1) {
					//toName is something like "beanName.propName"
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
					if (logger.isDebugEnabled()) {
						logger.debug("Mapping attribute from name '" + fromName + "' to name '" + toName
								+ "' with value '" + fromValue + "'");
					}
					to.setAttribute(toName, fromValue);
				}
			}
		}
	}

	/**
	 * <p>
	 * Create a new bean wrapper wrapping given object. Can be redefined in
	 * subclasses in case special property editors need to be registered or when
	 * other similar tuning is required.
	 */
	protected BeanWrapper createBeanWrapper(Object obj) {
		return new BeanWrapperImpl(obj);
	}

	private static class MapAttributesAccessorAdapter implements MutableAttributesAccessor {
		private Map map;

		public MapAttributesAccessorAdapter(Map map) {
			this.map = map;
		}

		public Object getAttribute(String attributeName) {
			return map.get(attributeName);
		}

		public boolean containsAttribute(String attributeName) {
			return map.containsKey(attributeName);
		}

		public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		public void assertAttributePresent(String attributeName) throws IllegalStateException {
			throw new UnsupportedOperationException();
		}

		public boolean containsAttribute(String attributeName, Class requiredType) {
			throw new UnsupportedOperationException();
		}

		public Object getAttribute(String attributeName, Class requiredType) {
			throw new UnsupportedOperationException();
		}

		public Object getRequiredAttribute(String attributeName) {
			throw new UnsupportedOperationException();
		}

		public Collection attributeEntries() {
			throw new UnsupportedOperationException();
		}

		public Collection attributeNames() {
			throw new UnsupportedOperationException();
		}

		public Collection attributeValues() {
			throw new UnsupportedOperationException();
		}

		public Object getRequiredAttribute(String attributeName, Class requiredType) {
			throw new UnsupportedOperationException();
		}

		public void setAttribute(String attributeName, Object attributeValue) {
			map.put(attributeName, attributeValue);
		}

		public void setAttributes(Map attributes) {
			throw new UnsupportedOperationException();
		}

		public void removeAttribute(String attributeName) {
			map.remove(attributeName);
		}
	}
}