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

package org.springframework.scheduling.support;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;

/**
 * Adapter that implements the Runnable interface as a configurable
 * method invocation based on Spring's MethodInvoker.
 *
 * <p>Derives from ArgumentConvertingMethodInvoker, inheriting common
 * configuration properties from MethodInvoker.
 *
 * <p>Useful to generically encapsulate a method invocation as timer task for
 * <code>java.util.Timer</code>, in combination with a DelegatingTimerTask adapter.
 * Can also be used with JDK 1.5's <code>java.util.concurrent.Executor</code>
 * abstraction, which works with plain Runnables.
 *
 * <p>Extended by Spring's MethodInvokingTimerTaskFactoryBean adapter
 * for <code>java.util.TimerTask</code>. Note that you can populate a
 * ScheduledTimerTask object with a plain MethodInvokingRunnable instance
 * as well, which will automatically get wrapped with a DelegatingTimerTask.
 *
 * @author Juergen Hoeller
 * @since 1.2.4
 * @see org.springframework.util.MethodInvoker
 * @see org.springframework.beans.support.ArgumentConvertingMethodInvoker
 * @see org.springframework.scheduling.timer.DelegatingTimerTask
 * @see org.springframework.scheduling.timer.ScheduledTimerTask#setRunnable
 * @see org.springframework.scheduling.timer.MethodInvokingTimerTaskFactoryBean
 * @see java.util.Timer
 * @see java.util.concurrent.Executor#execute(Runnable)
 */
public class MethodInvokingRunnable extends ArgumentConvertingMethodInvoker
		implements Runnable, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());


	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchMethodException {
		prepare();
	}


	public void run() {
		try {
			invoke();
		}
		catch (InvocationTargetException ex) {
			logger.error(getInvocationFailureMessage(), ex);
			// Do not throw exception, else the main loop of the Timer will stop!
		}
		catch (Throwable ex) {
			logger.error(getInvocationFailureMessage(), ex);
			// Do not throw exception, else the main loop of the Timer will stop!
		}
	}

	protected String getInvocationFailureMessage() {
		return  "Invocation of method '" + getTargetMethod() +
				"' on target object [" + getTargetObject() + "] failed";
	}

}
