/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.util.Assert;

/**
 * {@link FactoryBean} for {@link Resource} descriptors, exposing a
 * looked-up Resource object.
 *
 * <p>If used in the context of a surrounding
 * {@link org.springframework.context.ApplicationContext}, the looking up of the
 * Resource will be delegated to the surrounding 
 * {@link org.springframework.context.ApplicationContext#getResource(String) ApplicationContext's getResource(String) method}.
 * Resource loading behavior will thus be specific to the context implementation.
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 28.12.2003
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ResourceFactoryBean implements FactoryBean, InitializingBean {

    private Resource resource;


    /**
     * Set the resource location.
     * <p>This property is required.
     * @param location the resource location to feed into {@link org.springframework.context.ApplicationContext#getResource}
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


    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.resource, "The location property is required");
    }

}
