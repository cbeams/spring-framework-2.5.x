package org.springframework.scheduling.timer;

import java.util.TimerTask;

/**
 * JavaBean that describes a scheduled TimerTask, consisting of
 * the TimerTask itself and a delay plus period. Period needs to
 * be specified; there is no point in a default for it.
 *
 * <p>The J2SE Timer does not offer more sophisticated scheduling
 * options like cron expressions. Consider using Quartz for such
 * demanding needs.
 *
 * <p>Note that Timer uses a TimerTask instance that is shared
 * between repeated executions, in contrast to Quartz which
 * instantiates a new Job for each execution.
 *
 * @author Juergen Hoeller
 * @since 19.02.2004
 * @see java.util.TimerTask
 * @see java.util.Timer#schedule(TimerTask, long, long)
 * @see java.util.Timer#scheduleAtFixedRate(TimerTask, long, long)
 */
public class ScheduledTimerTask {

	private TimerTask timerTask;

	private long delay = 0;

	private long period = 0;

	private boolean fixedRate = false;

	/**
	 * Set the TimerTask to schedule.
	 */
	public void setTimerTask(TimerTask timerTask) {
		this.timerTask = timerTask;
	}

	/**
	 * Return the TimerTask to schedule.
	 */
	public TimerTask getTimerTask() {
		return timerTask;
	}

	/**
	 * Set the delay before starting the task for the first time,
	 * in milliseconds. Default is 0, immediately starting the
	 * task after successful scheduling.
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * Return the delay before starting the job for the first time.
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Set the period between repeated task executions,
	 * in milliseconds. Default is 0; this property needs to
	 * be set to a positive value for proper execution.
	 */
	public void setPeriod(long period) {
		this.period = period;
	}

	/**
	 * Return the period between repeated task executions.
	 */
	public long getPeriod() {
		return period;
	}

	/**
	 * Set whether to schedule as fixed-rate execution, rather than
	 * fixed-delay execution. Default is false, i.e. fixed delay.
	 * See Timer javadoc for details on those execution modes.
	 * @see java.util.Timer#schedule(TimerTask, long, long)
	 * @see java.util.Timer#scheduleAtFixedRate(TimerTask, long, long)
	 */
	public void setFixedRate(boolean fixedRate) {
		this.fixedRate = fixedRate;
	}

	/**
	 * Return whether to schedule as fixed-rate execution.
	 */
	public boolean isFixedRate() {
		return fixedRate;
	}

}
