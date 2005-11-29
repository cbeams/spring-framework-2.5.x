package org.springframework.aop.support.aspectj;

public interface Lockable {
	
	void lock();
	
	void unlock();
	
	boolean isLocked();

}
