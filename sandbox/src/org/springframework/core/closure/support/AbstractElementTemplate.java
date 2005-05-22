/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.core.closure.support;

import org.springframework.core.closure.Closure;
import org.springframework.core.closure.Constraint;
import org.springframework.core.closure.ElementClosureTemplate;

/**
 * Base superclass for process templates.
 * @author Keith Donald
 */
public abstract class AbstractElementTemplate implements ElementClosureTemplate {

	private ElementClosureTemplate wrappedTemplate;

	private boolean runOnce = false;

	private volatile ProcessStatus status = ProcessStatus.CREATED;

	protected AbstractElementTemplate() {

	}

	protected AbstractElementTemplate(boolean runOnce) {
		this.runOnce = runOnce;
	}

	private AbstractElementTemplate(ElementClosureTemplate wrappedTemplate) {
		this.wrappedTemplate = wrappedTemplate;
	}

	protected ElementClosureTemplate getWrappedTemplate() {
		return wrappedTemplate;
	}

	public boolean allTrue(Constraint constraint) {
		WhileTrueController controller = new WhileTrueController(this, constraint);
		run(controller);
		return controller.allTrue();
	}

	public boolean anyTrue(Constraint constraint) {
		return findFirst(constraint, null) != null;
	}

	public ElementClosureTemplate findAll(final Constraint constraint) {
		return new AbstractElementTemplate(this) {
			public void run(final Closure closure) {
				getWrappedTemplate().run(new ConstrainedBlock(closure, constraint));
			}
		};
	}

	public Object findFirst(Constraint constraint) {
		return findFirst(constraint, null);
	}

	public Object findFirst(Constraint constraint, Object defaultIfNoneFound) {
		ObjectFinder finder = new ObjectFinder(this, constraint);
		run(finder);
		return (finder.foundObject() ? finder.getFoundObject() : defaultIfNoneFound);
	}

	public boolean isStopped() {
		return this.status == ProcessStatus.STOPPED;
	}

	public boolean isFinished() {
		return this.status == ProcessStatus.COMPLETED;
	}

	public boolean isRunning() {
		return this.status == ProcessStatus.RUNNING;
	}

	public void stop() throws IllegalStateException {
		if (this.wrappedTemplate != null) {
			wrappedTemplate.stop();
		}
		this.status = ProcessStatus.STOPPED;
	}

	public Object doWithClosure(Closure closure) {
		run(closure);
		return null;
	}

	public void runUntil(Closure templateCallback, final Constraint constraint) {
		run(new UntilTrueController(this, templateCallback, constraint));
	}

	protected void reset() {
		if (this.status == ProcessStatus.STOPPED || this.status == ProcessStatus.COMPLETED) {
			if (this.runOnce) {
				throw new UnsupportedOperationException("This process template can only safely execute once; "
						+ "instantiate a new instance per request");
			}
			else {
				this.status = ProcessStatus.RESET;
			}
		}
	}

	protected void setRunning() {
		this.status = ProcessStatus.RUNNING;
	}

	protected void setCompleted() {
		this.status = ProcessStatus.COMPLETED;
	}

	public abstract void run(Closure templateCallback);

	private static class WhileTrueController extends Block {
		private ElementClosureTemplate template;

		private Constraint constraint;

		private boolean allTrue = true;

		public WhileTrueController(ElementClosureTemplate template, Constraint constraint) {
			this.template = template;
			this.constraint = constraint;
		}

		protected void handle(Object o) {
			if (!this.constraint.test(o)) {
				this.allTrue = false;
				this.template.stop();
			}
		}

		public boolean allTrue() {
			return allTrue;
		}
	}

	private static class UntilTrueController extends Block {
		private ElementClosureTemplate template;

		private Closure templateCallback;

		private Constraint constraint;

		private boolean allTrue = true;

		public UntilTrueController(ElementClosureTemplate template, Closure templateCallback, Constraint constraint) {
			this.template = template;
			this.templateCallback = templateCallback;
			this.constraint = constraint;
		}

		protected void handle(Object o) {
			if (this.constraint.test(o)) {
				this.template.stop();
			}
			else {
				this.templateCallback.call(o);
			}
		}
	}

	private static class ObjectFinder extends Block {
		private ElementClosureTemplate template;

		private Constraint constraint;

		private Object foundObject;

		public ObjectFinder(ElementClosureTemplate template, Constraint constraint) {
			this.template = template;
			this.constraint = constraint;
		}

		protected void handle(Object o) {
			if (this.constraint.test(o)) {
				foundObject = o;
				template.stop();
			}
		}

		public boolean foundObject() {
			return foundObject != null;
		}

		public Object getFoundObject() {
			return foundObject;
		}
	}

}