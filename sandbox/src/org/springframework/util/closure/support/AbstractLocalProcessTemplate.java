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
package org.springframework.util.closure.support;

import org.springframework.util.closure.Closure;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.ProcessTemplate;

/**
 * Base superclass for process templates.
 * <p>
 * Note: instances deriving from this class should generally be created on a per
 * request basis. This 'local' implementation is not designed to be reused after
 * one use, or by use in multiple threads.
 * @author Keith Donald
 */
public abstract class AbstractLocalProcessTemplate implements ProcessTemplate {

	private ProcessTemplate wrappedTemplate;

	private boolean stopped;

	protected AbstractLocalProcessTemplate() {

	}

	protected AbstractLocalProcessTemplate(ProcessTemplate wrappedTemplate) {
		this.wrappedTemplate = wrappedTemplate;
	}

	protected ProcessTemplate getWrappedTemplate() {
		return wrappedTemplate;
	}

	public boolean allTrue(Constraint constraint) {
		TemplateController controller = new TemplateController(this, constraint);
		run(controller);
		return controller.allTrue();
	}

	public boolean anyTrue(Constraint constraint) {
		return findFirst(constraint, null) != null;
	}

	public ProcessTemplate findAll(final Constraint constraint) {
		return new AbstractLocalProcessTemplate(this) {
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
		return this.stopped;
	}

	public void stop() throws IllegalStateException {
		if (this.wrappedTemplate != null) {
			wrappedTemplate.stop();
		}
		this.stopped = true;
	}

	public void runUntil(Closure templateCallback, final Constraint constraint) {
		run(new TemplateController(this, constraint, templateCallback));
	}

	public abstract void run(Closure templateCallback);

	private static class TemplateController extends Block {
		private ProcessTemplate template;

		private Constraint constraint;

		private Closure templateCallback;

		private boolean allTrue = true;

		public TemplateController(ProcessTemplate template, Constraint constraint) {
			this.template = template;
			this.constraint = constraint;
		}

		public TemplateController(ProcessTemplate template, Constraint constraint, Closure templateCallback) {
			this.template = template;
			this.constraint = constraint;
			this.templateCallback = templateCallback;
		}

		protected void handle(Object o) {
			if (!this.constraint.test(o)) {
				allTrue = false;
				this.template.stop();
			}
			else {
				if (this.templateCallback != null) {
					this.templateCallback.call(o);
				}
			}
		}

		public boolean allTrue() {
			return allTrue;
		}
	}

	private static class ObjectFinder extends Block {
		private ProcessTemplate template;

		private Constraint constraint;

		private Object foundObject;

		public ObjectFinder(ProcessTemplate template, Constraint constraint) {
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