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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean that exposes a EHCache CacheManager singleton,
 * configured from a specified config location.
 *
 * <p>If no config location is specified, a CacheManager will be
 * configured from "ehcache.xml" in the root of the class path
 * (i.e., defautl EHCache initialization will apply).
 *
 * <p>Setting up a separate EhCacheManagerFactoryBean is also advisable
 * when using EhCacheFactoryBean, as it cares for proper shutdown of the
 * CacheManager. EhCacheManagerFactoryBean is also necessary for loading
 * EHCache configuration from a non-default config location.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see EhCacheFactoryBean
 * @see net.sf.ehcache.CacheManager
 */
public class EhCacheManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private CacheManager cacheManager;

	/**
	 * Set the location of the EHCache config file. A typical value is "WEB-INF/ehcache.xml".
	 * Default is "ehcache.xml" in the root of the class path (default EHCache initialization).
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	public void afterPropertiesSet() throws IOException, CacheException {
		logger.info("Creating EHCache CacheManager");
		if (this.configLocation != null) {
			this.cacheManager = CacheManager.create(this.configLocation.getURL());
		}
		else {
			this.cacheManager = CacheManager.create();
		}
	}

	public Object getObject() {
		return this.cacheManager;
	}

	public Class getObjectType() {
		return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
	}

	public boolean isSingleton() {
		return true;
	}

	public void destroy() {
		logger.info("Shutting down EHCache CacheManager");
		this.cacheManager.shutdown();
	}

}
