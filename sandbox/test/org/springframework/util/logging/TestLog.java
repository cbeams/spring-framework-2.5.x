/*
 * Created on Nov 7, 2004
 */
package org.springframework.util.logging;

import org.apache.commons.logging.impl.SimpleLog;


public class TestLog extends SimpleLog {
	private String name;
	
	public TestLog(String name) {
		super(name);
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}