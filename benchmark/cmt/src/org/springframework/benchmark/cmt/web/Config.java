/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.web;

/**
 * 
 * @author Rod Johnson
 */
public class Config {
	
	private String mode = "pojo";
	

	/**
	 * @return Returns the mode.
	 */
	public String getMode() {
		return this.mode;
	}
	/**
	 * @param mode The mode to set.
	 */
	public void setMode(String mode) {
		System.out.println(">>>>>>>>>>>>>>>>>>> Mode set to '" + mode + "'");
		this.mode = mode;
	}
}
