/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms.support;

/**
 * Information about a JMS Queue.
 * @author Mark Pollack
 */
public class QueueInfo extends DestinationInfo {

	public QueueInfo(String name)
	{
		setName(name);
	}
}
