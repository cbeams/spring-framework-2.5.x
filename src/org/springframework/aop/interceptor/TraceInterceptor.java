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

package org.springframework.aop.interceptor;

/**
 * Simple AOP Alliance <code>MethodInterceptor</code> that can be introduced
 * in a chain to display verbose trace information about intercepted method
 * invocations, with method entry and method exit info.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @deprecated since Spring 1.2: in favor of SimpleTraceInterceptor
 * @see SimpleTraceInterceptor
 */
public class TraceInterceptor extends SimpleTraceInterceptor {

}
