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

package org.springframework.context.support;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceEditor;

/**
 * ApplicationContext-aware PropertyEditor for Resource descriptors.
 *
 * <p>Delegates to the ApplicationContext's getResource method for resolving
 * resource locations to Resource descriptors. Resource loading behavior is
 * specific to the context implementation.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ContextResourceEditor extends ResourceEditor {

	private final ApplicationContext applicationContext;

	/**
	 * Create a new ContextResourceEditor for the given context.
	 * @param applicationContext context to resolve resources with
	 */
	public ContextResourceEditor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		String resolvedPath = resolvePath(text);
		setValue(this.applicationContext.getResource(resolvedPath));
	}

}
