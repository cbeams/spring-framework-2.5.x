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

package org.springframework.validation;

/**
 * Interface for strategies that register custom property editors with a
 * data binder. This is particularly useful when you need to use the
 * same set of property editors in several different situations: write
 * a corresponding registrar and reuse that in each case.
 *
 * <p>Note: This interface is only intended for use with Spring Web Flow previews.
 * Since Spring 1.2.6, it is superseded by the PropertyEditorRegistrar interface
 * in the beans package, working on a passed-in PropertyEditorRegistry interface
 * rather than the DataBinder class. This is also what Spring Web Flow 1.0 RC1+
 * is using.
 * 
 * @author Keith Donald
 * @since 1.2.2
 * @deprecated since Spring 1.2.6, in favor of the PropertyEditorRegistrar
 * interface in the <code>org.springframework.beans</code> package
 * @see org.springframework.beans.PropertyEditorRegistrar
 * @see org.springframework.beans.PropertyEditorRegistry
 */
public interface PropertyEditorRegistrar {
	
	/**
	 * Register custom PropertyEditors with the given DataBinder.
	 * @param binder the DataBinder to register the custom PropertyEditors with
	 */
	void registerCustomEditors(DataBinder binder);

}
