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

package org.springframework.jmx.export.metadata;

/**
 * @author Rob Harrop
 */
public class ManagedOperationParameter {

	/**
	 * Stores the index of the parameter.
	 */
	private int index = 0;

	/**
	 * Stores the name of the parameter.
	 */
	private String name = "";

	/**
	 * Stores the description of the parameter.
	 */
	private String description = "";

	/**
	 * Gets the parameter description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the parameter description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the parameter name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the parameter name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the index of the parameter in the operation signature.
	 */
	public int getIndex() {
		return index;
	}

  /**
	 * Sets the index of the parameter
	 */ 
	public void setIndex(int index) {
		this.index = index;
	}
}
