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

package org.springframework.beans;

import java.beans.PropertyEditor;

/**
 * Interface that encapsulates methods for registering JavaBeans PropertyEditors.
 * This is the interface that a PropertyEditorRegistrar operates on.
 *
 * <p>Implemented by BeanWrapper/BeanWrapperImpl and by the DataBinder class
 * in the validation package.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see PropertyEditorRegistrar
 * @see BeanWrapper
 * @see BeanWrapperImpl
 * @see org.springframework.validation.DataBinder
 */
public interface PropertyEditorRegistry {

	/**
	 * Register the given custom property editor for all properties
	 * of the given type.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 */
	void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor);

	/**
	 * Register the given custom property editor for the given type and
	 * property, or for all properties of the given type.
	 * <p>If the property path denotes an array or Collection property,
	 * the editor will get applied either to the array/Collection itself
	 * (the PropertyEditor has to create an array or Collection value) or
	 * to each element (the PropertyEditor has to create the element type),
	 * depending on the specified required type.
	 * <p>Note: Only one single registered custom editor per property path
	 * is supported. In case of a Collection/array, do not register an editor
	 * for both the Collection/array and each element on the same property.
	 * @param requiredType type of the property (can be <code>null</code> if a property
	 * is given but should be specified in any case for consistency checking)
	 * @param propertyPath path of the property (name or nested path), or
	 * null if registering an editor for all properties of the given type
	 * @param propertyEditor editor to register
	 */
	void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor);

	/**
	 * Find a custom property editor for the given type and property.
	 * @param requiredType type of the property (can be <code>null</code> if a property
	 * is given but should be specified in any case for consistency checking)
	 * @param propertyPath path of the property (name or nested path), or
	 * null if looking for an editor for all properties of the given type
	 * @return the registered editor, or <code>null</code> if none
	 */
	PropertyEditor findCustomEditor(Class requiredType, String propertyPath);

}
