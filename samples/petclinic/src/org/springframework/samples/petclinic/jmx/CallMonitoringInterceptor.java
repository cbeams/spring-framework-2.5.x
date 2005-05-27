
package org.springframework.samples.petclinic.jmx;

import org.springframework.util.StopWatch;
import org.aopalliance.intercept.MethodInvocation;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * 
 * @author robh
 */
public class CallMonitoringInterceptor implements MethodInterceptor, CallMonitor {

    private int callCount = 0;

    private long callTime;

    private boolean isEnabled = true;

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (this.isEnabled) {
            StopWatch sw = new StopWatch(methodInvocation.getMethod().getName());

            sw.start("invoke");
            Object retval = methodInvocation.proceed();
            sw.stop();

            this.callTime = sw.getTotalTimeMillis();
            this.callCount++;
            return retval;
        }
        else {
            return methodInvocation.proceed();
        }
    }


    public int getCallCount() {
        return callCount;
    }

    public long getCallTime() {
        return callTime;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}

