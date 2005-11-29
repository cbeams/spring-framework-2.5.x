package org.springframework.aop.support.aspectj;

public class CannotBeUnlocked implements Lockable, Comparable {

	public void lock() {
	}

	public void unlock() {
		throw new UnsupportedOperationException();
	}

	public boolean isLocked() {
		return true;
	}

	public int compareTo(Object arg0) {
		throw new UnsupportedOperationException();
	}

}
