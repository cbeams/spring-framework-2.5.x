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

package org.springframework.jmx.export.notification;

import org.springframework.util.Assert;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.modelmbean.ModelMBean;

/**
 * Implementation of the {@link NotificationPublisher} interface that uses the infrastructure provided the
 * {@link ModelMBean} interface to track {@link javax.management.NotificationListener javax.management.NotificationListeners}
 * and send {@link Notification Notifications} to those listeners.
 *
 * @author Rob Harrop
 * @see ModelMBean
 * @see NotificationPublisherAware
 */
public class ModelMBeanNotificationPublisher implements NotificationPublisher {

	/**
	 * The {@link ModelMBean} instance wrapping the managed resource into which this <code>NotificationPublisher</code>
	 * will be injected.
	 */
	private ModelMBean modelMBean;

	/**
	 * Creates a new <code>ModelMBeanNotificationPublisher</code> that will publish all {@link javax.management.Notification Notifications}
	 * to the supplied {@link ModelMBean}.
	 */
	public ModelMBeanNotificationPublisher(ModelMBean modelMBean) {
		this.modelMBean = modelMBean;
	}

	/**
	 * Sends the supplied {@link Notification} using the wrapped {@link ModelMBean} instance.
	 */
	public void sendNotification(Notification notification) {
		Assert.notNull(notification, "Notification cannot be null.");
		try {
			if (notification instanceof AttributeChangeNotification) {
				this.modelMBean.sendAttributeChangeNotification((AttributeChangeNotification) notification);
			}
			else {
				this.modelMBean.sendNotification(notification);
			}
		}
		catch (MBeanException e) {
			throw new UnableToSendNotificationException("Unable to send notification.", e);
		}
	}
}
