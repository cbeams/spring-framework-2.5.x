/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.util;

/**
 * Interface implemented by objects that can provide performance information
 * as well as a record of the number of times they are accessed.
 *
 * <p>Implementing objects must ensure that implementing this interface
 * does <b>not</b> compromise thread safety. However, it may be acceptable
 * for slight innaccuracies in reported statistics to result from the
 * avoidance of synchronization: performance may be well be more important
 * than exact reporting, so long as the errors are not likely to be misleading.
 * 
 * @author Rod Johnson
 * @since November 21, 2000
 * @version $Id: ResponseTimeMonitor.java,v 1.2 2004-01-04 23:43:42 jhoeller Exp $
 */
public interface ResponseTimeMonitor {

	/**
	 * Return the number of accesses to this resource.
	 */
	int getAccessCount();

	/**
	 * Return the average response time in milliseconds.
	 */
	int getAverageResponseTimeMillis();

	/**
	 * Return the best (quickest) response time in milliseconds.
	 */
	int getBestResponseTimeMillis();

	/**
	 * Return the worst (slowest) response time in milliseconds.
	 */
	int getWorstResponseTimeMillis();

}
