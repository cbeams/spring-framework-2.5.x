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

package org.springframework.web.servlet.tags;

import org.springframework.web.servlet.support.RequestContext;

/**
 * This subclass is just kept for old versions of the Spring tag library that
 * expect a BindStatus class in the tag package, for example when deploying
 * a Spring 1.1 jar to an existing web application with precompiled JSPs.
 *
 * <p>As of Spring 1.1, use the BindStatus class in the generic support package
 * instead. That class is also used for the RequestContext in a Velocity or
 * FreeMarker view.
 *
 * @author Juergen Hoeller
 * @deprecated in favor of org.springframework.web.servlet.support.BindStatus
 * @see org.springframework.web.servlet.support.BindStatus
 * @see org.springframework.web.servlet.support.RequestContext#getBindStatus
 */
public class BindStatus extends org.springframework.web.servlet.support.BindStatus {

	public BindStatus(RequestContext requestContext, String path, boolean htmlEscape) {
		super(requestContext, path, htmlEscape);
	}

}
