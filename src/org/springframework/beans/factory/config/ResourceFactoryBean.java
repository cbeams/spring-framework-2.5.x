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

package org.springframework.beans.factory.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean for Resource descriptors. Exposes a looked-up Resource object.
 *
 * <p>Delegates to the ApplicationContext's getResource method.
 * Resource loading behavior is specific to the context implementation.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ResourceFactoryBean implements FactoryBean {

	private Resource resource;

	/**
	 * Set the resource location.
	 * @param location the resource location to feed into getResource
	 */
	public void setLocation(Resource location) {
		this.resource = location;
	}

	public Object getObject() {
		return this.resource;
	}

	public Class getObjectType() {
		return (this.resource != null ? this.resource.getClass() : Resource.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
