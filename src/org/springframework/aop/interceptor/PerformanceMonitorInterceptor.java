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

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

/**
 * Trivial performance monitor interceptor.
 * This interceptor has no effect on the intercepted method call.
 *
 * <p>Presently logs information using Commons Logging, at "info" level.
 * Could make this much more sophisticated, storing information etc.
 *
 * @author Rod Johnson
 * @author Dmitriy Kopylenko
 */
public class PerformanceMonitorInterceptor implements MethodInterceptor, Serializable {

	/**
	 * Static to avoid the need to serialize it
	 */
	protected static final Log logger = LogFactory.getLog(PerformanceMonitorInterceptor.class);

	public Object invoke(MethodInvocation invocation) throws Throwable {
		String name = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
		logger.debug("Begin performance monitoring of method '" + name + "'");

		StopWatch sw = new StopWatch(name);
		sw.start(name);
		Object rval = invocation.proceed();
		sw.stop();

		logger.info(sw.shortSummary());
		logger.debug("End performance monitoring of method '" + name + "'");

		return rval;
	}

}
