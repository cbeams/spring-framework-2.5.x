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

package org.springframework.cache.ehcache;

import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean that exposes a EHCache CacheManager singleton configured from
 * specified config location. If config location is not specified, CacheManager is configured
 * from ehcache.xml file loaded from a classpath.
 *
 * @author Dmitriy Kopylenko
 * @since 1.1.1
 */
public class EhCacheManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    private Resource configLocation;
    
    private CacheManager cacheManager;

    /**
     * Set the location of the EHCache config file as class path resource.
     * A typical value is "WEB-INF/ehcache.xml".
     */
    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }
    
	public void afterPropertiesSet() throws IOException, CacheException {
	    if(this.configLocation == null){
	        this.cacheManager = CacheManager.create();
	    }
	    else{
	        this.cacheManager = CacheManager.create(this.configLocation.getURL());
	    }
	}

    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        this.cacheManager.shutdown();
    }
    
	public Object getObject() {
		return this.cacheManager;
	}

	public Class getObjectType() {
		return (CacheManager.class);
	}

	public boolean isSingleton() {
		return true;
	}
}
