/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import org.apache.commons.logging.Log;

/**
 * @author robh
 *
 */
public class LogTestBean {

	private Log log;
	
	public void setLog(Log log) {
		this.log = log;
	}
	
	public Log getLog() {
		return log;
	}
}
