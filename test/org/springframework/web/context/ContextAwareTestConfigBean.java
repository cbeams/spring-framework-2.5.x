/*
 * ContextAwareTestConfigBean.java
 *
 * Created on 15 December 2001, 00:19
 */

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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;

/**
 *
 * @author  rod
 * @version 
 */
public class ContextAwareTestConfigBean extends TestConfigBean implements ApplicationContextAware {
	
	private String namespace;
	
	private ApplicationContext ctx;

	/** Creates new ContextAwareTestConfigBean */
    public ContextAwareTestConfigBean() {
    }

	/** Set the ApplicationContext object used by this object
	 * @param ctx ApplicationContext object used by this object
	 * @param namespace namespace this object is in: null means default namespace
	 * @throws ApplicationContextException if initialization attempted by this object
	 * after it has access to the WebApplicatinContext fails
	 */
	public void setApplicationContext(ApplicationContext ctx) throws ApplicationContextException {
		this.ctx = ctx;
		//this.namespace = namespace;
	}
	
	public ApplicationContext getApplicationContext() {
		return ctx;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
}
