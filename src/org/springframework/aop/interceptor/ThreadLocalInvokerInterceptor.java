/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.SimpleIntroductionAdvice;
import org.springframework.beans.factory.DisposableBean;

/**
 * Alternative to an object pool. This invoker uses a threading model in which
 * every thread has its own copy of the target. There's no contention for targets.
 * Target object creation is kept to a minimum on the running server.
 *
 * <p>Application code is written as to a normal pool; callers can't assume they
 * will be dealing with the same instance in invocations in different threads.
 * However, state can be relied on during the operations of a single thread.
 *
 * <p>Cleanup is performed in the destroy() method from DisposableBean.
 * We can't get at the ThreadLocals there, but we can use a layer of indirection
 * to clear the references they hold.
 *
 * <p><b>This pooling model should be considered alpha. It has not yet been
 * tested in production.</b>
 *
 * @author Rod Johnson
 * @version $Id: ThreadLocalInvokerInterceptor.java,v 1.2 2003-11-24 21:48:06 jhoeller Exp $
 */
public class ThreadLocalInvokerInterceptor extends PrototypeInvokerInterceptor implements ThreadLocalInvokerStats, DisposableBean {
	
	/**
	 * ThreadLocal holding the target associated with the current
	 * thread.
	 */
	private static ThreadLocal holders = new ThreadLocal();
	
	/**
	 * Level of indirection so that we can clear the references out of
	 * the ThreadLocals without actually clearing the ThreadLocals.
	 */
	private static class Holder {
		private Holder(Object target) {
			this.target = target;
		}
		public Object target;
	}


	/**
	 * Set of managed holders, enabling us to keep track
	 * of the targets we've created.
	 */
	private Set holderSet = new HashSet();
	
	private int invocations;
	
	private int hits;
	
	/**
	 * Implementation of abstract getTarget() method.
	 * We look for a target held in a ThreadLocal. If
	 * we don't find one, we create one and bind it to the thread.
	 * No synchronization is required.
	 */
	public Object getTarget() {
		++invocations;
		Holder targetHolder = (Holder) holders.get();
		if (targetHolder == null || targetHolder.target == null) {
			logger.info("No target for apartment prototype '" + getTargetBeanName() + 
					"' found in thread: creating one and binding it to thread '" + Thread.currentThread().getName() + "'");
			// Associate target with thread local
			targetHolder = new Holder(super.getTarget());
			holders.set(targetHolder);
			this.holderSet.add(targetHolder);
		}
		else {
			++hits;
		}
		
		return targetHolder.target;
	}

	/**
	 * We can't get at the ThreadLocals, but we can clear
	 * their holders.
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() {
		logger.info("Destroying ThreadLocal bindings");
		for (Iterator itr = this.holderSet.iterator(); itr.hasNext(); ) {
			Holder holder = (Holder) itr.next();
			if (holder.target instanceof DisposableBean) {
				try {
					((DisposableBean) holder.target).destroy();
				}
				catch (Exception ex) {
					// Do nothing
					logger.warn("Thread-bound target of class '" + holder.target.getClass() + 
							"' threw exception from destroy() method", ex);
				}
			}
			holder.target = null;
		}
		this.holderSet.clear();
	}

	public int getInvocations() {
		return invocations;
	}

	public int getHits() {
		return hits;
	}

	public int getObjects() {
		return holderSet.size();
	}

	/**
	 * Return an introduction advisor mixin that allows the AOP proxy to be
	 * case to ThreadLocalInvokerStats.
	 */
	public InterceptionIntroductionAdvisor getStatsMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new SimpleIntroductionAdvice(dii, ThreadLocalInvokerStats.class);
	}

}
