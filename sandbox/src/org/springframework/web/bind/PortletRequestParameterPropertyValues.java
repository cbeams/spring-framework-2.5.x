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

package org.springframework.web.bind;

import javax.portlet.PortletRequest;

import org.springframework.beans.MutablePropertyValues;

/**
 * PropertyValues implementation created from parameters in a PortlettRequest.
 *  *
 * <p>This class is not immutable to be able to efficiently remove property
 * values that should be ignored for binding.
 *
 */
public class PortletRequestParameterPropertyValues extends MutablePropertyValues {

	/** Default prefix separator */
	//public static final String DEFAULT_PREFIX_SEPARATOR = "_";

	public PortletRequestParameterPropertyValues(PortletRequest request) {
	    // Nick Lothian: I don't understand why the functionality in these
	    // constructors was/is commented out.
		//this(request, null, null);
	    super(request.getParameterMap());
	}

	public PortletRequestParameterPropertyValues(PortletRequest request, String prefix) {
		//this(request, prefix, DEFAULT_PREFIX_SEPARATOR);
	}

	public PortletRequestParameterPropertyValues(PortletRequest request, String prefix, String prefixSeparator) {
		//super(WebUtils.getParametersStartingWith(request, (prefix != null) ? prefix + prefixSeparator : null));
	}

}
