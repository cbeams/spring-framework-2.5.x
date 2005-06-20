
package org.springframework.web.util;

import org.apache.log4j.NDC;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rob Harrop
 */
public class Log4jNestedDiagnosticContextFilter extends AbstractRequestLoggingFilter {

	protected void beforeRequest(HttpServletRequest request) {
		NDC.push(getMessage(request));
	}

	protected void afterRequest(HttpServletRequest request) {
		NDC.pop();
	}
}
