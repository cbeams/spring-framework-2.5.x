/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.bcel;

import java.util.Date;

/**
 * A class that will be used in the tests.  Attributes will be added
 * to various program elements.
 * @author Mark Pollack
 * @since Sep 28, 2003
 * 
 */
public class TargetBean {
 
 	/**
 	 * A public field
 	 */
 	public int height;
 
 	/**
 	 * do nothing constructor
 	 *
 	 */
 	public TargetBean() {
 		
 	}
 	
 	/**
 	 * A simple method.
 	 *
 	 */
 	public void doReport() {
 		
 	}
 
 	/**
 	 * A method with a more complex signature.
	 * @param who who
	 * @param what what
 	 * @param where where
	 * @param when when
	 * @param why why
	 * @return
	 */
 	public int doWork(String who, int what, float where, Date when, Object why) {
 		return 1;	
 	}
}
