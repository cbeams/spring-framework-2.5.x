package org.springframework.scheduling.timer;

import org.springframework.core.NestedRuntimeException;

/**
 * RuntimeException to be thrown when a TimerTask implementation
 * encounters a (possibly checked) exception that it wants to rethrow.
 *
 * <p>This exception is analogous to Quartz' JobExecutionException.
 * Unfortunately, the Timer API does not specify such an exception itself.
 *
 * @author Juergen Hoeller
 * @since 19.03.2004
 * @see org.quartz.JobExecutionException
 */
public class TimerTaskExecutionException extends NestedRuntimeException {

	/**
	 * Create a new TimerTaskExecutionException.
	 * @param msg the error message
	 * @param ex the exception that occured within the TimerTask
	 */
	public TimerTaskExecutionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
