package org.springframework.util;

import java.util.Date;

/**
 * Base class for objects that can expire after a certain number of seconds
 * or milliseconds. The main usage is to determine remaining values for
 * transactional timeouts.
 * @author Juergen Hoeller
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin
 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
 */
public class ExpiringObject {

	private Date deadline;

	/**
	 * Set the timeout for this object in seconds.
	 * @param seconds number of seconds until expiration
	 */
	public void setTimeoutInSeconds(int seconds) {
		setTimeoutInMillis(seconds * 1000);
	}

	/**
	 * Set the timeout for this object in milliseconds.
	 * @param millis number of milliseconds until expiration
	 */
	public void setTimeoutInMillis(long millis) {
		this.deadline = new Date(System.currentTimeMillis() + millis);
	}

	/**
	 * Return the expiration deadline of this object.
	 * @return the deadline as Date object
	 */
	public Date getDeadline() {
		return deadline;
	}

	/**
	 * Return the time to live for this object in seconds.
	 * Rounds up eagerly, e.g. 9.00001 still to 10.
	 * @return number of seconds until expiration
	 */
	public int getTimeToLiveInSeconds() {
		double diff = ((double) getTimeToLiveInMillis()) / 1000;
		return (int) Math.ceil(diff);
	}

	/**
	 * Return the time to live for this object in milliseconds.
	 * @return number of millseconds until expiration
	 */
	public long getTimeToLiveInMillis() {
		return deadline.getTime() - System.currentTimeMillis();
	}

}
