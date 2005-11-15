package org.springframework.jmx.export.metadata;

import org.springframework.util.StringUtils;

/**
 * @author Rob Harrop
 */
public class ManagedNotification {

	private String[] notificationTypes;

	private String name;

	private String description;

	public String[] getNotificationTypes() {
		return notificationTypes;
	}

	public void setNotificationTypes(String notificationTypes) {
		setNotificationTypes(StringUtils.commaDelimitedListToStringArray(notificationTypes));
	}

	public void setNotificationTypes(String[] notificationTypes) {
		this.notificationTypes = notificationTypes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
