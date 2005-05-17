/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.execution;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.EventListenerListHelper;
import org.springframework.core.closure.ProcessTemplate;
import org.springframework.core.closure.support.Block;
import org.springframework.util.Assert;
import org.springframework.web.flow.FlowContext;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;

/**
 * A strongly typed listener list class for FlowExecutionListeners. It helps
 * in managing a list of <code>FlowExecutionListener</code>s.
 * 
 * @see org.springframework.web.flow.execution.FlowExecutionListener
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionListenerList {

	protected final Log logger = LogFactory.getLog(FlowExecutionListenerList.class);
	
	/**
	 * The list of listeners that should receive event callbacks during managed
	 * flow executions (client sessions).
	 */
	private EventListenerListHelper flowExecutionListeners = new EventListenerListHelper(FlowExecutionListener.class);

	/**
	 * Add a listener.
	 * @param listener the listener to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListener listener) {
		return this.flowExecutionListeners.add(listener);
	}

	/**
	 * Add a set of listeners.
	 * @param listeners the listeners to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListener[] listeners) {
		return this.flowExecutionListeners.addAll(listeners);
	}

	/**
	 * Add a list of listeners.
	 * @param flowExecutionListenerList the listeners to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListenerList flowExecutionListenerList) {
		if (flowExecutionListenerList == null) {
			return false;
		}
		Iterator it = flowExecutionListenerList.iterator();
		boolean changed = false;
		while (it.hasNext()) {
			if (add((FlowExecutionListener)it.next())) {
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Remove a listener from the list.
	 * @param listener the listener to remove
	 */
	public void remove(FlowExecutionListener listener) {
		this.flowExecutionListeners.remove(listener);
	}

	/**
	 * Remove all listeners from the list.
	 */
	public void clear() {
		this.flowExecutionListeners.clear();
	}

	/**
	 * Is at least one instance of the provided FlowExecutionListener
	 * implementation present in the listener list?
	 * @param listenerImplementationClass the flow execution listener
	 *        implementation, must be an implementation of FlowExecutionListener
	 * @return true if present, false otherwise
	 */
	public boolean isAdded(Class listenerImplementationClass) {
		Assert.isTrue(FlowExecutionListener.class.isAssignableFrom(listenerImplementationClass),
				"Listener class must be a FlowExecutionListener");
		return this.flowExecutionListeners.isAdded(listenerImplementationClass);
	}

	/**
	 * Is the provid FlowExecutionListener instance present in the listener
	 * list?
	 * @param listener the execution listener
	 * @return true if present, false otherwise.
	 */
	public boolean isAdded(FlowExecutionListener listener) {
		return this.flowExecutionListeners.isAdded(listener);
	}

	/**
	 * Returns an iterator looping over all listeners in this list.
	 */
	public Iterator iterator() {
		return this.flowExecutionListeners.iterator();
	}

	/**
	 * Return a process template that knows how to iterate over the list of flow
	 * execution listeners and dispatch each listener to a handler callback for
	 * processing.
	 * @return the iterator process template.
	 */
	public ProcessTemplate iteratorTemplate() {
		return flowExecutionListeners.iteratorTemplate();
	}

	/**
	 * Returns the number of execution listeners in this list.
	 * @return the flow execution listener count
	 */
	public int size() {
		return flowExecutionListeners.getListenerCount();
	}

	/**
	 * Is this listener list empty?
	 * @return true or false
	 */
	public boolean isEmpty() {
		return flowExecutionListeners.isEmpty();
	}

	/**
	 * Returns the listeners in this list as an array.
	 */
	public FlowExecutionListener[] toArray() {
		return (FlowExecutionListener[])flowExecutionListeners.toArray();
	}
	
	// methods to fire events to all listeners

	/**
	 * Notify all interested listeners that a request was submitted to the flow
	 * execution.
	 */
	public void fireRequestSubmitted(final RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request submitted event to " + size() + " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestSubmitted(context);
			}
		});
	}

	/**
	 * Notify all interested listeners that the flow execution finished
	 * processing a request.
	 */
	public void fireRequestProcessed(final RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request processed event to " + size()	+ " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestProcessed(context);
			}
		});
	}
	
	/**
	 * Notify all interested listeners that a flow execution session is starting.
	 */
	public void fireSessionStarting(final RequestContext context, final State startState, final Map input) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution starting event to " + size() + " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).sessionStarting(context, startState, input);
			}
		});
	}

	/**
	 * Notify all interested listeners that a flow execution session has started.
	 */
	public void fireSessionStarted(final RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution started event to " + size()	+ " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).sessionStarted(context);
			}
		});
	}

	/**
	 * Notify all interested listeners that an event was signaled in the flow
	 * execution.
	 */
	public void fireEventSignaled(final RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event signaled event to " + size()	+ " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(context);
			}
		});
	}

	/**
	 * Notify all interested listeners that a state is being entered in the
	 * flow execution.
	 */
	public void fireStateEntering(final RequestContext context, final State nextState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state entering event to " + size()	+ " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateEntering(context, nextState);
			}
		});
	}

	/**
	 * Notify all interested listeners that a state was entered in the
	 * flow execution.
	 */
	public void fireStateEntered(final RequestContext context, final State previousState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state entered event to " + size() + " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateEntered(context, previousState, context.getFlowContext().getCurrentState());
			}
		});
	}

	/**
	 * Notify all interested listeners that a flow session was activated in the
	 * flow execution.
	 */
	public void fireResumed(final RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing resumed event to " + size() + " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).resumed(context);
			}
		});
	}

	/**
	 * Notify all interested listeners that a flow session was paused in the
	 * flow execution.
	 */
	public void firePaused(final RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing paused event to " + size()	+ " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).paused(context);
			}
		});
	}

	/**
	 * Notify all interested listeners that a flow execution session has ended.
	 */
	public void fireEnded(final RequestContext context, final FlowSession endedSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution ended event to " + size() + " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).ended(context, endedSession);
			}
		});
	}

	/**
	 * Notify all interested listeners that the flow execution has expired.
	 */
	public void fireExpired(final FlowContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution expired event to " + size() + " listener(s)");
		}
		iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).expired(context);
			}
		});
	}

}