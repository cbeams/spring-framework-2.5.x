/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util.enums;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for looking up <code>LabeledEnum</code> instances.
 * @author Keith Donald
 */
public interface LabeledEnumResolver {

	/**
	 * Returns a set of enumerations of a particular type. Each element in the
	 * list should be an instance of LabeledEnum.
	 * @param type the enum type
	 * @return A list of localized enumeration instances for the provided type
	 * @throws IllegalArgumentException if the type is not supported
	 */
	public Collection getLabeledEnumCollection(String type);

	/**
	 * Returns a map of enumerations of a particular type. Each element in the
	 * map should be a key->value pair, where the key is the enum code, and the
	 * value is the <code>LabeledEnum</code> instance.
	 * @param type the enum type
	 * @return A map of localized enumeration instances
	 * @throws IllegalArgumentException if the type is not supported
	 */
	public Map getLabeledEnumMap(String type);

	/**
	 * Resolves a single <code>LabeledEnum</code> by its identifying code.
	 * @param type the enum type
	 * @param code the enum code
	 * @return The enum, or <code>null</code> if not found.
	 * @throws IllegalArgumentException if the code did not map to a valid
	 *         instance
	 */
	public LabeledEnum getLabeledEnum(String type, Comparable code) throws IllegalArgumentException;

	/**
	 * Resolves a single <code>LabeledEnum</code> by its identifying code.
	 * @param type the enum type
	 * @param label the enum label
	 * @return The enum, or <code>null</code> if not found.
	 * @throws IllegalArgumentException if the label did not map to a valid
	 *         instance
	 */
	public LabeledEnum getLabeledEnum(String type, String label) throws IllegalArgumentException;

}