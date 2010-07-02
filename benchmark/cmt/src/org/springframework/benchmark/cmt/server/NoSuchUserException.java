/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

/**
 * 
 * @author Rod Johnson
 */
public class NoSuchUserException extends Exception {
	
	private static final long serialVersionUID = 1849150985978495525L;
	
	private long id;
	
	public NoSuchUserException(long id) {
		super("No user with id " + id);
		this.id = id;
	}

	/**
	 * @return Returns the id.
	 */
	public long getId() {
		return this.id;
	}

}
