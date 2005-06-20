
package org.springframework.web.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author robh
 */
public class RequestLoggingFilter extends AbstractRequestLoggingFilter{

	private Log logger = LogFactory.getLog(RequestLoggingFilter.class);

	protected void beforeRequest(HttpServletRequest request) {
		if(logger.isDebugEnabled()) {
			logger.debug("Processing Request: " + getMessage(request));
		}
	}

	protected void afterRequest(HttpServletRequest request) {
		if(logger.isDebugEnabled()) {
			logger.debug("Processed Request: " + getMessage(request));
		}
	}
}
