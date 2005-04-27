
package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

/**
 * Intended to replace current PerformanceMonitorInterceptor.
 * @author robh
 */
public class PerformanceTraceInterceptor extends AbstractTraceInterceptor {

	protected Object invokeUnderTrace(MethodInvocation methodInvocation, Log logger) throws Throwable {
		String name = methodInvocation.getMethod().getDeclaringClass().getName() + "." + methodInvocation.getMethod().getName();
		StopWatch stopWatch = new StopWatch(name);

		try {
			stopWatch.start(name);
			return methodInvocation.proceed();
		}
		finally {
			stopWatch.stop();
			this.log(logger, stopWatch.shortSummary());
		}
	}
}
