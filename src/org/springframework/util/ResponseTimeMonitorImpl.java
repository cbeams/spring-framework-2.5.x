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

package org.springframework.util;

import java.util.Date;

/**
 * Implementation of ResponseTimeMonitor for use via delegation by
 * objects that implement this interface.
 *
 * <p>Uses no synchronization, so is suitable for use in a web application.
 *
 * @author Rod Johnson
 * @since November 21, 2000
 * @version $Id: ResponseTimeMonitorImpl.java,v 1.4 2004-04-08 16:24:57 jhoeller Exp $
 */
public class ResponseTimeMonitorImpl implements ResponseTimeMonitor {

	/** The number of operations recorded by this object */
	private int accessCount;

	/** The system time at which this object was initialized */
	private long initedMillis;

	/** The sum of the response times for all operations */
	private int totalResponseTimeMillis = 0;

	/** The best response time this object has recorded */
	private int bestResponseTimeMillis = Integer.MAX_VALUE;

	/** The worst response time this object has recorded */
	private int worstResponseTimeMillis = Integer.MIN_VALUE;

	/**
	 * Create a new ResponseTimeMonitorImpl.
	 */
	public ResponseTimeMonitorImpl() {
		initedMillis = System.currentTimeMillis();
	}

	/**
	 * Return the number of hits this object has handled.
	 */
	public final int getAccessCount() {
		return accessCount;
	}

	/**
	 * Return the number of milliseconds since this object was loaded.
	 */
	public final long getUptime() {
		return System.currentTimeMillis() - this.initedMillis;
	}

	/**
	 * Return the date when this object was loaded.
	 */
	public final Date getLoadDate() {
		return new Date(this.initedMillis);
	}

	/**
	 * Return the average response time achieved by this object.
	 */
	public final int getAverageResponseTimeMillis() {
		// avoid division by 0
		if (getAccessCount() == 0) {
			return 0;
		}
		return this.totalResponseTimeMillis / getAccessCount();
	}

	/**
	 * Return the best (lowest) response time achieved by this object.
	 */
	public final int getBestResponseTimeMillis() {
		return bestResponseTimeMillis;
	}

	/**
	 * Return the worst (slowest) response time achieved by this object.
	 */
	public final int getWorstResponseTimeMillis() {
		return worstResponseTimeMillis;
	}

	/**
	 * Utility method to record this response time, updating
	 * the best and worst response times if necessary.
	 * @param responseTime the response time of this request
	 */
	public final void recordResponseTime(long responseTime) {
		++this.accessCount;
		int iResponseTime = (int) responseTime;
		this.totalResponseTimeMillis += iResponseTime;
		if (iResponseTime < this.bestResponseTimeMillis) {
			this.bestResponseTimeMillis = iResponseTime;
		}
		if (iResponseTime > this.worstResponseTimeMillis) {
			this.worstResponseTimeMillis = iResponseTime;
		}
	}

	/**
	 * Return a human-readable string showing the performance
	 * data recorded by this object.
	 */
	public String toString() {
		return "hits=" + getAccessCount() + 
			"; avg=" + getAverageResponseTimeMillis() + 
			"; best=" + getBestResponseTimeMillis() + 
			"; worst=" + getWorstResponseTimeMillis();
	}

}
