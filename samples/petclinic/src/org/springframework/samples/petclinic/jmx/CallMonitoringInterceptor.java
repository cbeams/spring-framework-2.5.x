package org.springframework.samples.petclinic.jmx;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.util.StopWatch;

/**
 * Simple interceptor that monitors call count and call invocation time.
 * Implements the CallMonitor management interface.
 * 
 * @author Rob Harrop
 * @since 1.2
 */
public class CallMonitoringInterceptor implements CallMonitor, MethodInterceptor {

	private boolean isEnabled = true;

	private int callCount = 0;

	private long callTime = 0;


	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void reset() {
		this.callCount = 0;
		this.callTime = 0;
	}

	public int getCallCount() {
		return callCount;
	}

	public long getCallTime() {
		return callTime;
	}


	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		if (this.isEnabled) {
			this.callCount++;

			StopWatch sw = new StopWatch(methodInvocation.getMethod().getName());

			sw.start("invoke");
			Object retVal = methodInvocation.proceed();
			sw.stop();

			this.callTime = sw.getTotalTimeMillis();
			return retVal;
		}

		else {
			return methodInvocation.proceed();
		}
	}

}
