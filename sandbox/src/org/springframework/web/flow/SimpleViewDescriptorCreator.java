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
package org.springframework.web.flow;

/**
 * Simple view descriptor factory that produces a ViewDescriptor with the same
 * view name each time.
 * 
 * @author Keith Donald
 */
public class SimpleViewDescriptorCreator implements ViewDescriptorCreator {

	/**
	 * A static view name to render.
	 */
	private String viewName;

	/**
	 * Creates a creator that will produce view descriptors requesting that the
	 * specified view is rendered.
	 * @param viewName the view name
	 */
	public SimpleViewDescriptorCreator(String viewName) {
		this.viewName = viewName;
	}
	
	/**
	 * Returns the static view name.
	 * @return the view name
	 */
	public String getViewName() {
		return viewName;
	}
	
	public ViewDescriptor createViewDescriptor(RequestContext context) {
		return new ViewDescriptor(viewName, context.getModel());
	}

}