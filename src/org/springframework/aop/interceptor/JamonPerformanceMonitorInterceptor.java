package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Performance monitor interceptor that uses <b>Jamon </b> library to perfom the performance mesurment on the
 * intercepted method and output the stats.
 * <p>
 * This code is inspired by Thierry Templier's blog
 * 
 * @author Dmitriy Kopylenko
 * @since 1.1.3
 */
public class JamonPerformanceMonitorInterceptor implements MethodInterceptor, Serializable {

    protected static final Log logger = LogFactory.getLog(JamonPerformanceMonitorInterceptor.class);
    
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Monitor mon = MonitorFactory.start(invocation.toString());
        Object rval = invocation.proceed();
        mon.stop();
        logger.info("The performance statistics for " + invocation +":\n" + mon);

        return rval;
    }
}
