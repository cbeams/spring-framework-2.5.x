/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

/**
 * 
 * @author Rod Johnson
 */
public class NoSuchItemException extends Exception {
	
	private static final long serialVersionUID = 5774997739392295541L;
	
	private final long id;
	
	public NoSuchItemException(long id) {
		super("No item with id=" + id);
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

}
