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

import org.aopalliance.aop.AspectException;

/**
 * Class containing static methods used to obtain information about the
 * current AOP invocation. 
 *
 * <p>The currentProxy() method is usable if the AOP framework is configured
 * to expose the current proxy (not the default). It returns the AOP proxy in 
 * use. Target objects or advice can use this to make advised calls, in the same way 
 * as getEJBObject() can be used in EJBs. They can also use it to find advice
 * configuration.
 *
 * <p>The AOP framework does not expose proxies by default, as there is a performance cost
 * in doing so.
 *
 * <p>The functionality in this class might be used by a target object
 * that needed access to resources on the invocation. However, this
 * approach should not be used when there is a reasonable alternative,
 * as it makes application code dependent on usage under AOP and
 * the Spring AOP framework.
 *
 * @author Rod Johnson
 * @since 13-Mar-2003
 */
public abstract class AopContext {
	
	/**
	 * AOP proxy associated with this thread. Will be null unless the
	 * exposeInvocation property on the controlling proxy has been set to true.
	 * The default value for this property is false, for performance reasons.
	 */
	private static ThreadLocal currentProxy = new ThreadLocal();


	/**
	 * Try to return the current AOP proxy. This method is usable
	 * only if the calling method has been invoked via AOP, and the
	 * AOP framework has been set to expose proxies. Otherwise,
	 * this method will throw an AspectException.
	 * @return Object the current AOP proxy (never returns null)
	 * @throws AspectException if the proxy cannot be found,
	 * because the method was invoked outside an AOP invocation
	 * context, or because the AOP framework has not been configured
	 * to expose the proxy
	 */
	public static Object currentProxy() throws AspectException {
		if (currentProxy.get() == null) {
			throw new AspectException("Cannot find proxy: set 'exposeProxy' property on Advised to make it available");
		}
		return currentProxy.get();
	}
	
	/**
	 * Make the given proxy available via the currentProxy method. 
	 * Note that the caller should be careful to return the old value
	 * before it's done.
	 * @param proxy the proxy to expose
	 * @return the old proxy, which may be null if none was bound
	 */
	static Object setCurrentProxy(Object proxy) {
		Object old = currentProxy.get();
		currentProxy.set(proxy);
		return old;
	}

}
