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
 *
 * <p>This context is used to implement the <code>bean()</code> PCD in an
 * AspectJ expression.
 *
 * <p>The caller must follow a "resource allocation" pattern to ensure that the
 * context is reset after proxy creation is complete. This can be accomplished
 * by following a code pattern such as:
 *
 * <pre class="code">
 * try {
 *     ProxyCreationContext.notifyProxyCreationStart(beanName, true);
 *     // ... proxy creation logic
 * } finally {
 *     ProxyCreationContext.notifyProxyCreationComplete();
 * }
 * </pre>
 *
 * @author Ramnivas Laddad
 * @see BeanNamePointcutDesignatorHandler
 * @since 2.1
 */
public class ProxyCreationContext {

	private static final ThreadLocal proxyCreationInfoHolder = new ThreadLocal();


	public static void notifyProxyCreationStart(String beanName, boolean isInnerBean) {
		ProxyCreationInfo info = new ProxyCreationInfo(beanName, isInnerBean);
		proxyCreationInfoHolder.set(info);
	}

	public static void notifyProxyCreationComplete() {
		proxyCreationInfoHolder.set(null);
	}

	public static boolean isProxyCreationInProgress() {
		return proxyCreationInfoHolder.get() != null;
	}

	public static String getCurrentProxyingBeanName() {
		ProxyCreationInfo info = (ProxyCreationInfo) proxyCreationInfoHolder.get();
		return info == null ? null : info.beanName;
	}

	public static boolean isCurrentProxyingBeanAnInnerBean() {
		ProxyCreationInfo info = (ProxyCreationInfo) proxyCreationInfoHolder.get();
		return info != null && info.isInnerBean;
	}


	private static final class ProxyCreationInfo {

		private final String beanName;

		private final boolean isInnerBean;

		public ProxyCreationInfo(String beanName, boolean isInnerBean) {
			this.beanName = beanName;
			this.isInnerBean = isInnerBean;
		}
	}

}
