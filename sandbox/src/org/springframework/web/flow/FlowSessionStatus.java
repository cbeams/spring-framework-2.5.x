/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import org.springframework.enums.ShortCodedEnum;

/**
 * @author Keith Donald
 */
public class FlowSessionStatus extends ShortCodedEnum {
	public static FlowSessionStatus ACTIVE = new FlowSessionStatus(0, "Active");

	public static FlowSessionStatus SUSPENDED = new FlowSessionStatus(1, "Suspended");

	public static FlowSessionStatus ENDED = new FlowSessionStatus(2, "Ended");

	private FlowSessionStatus(int code, String label) {
		super(code, label);
	}
}