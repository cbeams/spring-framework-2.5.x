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

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

/**
 * AdvisorChainFactory implementation that caches by method.
 *
 * <p>Uses java.util.IdentityHashMap on J2SE 1.4, which skips expensive
 * Method.hashCode() call. On J2SE 1.3, falls back to using java.util.HashMap.
 *
 * @author Rod Johnson
 * @version $Id: HashMapCachingAdvisorChainFactory.java,v 1.5 2004-05-18 08:03:30 jhoeller Exp $
 * @see java.util.IdentityHashMap
 * @see java.util.HashMap
 * @see java.lang.reflect.Method#hashCode
 */
public final class HashMapCachingAdvisorChainFactory implements AdvisorChainFactory {

	public static final String IDENTITY_HASH_MAP_CLASS_NAME = "java.util.IdentityHashMap";

	private final Map methodCache = createMap();
	
	private Map createMap() {
		// Use IdentityHashMap, introduced in J2SE 1.4, which is a lot faster
		// as we want to compare Method keys by reference.
		// The reason we do this via reflection rather than using new is to avoid
		// a dependence in this class that will break it under J2SE 1.3.
		try {
			Class clazz = Class.forName(IDENTITY_HASH_MAP_CLASS_NAME);
			return (Map) clazz.newInstance();
		}
		catch (Exception ex) {
			// will only happen on J2SE < 1.4
			LogFactory.getLog(getClass()).debug("Falling back to java.util.HashMap (J2SE < 1.4 ?): couldn't create " +
																					"an IdentityHashMap using reflection (" + ex.getMessage() + ")");
			return new HashMap();
		}
	}
	
	public List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy,
																													Method method, Class targetClass) {
		List cached = (List) this.methodCache.get(method);
		if (cached == null) {
			// recalculate
			cached = AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(config, proxy,
																																													method, targetClass);
			this.methodCache.put(method, cached);
		}
		return cached;
	}

	public void activated(AdvisedSupport advisedSupport) {
	}

	public void adviceChanged(AdvisedSupport advisedSupport) {
		this.methodCache.clear();
	}

}
