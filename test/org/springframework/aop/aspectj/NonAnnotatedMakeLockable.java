package org.springframework.aop.aspectj;

import org.springframework.aop.framework.Lockable;

public class NonAnnotatedMakeLockable {
	
	public static Lockable mixin;
	
	public void checkNotLocked(Lockable mixin) {
		if (mixin.locked()) {
			throw new IllegalStateException();
		}
	}

}
