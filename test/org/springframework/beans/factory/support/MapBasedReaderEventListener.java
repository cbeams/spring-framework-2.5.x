/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rob Harrop
 */
public class MapBasedReaderEventListener implements ReaderEventListener {

	private Map components = new HashMap();

	public void componentRegistered(ComponentDefinition componentDefinition) {
		this.components.put(componentDefinition.getName(), componentDefinition);
	}

	public ComponentDefinition getComponentDefinition(String name) {
		return (ComponentDefinition) this.components.get(name);
	}
}
