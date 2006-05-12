/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.util;

import java.lang.reflect.Method;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public interface BridgeMethodResolver {

	/**
	 * Given a synthetic {@link java.lang.reflect.Method#isBridge bridge Method} returns the {@link java.lang.reflect.Method}
	 * being bridged. A bridge method may be created by the compiler when extending a parameterized
	 * type whose methods have parameterized arguments. During runtime invocation the bridge {@link java.lang.reflect.Method} may
	 * be invoked and/or used via reflection. When attempting to locate annotations on {@link java.lang.reflect.Method Methods} it is
	 * wise to check for bridge {@link java.lang.reflect.Method Methods} as appropriate and find the bridged {@link java.lang.reflect.Method}.
	 * <p/>See <a href="http://java.sun.com/docs/books/jls/third_edition/html/expressions.html#15.12.4.5">
	 * The Java Language Specification</a> for more details on the use of bridge methods.
	 *
	 * @return the bridged {@link java.lang.reflect.Method} if the supplied {@link java.lang.reflect.Method} is a valid bridge, otherwise the supplied {@link java.lang.reflect.Method}
	 * @throws IllegalStateException if no bridged {@link java.lang.reflect.Method} can be found.
	 */
	Method resolveBridgeMethod(Method bridgeMethod);
}
