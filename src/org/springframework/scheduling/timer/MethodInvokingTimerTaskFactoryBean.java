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

package org.springframework.scheduling.timer;

import java.lang.reflect.InvocationTargetException;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
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
public class MethodInvokingTimerTaskFactoryBean extends ArgumentConvertingMethodInvoker
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

		protected final Log logger = LogFactory.getLog(getClass());

		private final MethodInvoker methodInvoker;

		private final String errorMessage;

		/**
		 * Create a new MethodInvokingTimerTask with the MethodInvoker to use.
		 */
		private MethodInvokingTimerTask(MethodInvoker methodInvoker) {
			this.methodInvoker = methodInvoker;
			this.errorMessage = "Invocation of method '" + this.methodInvoker.getTargetMethod() +
					"' on target object [" + this.methodInvoker.getTargetObject() + "] failed";

		}

		/**
		 * Invoke the method via the MethodInvoker.
		 */
		public void run() {
			try {
				this.methodInvoker.invoke();
			}
			catch (InvocationTargetException ex) {
				logger.error(this.errorMessage, ex);
				// Do not throw exception, else the main loop of the Timer will stop!
			}
			catch (Throwable ex) {
				logger.error(this.errorMessage, ex);
				// Do not throw exception, else the main loop of the Timer will stop!
			}
		}
	}

}
