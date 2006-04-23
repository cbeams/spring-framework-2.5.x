/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jmx.export;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Helper class that aggregates a {@link javax.management.NotificationListener},
 * a {@link javax.management.NotificationFilter}, and an arbitrary handback
 * object.
 *
 * <p>Also provides support for associating the encapsulated
 * {@link javax.management.NotificationListener} with any number of
 * MBeans from which it wishes to receive
 * {@link javax.management.Notification Notifications} via the
 * {@link #setMappedObjectNames mappedObjectNames} property.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class NotificationListenerBean implements InitializingBean {

	private NotificationListener notificationListener;

	private NotificationFilter notificationFilter;

	private Object handback;

	private String[] mappedObjectNames;


	/**
	 * Create a new instance of the {@link NotificationListenerBean} class.
	 */
	public NotificationListenerBean() {
	}

	/**
	 * Create a new instance of the {@link NotificationListenerBean} class.
	 * @param notificationListener the encapsulated listener
	 */
	public NotificationListenerBean(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}


	/**
	 * Set the {@link javax.management.NotificationListener}.
	 * @param notificationListener said {@link javax.management.NotificationListener}
	 */
	public void setNotificationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	/**
	 * Get the {@link javax.management.NotificationListener}.
	 * @return said {@link javax.management.NotificationListener}
	 */
	public NotificationListener getNotificationListener() {
		return notificationListener;
	}

	/**
	 * Set the {@link javax.management.NotificationFilter} associated
	 * with the encapsulated {@link #getNotificationFilter() NotificationFilter}.
	 * <p>May be <code>null</code>.
	 * @param notificationFilter said {@link javax.management.NotificationFilter}
	 */
	public void setNotificationFilter(NotificationFilter notificationFilter) {
		this.notificationFilter = notificationFilter;
	}

	/**
	 * Return the {@link javax.management.NotificationFilter} associated
	 * with the encapsulated {@link #getNotificationFilter() NotificationFilter}.
	 * <p>May be <code>null</code>.
	 * @return said {@link javax.management.NotificationFilter}
	 */
	public NotificationFilter getNotificationFilter() {
		return notificationFilter;
	}

	/**
	 * Set the (arbitrary) object that will be 'handed back' as-is by an
	 * {@link javax.management.NotificationBroadcaster} when notifying
	 * any {@link javax.management.NotificationListener}.
	 * <p>May be <code>null</code>.
	 * @param handback the handback object.
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, Object)
	 */
	public void setHandback(Object handback) {
		this.handback = handback;
	}

	/**
	 * Return the (arbitrary) object that will be 'handed back' as-is by an
	 * {@link javax.management.NotificationBroadcaster} when notifying
	 * any {@link javax.management.NotificationListener}.
	 * @return the handback object
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, Object)
	 */
	public Object getHandback() {
		return handback;
	}

	/**
	 * Set the {@link javax.management.ObjectName} of the single MBean
	 * that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * will be registered with to listen for 
	 * {@link javax.management.Notification Notifications}.
	 * @param mappedObjectName the {@link javax.management.ObjectName} identifying the
	 * target MBean that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * is to be registered with
	 */
	public void setMappedObjectName(String mappedObjectName) {
		setMappedObjectNames(mappedObjectName != null ? new String[] {mappedObjectName} : null);
	}

	/**
	 * Set the array of {@link javax.management.ObjectName ObjectNames} of the MBeans
	 * that the encapsulated {@link #getNotificationFilter() NotificationFilter}
	 * will be registered with to listen for
	 * {@link javax.management.Notification Notifications}.
	 * @param mappedObjectNames the array of {@link javax.management.ObjectName ObjectName}
	 * String representations, identifying the target MBeans that the encapsulated
	 * {@link #getNotificationFilter() NotificationFilter} is to be registered with
	 */
	public void setMappedObjectNames(String[] mappedObjectNames) {
		this.mappedObjectNames = mappedObjectNames;
	}

	/**
	 * Return the list of {@link javax.management.ObjectName ObjectNames} String
	 * representations for which the encapsulated
	 * {@link #getNotificationFilter() NotificationFilter} will
	 * be registered as a listener for {@link javax.management.Notification Notifications}.
	 */
	public String[] getMappedObjectNames() {
		return mappedObjectNames;
	}


	/**
	 * Check that this {@link NotificationListenerBean} has been
	 * correctly configured.
	 */
	public void afterPropertiesSet() {
		Assert.notNull(this.notificationListener, "Property 'notificationListener' is required");
	}

}
