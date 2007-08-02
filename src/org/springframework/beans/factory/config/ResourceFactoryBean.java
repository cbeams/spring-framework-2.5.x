/*
 * Copyright 2002-2007 the original author or authors.
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * {@link FactoryBean} for {@link Resource} descriptors,
 * exposing a Resource object for a specific resource location.
 *
 * <p>If used in the context of a surrounding
 * {@link org.springframework.context.ApplicationContext},
 * the resolution of a resource location String will be delegated
 * to the ApplicationContext's <code>getResource</code> method.
 * Resource loading behavior will thus be specific to the context implementation.
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 28.12.2003
 * @deprecated in favor of implicit String-to-Resource conversion through the
 * automatically registered {@link org.springframework.core.io.ResourceEditor}
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ResourceFactoryBean implements FactoryBean, InitializingBean {

	private Resource resource;


	/**
	 * Set the resource location. Can be populated with a String
	 * value in a bean definition, to be automatically translated via
	 * {@link org.springframework.context.ApplicationContext#getResource}
	 * <p>This property is required.
	 */
	public void setLocation(Resource location) {
		this.resource = location;
	}

	public void afterPropertiesSet() {
		if (this.resource == null) {
			throw new IllegalArgumentException("Property 'location' is required");
		}
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
