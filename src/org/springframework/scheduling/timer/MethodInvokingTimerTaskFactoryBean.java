package org.springframework.scheduling.timer;

import java.util.TimerTask;

import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.MethodInvoker;

/**
 * FactoryBean that exposes a TimerTask object that delegates
 * job execution to a specified (static or non-static) method.
 * Avoids the need to implement a one-line TimerTask that just
 * invokes an existing business method.
 *
 * <p>Derived from MethodInvoker to share common properties and
 * behavior with MethodInvokingFactoryBean.
 *
 * @author Juergen Hoeller
 * @since 19.02.2004
 * @see org.springframework.beans.factory.config.MethodInvokingFactoryBean
 */
public class MethodInvokingTimerTaskFactoryBean extends MethodInvoker
		implements FactoryBean, InitializingBean {

	private TimerTask timerTask;

	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchMethodException {
		prepare();
		this.timerTask = new MethodInvokingTimerTask(this);
	}

	public Object getObject() {
		return this.timerTask;
	}

	public Class getObjectType() {
		return TimerTask.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * TimerTask implementation that invokes a specified method.
	 * Automatically applied by MethodInvokingTimerTaskFactoryBean.
	 */
	private static class MethodInvokingTimerTask extends TimerTask {

		private final MethodInvoker methodInvoker;

		/**
		 * Create a new MethodInvokingTimerTask with the MethodInvoker to use.
		 */
		public MethodInvokingTimerTask(MethodInvoker methodInvoker) {
			this.methodInvoker = methodInvoker;
		}

		/**
		 * Invoke the method via the MethodInvoker.
		 */
		public void run() {
			try {
				this.methodInvoker.invoke();
			}
			catch (Exception ex) {
				throw new MethodInvocationException(ex, this.methodInvoker.getTargetMethod());
			}
		}
	}

}
