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

/**
 * Strategy interface for proxy creation.
 * Out-of-the-box implementations are available
 * for JDK dynamic proxies and CGLIB.
 * @author Rod Johnson
 * @see JdkDynamicAopProxy
 * @see Cglib2AopProxy
 */
public interface AopProxy {

	/**
	 * Create a new Proxy object for the given object. Uses the thread
	 * context class loader (if necessary for proxy creation).
	 * @see java.lang.Thread#getContextClassLoader
	 */
	Object getProxy();

	/**
	 * Create a new Proxy object for the given object.
	 * Uses the given class loader (if necessary for proxy creation).
	 * @param classLoader the class loader to use
	 */
	Object getProxy(ClassLoader classLoader);

}
