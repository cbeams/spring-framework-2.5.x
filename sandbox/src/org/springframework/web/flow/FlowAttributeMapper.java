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

import java.util.Map;

import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeSetter;

/**
 * A service interface that maps attributes between flow models.
 * <p>
 * A model mapper maps the attributes of a parent flow model down to a child
 * flow model, as <i>input</i> when the child is spawned as a subflow. In
 * addition, a model mapper maps attributes of a child flow model back up to a
 * resuming parent flow, as <i>output</i> when the child session ends and
 * control is returned to the parent flow.
 * <p>
 * For example, say you have the following parent flow session:
 * 
 * <pre>
 * 
 *  Parent Flow Session 1
 *  ---------------------
 *  - flow=myFlow
 *  - flowModel=[map:attr1-&gt;value1, attr2-&gt;value2, attr3-&gt;value3]
 *  
 * </pre>
 * 
 * For "Parent Flow Session 1" above, there are 3 attributes in the flow model
 * ("attr1", "attr2", and "attr3", respectively.) Any of these three attributes
 * may be mapped as input down to child subflows when those subflows are
 * spawned. An implementation of this interface performs the actual mapping,
 * encapsulating knowledge of <i>which </i> attributes should be mapped, and
 * <i>how </i> they will be mapped (for example, will the same attribute names
 * be used between models or not?)
 * <p>
 * For example:
 * 
 * <pre>
 * 
 *  Model Mapper 1
 *  --------------
 *  - inputMappings=(map:attr1-&gt;attr1, attr3-&gt;localAttr1)
 *  - outputMappings=(map:localAttr1-&gt;attr3)
 *  
 * </pre>
 * 
 * The above example "Model Mapper 1" specifies <code>inputMappings</code>
 * that define which parent attributes to map as input to the child. In this
 * case, two attributes in the parent model are mapped, "attr1" and "attr3".
 * "attr1" is mapped with the name "attr1" (given the same name in both models),
 * while "attr3" is mapped to "localAttr1", given a different name that is local
 * to the child flow model.
 * <p>
 * Likewise, when a child flow ends, the <code>outputMappings</code> define
 * which attributes to map as output up to the parent. In this case the
 * attribute "localAttr1" will be mapped up to the parent as "attr3", updating
 * the value of "attr3" in the parent flow model.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowAttributeMapper {

	/**
	 * Create a map of model attributes that should be passed as input to a
	 * spawning child flow session.
	 * <p>
	 * Attributes set in the <code>Map</code> returned by this method will be
	 * added to the flow model of the child sub flow session when the session is
	 * spawned and activated.
	 * @param parentFlowAttributes The parent flow model, containing the set of
	 *        possible attributes that may be passed down to the child.
	 * @return A map of attributes (name=value pairs) to pass as input to the
	 *         spawning child subflow.
	 */
	public Map createSubFlowInputAttributes(AttributeAccessor parentFlowAttributes);

	/**
	 * Map relavent model attributes of an ending sub flow session back up to a
	 * resuming parent flow session. This maps the <i>output </i> of the child
	 * as new input to the resuming parent.
	 * @param subFlowAttributes The child's flow model that should be mapped
	 *        from, containing the set of possible attributes that may be passed
	 *        up to the parent.
	 * @param parentFlowAttributes The parent's flow model that should be mapped
	 *        to, where output attributes of the child sub flow will be set as
	 *        input.
	 */
	public void mapSubFlowOutputAttributes(AttributeAccessor subFlowAttributes, AttributeSetter parentFlowAttributes);
}