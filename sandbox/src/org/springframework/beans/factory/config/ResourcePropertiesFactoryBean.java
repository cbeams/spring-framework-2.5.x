/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * A factory for creating indexed resources from a <code>.properties</code>
 * file.
 * 
 * This factory returns a <code>Map</code> where each key is the resource name
 * and each value is the backing <code>Resource</code> object.
 * 
 * @author Keith Donald
 * @see org.springframework.core.io.Resource
 */
public class ResourcePropertiesFactoryBean extends FactoryBeanSupport implements
        FactoryBean {
    protected static final Log logger = LogFactory
            .getLog(ResourcePropertiesFactoryBean.class);
    private Resource location;
    private String resourceBaseName;

    /**
     * Creates a ResourceFactoryBean that populates a map of resources from the
     * .properties file at the provided location. Each key in the map is the
     * <code>String></code> resource key and each value is a instance of
     * <code>org.springframework.core.io.Resource</code>.
     * 
     * @param location
     *            The location of the resource properties file.
     */
    public ResourcePropertiesFactoryBean(Resource location) {
        super();
        setLocation(location);
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Map.class;
    }

    /**
     * Sets the resource .properties location.
     * 
     * @param location
     *            The location.
     */
    public void setLocation(Resource location) {
        Assert.notNull(location);
        this.location = location;
    }

    /**
     * Sets the basepath to prepend to all resources created by this factory.
     * For example, if the basepath is "images", "images" will be prepended to
     * each resource path.
     * 
     * @param basename
     *            The resources basename path, or <code>null</code> if none.
     */
    public void setResourceBaseName(String basename) {
        this.resourceBaseName = basename;
    }

    /**
     * @see com.csi.commons.utils.beans.factory.FactoryBeanSupport#create()
     */
    protected Object create() throws IOException {
        PropertiesFactoryBean factory = new PropertiesFactoryBean();
        factory.setLocation(location);
        factory.afterPropertiesSet();
        logger
                .info("Loading resource property strings from '" + location
                        + "'");
        Map resourcePaths = (Map)factory.getObject();
        Assert.notNull(resourcePaths);
        Map resources = new HashMap(resourcePaths.size());
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        for (Iterator i = resourcePaths.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            String key = (String)entry.getKey();
            String resourcePath = (String)entry.getValue();
            if (resourceBaseName != null) {
                if (resourcePath.startsWith("/")) {
                    resourcePath = resourceBaseName + resourcePath;
                } else {
                    resourcePath = resourceBaseName + "/" + resourcePath;
                }
            }
            resources.put(key, resourceLoader.getResource(resourcePath));
        }
        return resources;
    }
}