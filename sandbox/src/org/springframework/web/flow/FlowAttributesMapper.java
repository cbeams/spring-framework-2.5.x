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

/**
 * A service interface for mapping attributes of a parent flow session down to a
 * child flow session, when the child flow is spawned. In addition, this
 * interface maps attributes of a child flow session back up to a resuming
 * parent flow, when the child session ends.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowAttributesMapper {

	/**
	 * Create a map of model attributes that should be passed to the spawning
	 * child flow.
	 * @param parentFlowModel The parent flow attributes, the possible set to
	 *        pass down to the child.
	 * @return A map of attributes to pass as input down to the spawning child
	 *         subflow.
	 */
	public Map createSubFlowInputAttributes(AttributesAccessor parentFlowModel);

	/**
	 * Map relavent attributes of an ending sub flow model back up to a resuming
	 * parent flow model.
	 * @param subFlowModel The child's attributes that should be mapped from
	 * @param parentFlowModel The parent's attributes that should be mapped to
	 */
	public void mapSubFlowOutputAttributes(AttributesAccessor subFlowModel, MutableAttributesAccessor parentFlowModel);
}