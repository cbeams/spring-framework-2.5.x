/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.util.closure.support;

import org.springframework.util.closure.Closure;

/**
 * @author Keith Donald
 */
public abstract class AbstractLocalProcessTemplateWorkflow extends AbstractLocalProcessTemplate {

	public final void run(Closure templateCallback) {
		doSetup();
		while (processing()) {
			templateCallback.call(doWork());
		}
		doCleanup();
	}

	protected void doSetup() {

	}

	protected boolean processing() {
		return hasMoreWork() && !isStopped();
	}

	protected abstract boolean hasMoreWork();

	protected abstract Object doWork();

	protected void doCleanup() {

	}
}