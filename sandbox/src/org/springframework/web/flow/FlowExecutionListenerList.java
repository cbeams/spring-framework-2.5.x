/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.util.EventListenerListHelper;
import org.springframework.util.closure.ProcessTemplate;

/**
 * A strongly typed listener list class for FlowExecutionListeners.
 * @author Keith Donald
 */
public class FlowExecutionListenerList {

	/**
	 * The list of listeners that should receive event callbacks during managed
	 * flow executions (client sessions).
	 */
	private EventListenerListHelper flowExecutionListeners = new EventListenerListHelper(FlowExecutionListener.class);

	/**
	 * @param listener
	 */
	public boolean add(FlowExecutionListener listener) {
		return this.flowExecutionListeners.add(listener);
	}

	/**
	 * @param listeners
	 */
	public boolean add(FlowExecutionListener[] listeners) {
		return this.flowExecutionListeners.addAll(listeners);
	}

	/**
	 * @param flowExecutionListenerList
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
	 * @param listener
	 */
	public void remove(FlowExecutionListener listener) {
		this.flowExecutionListeners.remove(listener);
	}

	/**
	 * 
	 */
	public void clear() {
		this.flowExecutionListeners.clear();
	}

	/**
	 * Is at least one instance of the provided FlowExecutionListener
	 * implementation present in the listener list?
	 * @param listenerImplementationClass The flow execution listener
	 *        implementation, must be a impl of FlowExecutionListener
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
	 * @param listener The execution listener
	 * @return true if present, false otherwise.
	 */
	public boolean isAdded(FlowExecutionListener listener) {
		return this.flowExecutionListeners.isAdded(listener);
	}

	/**
	 * @return
	 */
	public Iterator iterator() {
		return this.flowExecutionListeners.iterator();
	}

	/**
	 * Return a process template that knows how to iterate over the list of flow
	 * execution listeners and dispatch each listener to a handler callback for
	 * processing.
	 * @return The iterator process template.
	 */
	public ProcessTemplate iteratorTemplate() {
		return flowExecutionListeners;
	}

	/**
	 * Returns the number of execution listeners associated with this flow
	 * execution.
	 * @return The flow execution listener count
	 */
	public int size() {
		return flowExecutionListeners.getListenerCount();
	}
}