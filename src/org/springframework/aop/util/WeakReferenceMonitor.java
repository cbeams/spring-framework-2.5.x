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

package org.springframework.aop.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Track references to arbitrary objects using proxy and weak references. To
 * monitor an object one should call {@link #monitor(Object, ReleaseListener)}
 * and use returned handle (which is a proxy) as a reference instead of target
 * object itself.
 * 
 * When created handle becomes weakly reachable specified listener is called by
 * the background thread.
 * 
 * @author Tomasz Wysocki
 */
public class WeakReferenceMonitor {

	private static final Logger log = Logger.getLogger(WeakReferenceMonitor.class);

	private static class MonitoringProcess implements Runnable {

		public void run() {
			log.info("Monitoring thread started.");
			try {
				// Check if there are any tracked entries left
				while (!trackedEntries.isEmpty()) {
					// log.debug("Waiting for tracked entries:" +
					// trackedEntries);

					try {
						// log.debug("Waiting for weak reference on " +
						// handleQueue);
						Reference reference = handleQueue.remove();
						// log.debug("Obtained weak reference " + reference + "
						// on " + handleQueue);

						// stop tracking this reference
						Entry entry = remove(reference);

						// check if found, it should be.
						if (entry != null) {
							entry.notifyRelease();
						}
						else {
							log.error("No entry found for :" + reference);
						}

					}
					catch (InterruptedException e) {
						log.debug("Monitoring thread interrupted.", e);
						break;
					}
				}
			}
			finally {
				log.info("Finishing monitoring thread.");
				monitoringThread = null;
			}
		}
	}

	/**
	 * Entry for tracked targets. Entries are stored in trackedEntries map where
	 * key is a weak reference to target's handle (client proxy).
	 */
	private static class Entry {

		// Strong reference to target
		Object target;

		// Listener called when target's handle become weakly reachable
		ReleaseListener listener;

		public Entry(Object target, ReleaseListener listener) {
			this.target = target;
			this.listener = listener;
		}

		/** Notify registered listener of release event */
		public void notifyRelease() {
			// was listener registered for given entry?
			if (listener != null) {
				listener.notifyRelease(target);
			}
		}

	}

	/** Listener is notified when object is being released. */
	public static interface ReleaseListener {

		/**
		 * Given object is being released, ie. there are no monitored strong
		 * references to it.
		 * 
		 * @param object
		 *            being released
		 */
		void notifyRelease(Object object);

	}

	// queue receiving reachability events
	private static final ReferenceQueue handleQueue = new ReferenceQueue();

	// all tracked entries (WeakReference => Entry)
	private static final Map trackedEntries = Collections.synchronizedMap(new HashMap());

	// thread polling handleQueue, lazy initialized
	private static Thread monitoringThread = null;

	/**
	 * Start to monitor given target object for becoming weakly reachable. When
	 * returned handle is not used anymore given listener will be called.
	 * 
	 * Returned handle is in fact only a proxy to the <code>target</code>
	 * object. Note that this implementation proxies only interfaces of the
	 * target object! It may be extended in the future to support proxying of
	 * arbitrary classes.
	 * 
	 * @param target
	 *            target that will be monitored
	 * @param proxyTargetClass
	 *            indicates if the target should be proxied by interfaces only
	 *            (if the value is false), or also by class via a code-generated
	 *            proxy (if the value is true). It's normally recommended to
	 *            proxy by interface only, if possible. Note that any object
	 *            which does implement any interfaces will have to be proxied by
	 *            class, regardless of this flag.
	 * @param listener
	 *            that will be called upon target's handle release
	 * 
	 * @return handle that should be used instead of original object
	 */
	public static Object monitor(Object target, boolean proxyTargetClass,
			ReleaseListener listener) {
		log.debug("Monitoring target:" + target + " with release listener :" + listener);

		// create handle to target object
		// this handle will be returned to the client
		ProxyFactory pf = new ProxyFactory(target);
		pf.setProxyTargetClass(proxyTargetClass);
		Object handle = pf.getProxy();

		// make weak reference to this handle, so we can say when
		// handle is not used any more by polling on handleQueue
		WeakReference weakRef = new WeakReference(handle, handleQueue);

		// add monitored entry to internal map of all monitored entries
		add(weakRef, new Entry(target, listener));

		return handle;
	}

	/**
	 * Add entry to internal map of tracked entries. Internal polling thread is
	 * started if this is the first tracked entry.
	 * 
	 * @param ref
	 *            reference to tracked object handle (client proxy)
	 * @param entry
	 *            associated entry
	 */
	private static void add(Reference ref, Entry entry) {
		log.debug("Adding entry :" + entry);
		// add entry, the key is given reference
		trackedEntries.put(ref, entry);

		// start lazily thread polling handleQueue
		if (!isMonitoringThreadRunning()) {
			log.debug("Monitoring thread not yet started.");
			monitoringThread = new Thread(new MonitoringProcess(), WeakReferenceMonitor.class
					.getName());
			monitoringThread.start();
		}
	}

	/**
	 * Remove entry from internal map of tracked entries. Entry is identified by
	 * associated reference used as a key. Entry should have been registered
	 * with <code> add </code> method.
	 * 
	 * @param reference
	 *            reference that should be removed
	 * @return entry object associated with given reference, or
	 *         <code> null </code> if not found.
	 */
	private static Entry remove(Reference reference) {
		log.debug("Removing reference :" + reference);

		// remove entry and return it
		return (Entry) trackedEntries.remove(reference);

	}

	/** Check if monitoring thread is actually running */
	static boolean isMonitoringThreadRunning() {
		return monitoringThread != null;
	}
}