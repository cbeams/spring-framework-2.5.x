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
package org.springframework.web.flow;

import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.util.EventListenerListHelper;
import org.springframework.util.closure.ProcessTemplate;

/**
 * A strongly typed listener list class for FlowExecutionListeners.
 * 
 * @see org.springframework.web.flow.FlowExecutionListener
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionListenerList {

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
}