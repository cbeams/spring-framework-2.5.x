/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;


public interface Lockable {
	void lock();
	void unlock();
	boolean locked();
}