package org.springframework.web.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ResponseTimeMonitorImpl;

/**
 * Listener that logs the response times of web requests.
 * To be registered in a WebApplicationContext.
 * @author Rod Johnson
 * @since January 21, 2001
 * @see RequestHandledEvent
 */
public class PerformanceMonitorListener implements ApplicationListener {

	protected final Log logger = LogFactory.getLog(getClass());

	protected ResponseTimeMonitorImpl responseTimeMonitor;

	public PerformanceMonitorListener() {
		this.responseTimeMonitor = new ResponseTimeMonitorImpl();
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof RequestHandledEvent) {
			RequestHandledEvent rhe = (RequestHandledEvent) event;
			// Could use one monitor per URL
			this.responseTimeMonitor.recordResponseTime(rhe.getTimeMillis());
			if (logger.isInfoEnabled()) {
				// Stringifying objects is expensive. Don't do it unless it will show.
				logger.info("PerformanceMonitorListener: last=" + rhe.getTimeMillis() + "ms; " +
										this.responseTimeMonitor + "; client was " + rhe.getIpAddress());
			}
		}
	}

}
