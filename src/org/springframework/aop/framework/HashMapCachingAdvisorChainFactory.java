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
import java.util.List;
import java.util.Map;

import org.springframework.core.CollectionFactory;

/**
 * AdvisorChainFactory implementation that caches by method.
 *
 * <p>Uses IdentityHashMap on JDK >= 1.4 respectively Commons Collections 3.x'
 * IdentityMap (if available), which skip expensive Method.hashCode() calls.
 * Falls back to standard HashMap on plain JDK 1.3.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.core.CollectionFactory#createIdentityMapIfPossible
 * @see java.lang.reflect.Method#hashCode
 */
public final class HashMapCachingAdvisorChainFactory implements AdvisorChainFactory {

	private final Map methodCache = CollectionFactory.createIdentityMapIfPossible(32);
	
	public List getInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Object proxy, Method method, Class targetClass) {
		List cached = (List) this.methodCache.get(method);
		if (cached == null) {
			// recalculate
			cached = AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(
					config, proxy, method, targetClass);
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
