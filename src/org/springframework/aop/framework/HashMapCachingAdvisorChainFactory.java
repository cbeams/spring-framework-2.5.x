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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.JdkVersion;

/**
 * AdvisorChainFactory implementation that caches by method.
 *
 * <p>Uses java.util.IdentityHashMap on J2SE 1.4, which skips expensive
 * Method.hashCode() call. On J2SE 1.3, falls back to using java.util.HashMap.
 *
 * @author Rod Johnson
 * @see java.util.IdentityHashMap
 * @see java.util.HashMap
 * @see java.lang.reflect.Method#hashCode
 */
public final class HashMapCachingAdvisorChainFactory implements AdvisorChainFactory {

	private final Map methodCache = createMap();
	
	private Map createMap() {
		// Use IdentityHashMap, introduced in J2SE 1.4, which is a lot faster
		// as we want to compare Method keys by reference. If not available,
		// fall back to standard HashMap.
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14) {
			return IdentityHashMapCreator.createIdentityHashMap();
		}
		else {
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


	/**
	 * Actual creation of a java.util.IdentityHashMap.
	 * In separate inner class to avoid runtime dependency on JDK 1.4.
	 */
	private static abstract class IdentityHashMapCreator {

		private static Map createIdentityHashMap() {
			return new IdentityHashMap();
		}
	}

}
