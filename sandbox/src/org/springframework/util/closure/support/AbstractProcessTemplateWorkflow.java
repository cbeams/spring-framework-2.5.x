/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.util.closure.support;

import org.springframework.util.closure.Closure;

/**
 * @author Keith Donald
 */
public abstract class AbstractProcessTemplateWorkflow extends AbstractProcessTemplate {

	protected AbstractProcessTemplateWorkflow() {
		super();
	}

	protected AbstractProcessTemplateWorkflow(boolean runOnce) {
		super(runOnce);
	}

	public final void run(Closure templateCallback) {
		reset();
		setRunning();
		doSetup();
		while (processing()) {
			templateCallback.call(doWork());
		}
		setCompleted();
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