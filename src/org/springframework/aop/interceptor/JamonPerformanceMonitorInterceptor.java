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

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performance monitor interceptor that uses <b>JAMon</b> library
 * to perfom the performance mesurment on the intercepted method
 * and output the stats.
 *
 * <p>This code is inspired by Thierry Templier's blog.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 1.1.3
 */
public class JamonPerformanceMonitorInterceptor implements MethodInterceptor, Serializable {

	/** Static to avoid serializing the logger */
	protected static final Log logger = LogFactory.getLog(JamonPerformanceMonitorInterceptor.class);

	public Object invoke(MethodInvocation invocation) throws Throwable {
		String name = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
		Monitor mon = MonitorFactory.start(name);
		try {
			return invocation.proceed();
		}
		finally {
			mon.stop();
			if (logger.isInfoEnabled()) {
				logger.info("JAMon performance statistics for method [" + name + "]:\n" + mon);
			}
		}
	}

}
