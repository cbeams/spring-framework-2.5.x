package org.springframework.scheduling.timer;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that sets up a J2SE Timer and exposes it for
 * bean references.
 *
 * <p>Allows registration of ScheduledTimerTasks, automatically
 * starting the timer on initialization and cancelling it on destruction.
 * In typical scenarios, there is no need to access the Timer instance
 * itself in application code.
 *
 * <p>Note that Timer uses a TimerTask instance that is shared
 * between repeated executions, in contrast to Quartz which
 * instantiates a new Job for each execution.
 *
 * @author Juergen Hoeller
 * @since 19.02.2004
 * @see ScheduledTimerTask
 * @see java.util.Timer
 * @see java.util.TimerTask
 */
public class TimerFactoryBean implements FactoryBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private final Timer timer = new Timer();

	/**
	 * Register a list of ScheduledTimerTask objects with the Timer that
	 * this FactoryBean creates.
	 * @see java.util.Timer#schedule(java.util.TimerTask, long, long)
	 * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
	 */
	public void setScheduledTimerTasks(List scheduledTimerTasks) {
		logger.info("Initializing Timer");
		for (Iterator it = scheduledTimerTasks.iterator(); it.hasNext();) {
			ScheduledTimerTask scheduledTask = (ScheduledTimerTask) it.next();
			if (scheduledTask.isFixedRate()) {
				this.timer.scheduleAtFixedRate(scheduledTask.getTimerTask(), scheduledTask.getDelay(),
																			 scheduledTask.getPeriod());
			}
			else {
				this.timer.schedule(scheduledTask.getTimerTask(), scheduledTask.getDelay(),
														scheduledTask.getPeriod());
			}
		}
	}

	public Object getObject() {
		return this.timer;
	}

	public Class getObjectType() {
		return Timer.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * This implementation cancels the Timer, stopping all scheduled tasks.
	 */
	public void destroy() {
		logger.info("Cancelling Timer");
		this.timer.cancel();
	}

}
