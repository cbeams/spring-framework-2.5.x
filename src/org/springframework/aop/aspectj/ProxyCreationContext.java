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
package org.springframework.aop.aspectj;

/**
 * Context related to the current bean that is being proxied.
 * <p>
 * This context is used to implement the bean() PCD in AspectJ expression.
 * <p>
 * The caller must follow "resource allocation" pattern to ensure that the
 * context is reset after proxy creation is complete. This can be accomplished 
 * by code pattern such as this
 * <pre>
 * try {
 *     ProxyCreationContext.notifyProxyCreationStart(beanName);
 *     ... proxy creation logic
 * } finally {
 *     ProxyCreationContext.notifyProxyCreationComplete();
 * }
 * </pre>
 * 
 * @author Ramnivas Laddad
 * @since 2.1
 * @see BeanNamePointcutDesignatorHandler
 */
public class ProxyCreationContext {
	private static final ThreadLocal proxyCreationInProgress = new ThreadLocal();

	private static final ThreadLocal nameHolder = new ThreadLocal();

	public static void notifyProxyCreationStart(String beanName) {
		proxyCreationInProgress.set(Boolean.TRUE);
		nameHolder.set(beanName);
	}

	public static void notifyProxyCreationComplete() {
		proxyCreationInProgress.set(Boolean.FALSE);
		nameHolder.set(null);
	}

	public static boolean isProxyCreationInProgress() {
		return ((Boolean) proxyCreationInProgress.get()).booleanValue();
	}

	public static String getCurrentProxyingBeanName() {
		return (String) nameHolder.get();
	}
}
