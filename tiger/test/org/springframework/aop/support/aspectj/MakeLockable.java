package org.springframework.aop.support.aspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class MakeLockable {
	
	public static class DefaultLockable implements Lockable {
		
		private boolean locked;

		public void lock() {
			this.locked = true;
		}

		public void unlock() {
			this.locked = false;
		}

		public boolean isLocked() {
			return this.locked;
		}
	}
	
	@DeclareParents("org.springframework.aop.support.aspectj.NotLockable")
	private Lockable mixin = new DefaultLockable();

}
