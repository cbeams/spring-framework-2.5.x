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

import java.io.Serializable;
import java.util.Map;

import org.springframework.binding.AttributeAccessor;

/**
 * Base superclass for <code>Event</code> sub-types. An event is an occurence
 * of something. Subclasses typically report the occurence of events from
 * different sources; e.g, a http servlet request.
 * @author Keith Donald
 */
public interface Event extends AttributeAccessor, Serializable {
	public String getId();

	public String getStateId();

	public Map getParameters();

	public Object getParameter(String parameterName);
}