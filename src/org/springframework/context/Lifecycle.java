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

package org.springframework.context;

/**
 * Interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 *
 * <p>Can be implemented by both components (typically a Spring bean defined in
 * a Spring BeanFactory) and containers (typically a Spring ApplicationContext).
 * Containers will propagate start/stop signals to all components that apply.
 *
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the MBeanExporter will typically be defined with an
 * InterfaceBasedMBeanInfoAssembler, restricting the visibility of
 * activity-controlled components to the Lifecycle interface.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 * @see org.springframework.jmx.export.MBeanExporter
 */
public interface Lifecycle {

	/**
	 * Start the component.
	 * Should not throw an exception if the component is already running.
	 * <p>In the case of a container, this will propagate the start signal
	 * to all components that apply.
	 */
	void start();

	/**
	 * Stop the component.
	 * Should not throw an exception if the component isn't started yet.
	 * <p>In the case of a container, this will propagate the stop signal
	 * to all components that apply.
	 */
	void stop();

	/**
	 * Return whether the component is running.
	 * <p>In the case of a container, this will return <code>true</code>
	 * only if <i>all</i> components that apply are running.
	 */
	boolean isRunning();

}
