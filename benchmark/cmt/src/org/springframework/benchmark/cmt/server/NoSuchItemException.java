/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

/**
 * 
 * @author Rod Johnson
 * @version $Id: NoSuchItemException.java,v 1.1 2003-12-02 18:31:09 johnsonr Exp $
 */
public class NoSuchItemException extends Exception {
	
	private final long id;
	
	public NoSuchItemException(long id) {
		super("No item with id=" + id);
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

}
