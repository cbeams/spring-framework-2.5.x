/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.target;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.DisposableBean;

/**
 * Alternative to an object pool. This TargetSource uses a threading model in which
 * every thread has its own copy of the target. There's no contention for targets.
 * Target object creation is kept to a minimum on the running server.
 *
 * <p>Application code is written as to a normal pool; callers can't assume they
 * will be dealing with the same instance in invocations in different threads.
 * However, state can be relied on during the operations of a single thread:
 * for example, if one caller makes repeated calls on the AOP proxy.
 *
 * <p>Cleanup is performed in the destroy() method from DisposableBean.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #destroy
 */
public final class ThreadLocalTargetSource extends AbstractPrototypeBasedTargetSource
		implements ThreadLocalTargetSourceStats, DisposableBean {
	
	/**
	 * ThreadLocal holding the target associated with the current
	 * thread. Unlike most ThreadLocals, which are static, this variable
	 * is meant to be per thread per instance of the ThreadLocalTargetSource class.
	 */
	private ThreadLocal targetInThread = new ThreadLocal();

	/**
	 * Set of managed targets, enabling us to keep track of the targets we've created.
	 */
	private final Set targetSet = Collections.synchronizedSet(new HashSet());
	
	private int invocations;
	
	private int hits;
	
	/**
	 * Implementation of abstract getTarget() method.
	 * We look for a target held in a ThreadLocal. If we don't find one,
	 * we create one and bind it to the thread. No synchronization is required.
	 */
	public Object getTarget() {
		++this.invocations;
		Object target = this.targetInThread.get();
		if (target == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No target for prototype '" + getTargetBeanName() + "' found in thread: " +
				    "creating one and binding it to thread '" + Thread.currentThread().getName() + "'");
			}
			// associate target with ThreadLocal
			target = newPrototypeInstance();
			this.targetInThread.set(target);
			this.targetSet.add(target);
		}
		else {
			++this.hits;
		}
		return target;
	}
	
	public void releaseTarget(Object target) {
		// do nothing
	}

	/**
	 * Dispose of targets if necessary; clear ThreadLocal.
	 */
	public void destroy() {
		logger.info("Destroying ThreadLocalTargetSource bindings");
		for (Iterator it = this.targetSet.iterator(); it.hasNext(); ) {
			Object target = it.next();
			if (target instanceof DisposableBean) {
				try {
					((DisposableBean) target).destroy();
				}
				catch (Exception ex) {
					// do nothing
					logger.warn("Thread-bound target of class [" + target.getClass() +
					    "] threw exception from destroy() method", ex);
				}
			}
		}
		this.targetSet.clear();
		
		// clear ThreadLocal
		this.targetInThread = null;
	}

	public int getInvocationCount() {
		return invocations;
	}

	public int getHitCount() {
		return hits;
	}

	public int getObjectCount() {
		return targetSet.size();
	}

	/**
	 * @deprecated in favor of getInvocationCount
	 * @see #getInvocationCount
	 */
	public int getInvocations() {
		return invocations;
	}

	/**
	 * @deprecated in favor of getHitCount
	 * @see #getHitCount
	 */
	public int getHits() {
		return hits;
	}

	/**
	 * @deprecated in favor of getObjectCount
	 * @see #getObjectCount
	 */
	public int getObjects() {
		return targetSet.size();
	}

	/**
	 * Return an introduction advisor mixin that allows the AOP proxy to be
	 * cast to ThreadLocalInvokerStats.
	 */
	public IntroductionAdvisor getStatsMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, ThreadLocalTargetSourceStats.class);
	}

}
