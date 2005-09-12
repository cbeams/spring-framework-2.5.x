/*
 * Copyright 2002-2005 the original author or authors.
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

/**
 * Strategy interface for proxy creation.
 * Out-of-the-box implementations are available
 * for JDK dynamic proxies and CGLIB.
 *
 * @author Rod Johnson
 * @see JdkDynamicAopProxy
 * @see Cglib2AopProxy
 */
public interface AopProxy {

	/**
	 * Create a new proxy object.
	 * <p>Uses the most optimal default class loader (if necessary for proxy creation):
	 * usually, the thread context class loader.
	 * @see java.lang.Thread#getContextClassLoader()
	 */
	Object getProxy();

	/**
	 * Create a new proxy object.
	 * <p>Uses the given class loader (if necessary for proxy creation).
	 * <code>null</code> will simply be passed down and thus lead to the low-level
	 * proxy facility's default, which is usually different from the default chosen
	 * by the AopProxy implementation's <code>getProxy</code> method.
	 * @param classLoader the class loader to create the proxy with
	 * (or <code>null</code> for the low-level proxy facility's default)
	 */
	Object getProxy(ClassLoader classLoader);

}
