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

package org.springframework.web.context;

import javax.servlet.ServletContext;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the ServletContext (typically determined by the WebApplicationContext)
 * that it runs in.
 *
 * <p>Can be implemented instead of ApplicationContextAware (casting the
 * context to WebApplicationContext) if all an object needs is a reference
 * to the ServletContext.
 *
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see org.springframework.context.ApplicationContextAware
 */
public interface ServletContextAware {

	/**
	 * Set the ServletContext that this object runs in.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * Invoked after ApplicationContextAware's setApplicationContext.
	 * @param servletContext ServletContext object to be used by this object
	 */
	void setServletContext(ServletContext servletContext);

}
