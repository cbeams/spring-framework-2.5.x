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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
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
	private Set flowExecutionListeners = new HashSet();

	/**
	 * Add a listener.
	 * @param listener the listener to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListener listener) {
		if (listener == null) {
			return false;
		}
		else {
			return this.flowExecutionListeners.add(listener);
		}
	}

	/**
	 * Add a set of listeners.
	 * @param listeners the listeners to add
	 * @return true if the underlying listener list changed, false otherwise
	 */
	public boolean add(FlowExecutionListener[] listeners) {
		if (listeners == null) {
			return false;
		}
		else {
			boolean changed = false;
			for (int i = 0; i < listeners.length; i++) {
				changed = changed || add(listeners[i]);
			}
			return changed;
		}
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
		else {
			boolean changed = false;
			Iterator it = flowExecutionListenerList.iterator();
			while (it.hasNext()) {
				changed = changed || add((FlowExecutionListener)it.next());
			}
			return changed;
		}
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
	 * Is at least one instance of the provided FlowExecutionListener implementation
	 * present in the listener list?
	 * @param listenerImplementationClass the flow execution listener
	 *        implementation, must be an implementation of FlowExecutionListener
	 * @return true if present, false otherwise
	 */
	public boolean isAdded(Class listenerImplementationClass) {
		Assert.isTrue(FlowExecutionListener.class.isAssignableFrom(listenerImplementationClass),
				"Listener class must be a FlowExecutionListener");
		for (Iterator it = this.flowExecutionListeners.iterator(); it.hasNext(); ) {
			if (it.next().getClass().equals(listenerImplementationClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the provided FlowExecutionListener instance present in the listener list?
	 * @param listener the execution listener
	 * @return true if present, false otherwise.
	 */
	public boolean isAdded(FlowExecutionListener listener) {
		return this.flowExecutionListeners.contains(listener);
	}

	/**
	 * Returns an iterator looping over all listeners in this list.
	 */
	public Iterator iterator() {
		return this.flowExecutionListeners.iterator();
	}

	/**
	 * Returns the number of execution listeners in this list.
	 * @return the flow execution listener count
	 */
	public int size() {
		return flowExecutionListeners.size();
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
		return (FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[size()]);
	}
	
	// methods to fire events to all listeners
	
	/**
	 * Notify all interested listeners that a request was submitted to the flow
	 * execution.
	 */
	public void fireRequestSubmitted(RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request submitted event to " + size() + " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).requestSubmitted(context);
		}
	}

	/**
	 * Notify all interested listeners that the flow execution finished
	 * processing a request.
	 */
	public void fireRequestProcessed(RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request processed event to " + size()	+ " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).requestProcessed(context);
		}
	}
	
	/**
	 * Notify all interested listeners that a flow execution session is starting.
	 */
	public void fireSessionStarting(RequestContext context, State startState, Map input) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution starting event to " + size() + " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionStarting(context, startState, input);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session has started.
	 */
	public void fireSessionStarted(RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution started event to " + size()	+ " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionStarted(context);
		}
	}

	/**
	 * Notify all interested listeners that an event was signaled in the flow
	 * execution.
	 */
	public void fireEventSignaled(RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event signaled event to " + size()	+ " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).eventSignaled(context);
		}
	}

	/**
	 * Notify all interested listeners that a state is being entered in the
	 * flow execution.
	 */
	public void fireStateEntering(RequestContext context, State nextState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state entering event to " + size()	+ " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).stateEntering(context, nextState);
		}
	}

	/**
	 * Notify all interested listeners that a state was entered in the
	 * flow execution.
	 */
	public void fireStateEntered(RequestContext context, State previousState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state entered event to " + size() + " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).stateEntered(context, previousState, context.getFlowContext().getCurrentState());
		}
	}

	/**
	 * Notify all interested listeners that a flow session was activated in the
	 * flow execution.
	 */
	public void fireResumed(RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing resumed event to " + size() + " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).resumed(context);
		}
	}

	/**
	 * Notify all interested listeners that a flow session was paused in the
	 * flow execution.
	 */
	public void firePaused(RequestContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing paused event to " + size()	+ " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).paused(context);
		}
	}

	/**
	 * Notify all interested listeners that a flow execution session has ended.
	 */
	public void fireSessionEnded(RequestContext context, FlowSession endedSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution ended event to " + size() + " listener(s)");
		}
		for (Iterator it=iterator(); it.hasNext(); ) {
			((FlowExecutionListener)it.next()).sessionEnded(context, endedSession);
		}
	}
}