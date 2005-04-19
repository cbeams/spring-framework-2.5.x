
package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

/**
 * @author robh
 */
public class PerformanceTraceInterceptor extends AbstractTraceInterceptor {

	protected Object invokeUnderTrace(MethodInvocation methodInvocation, Log logger) throws Throwable {
		String name = methodInvocation.getMethod().getDeclaringClass().getName() + "." + methodInvocation.getMethod().getName();
		StopWatch stopWatch = new StopWatch(name);

		try {
			return methodInvocation.proceed();
		}
		finally {
			stopWatch.stop();
			logger.trace(stopWatch.shortSummary());
		}
	}
}
