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

package org.springframework.beans;

/**
 * Interface for strategies that register custom property editors with a
 * property editor registry. This is particularly useful when you need to use
 * the same set of property editors in several different situations: write
 * a corresponding registrar and reuse that in each case.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see PropertyEditorRegistry
 * @see java.beans.PropertyEditor
 */
public interface PropertyEditorRegistrar {
	
	/**
	 * Register custom PropertyEditors with the given PropertyEditorRegistry.
	 * The passed-in registry will usually be a BeanWrapper or a DataBinder.
	 * @param registry the PropertyEditorRegistry to register the custom
	 * PropertyEditors with
	 * @see BeanWrapper
	 * @see org.springframework.validation.DataBinder
	 */
	void registerCustomEditors(PropertyEditorRegistry registry);

}
