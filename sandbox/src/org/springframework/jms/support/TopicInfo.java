/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms.support;

/**
 * Information about a JMS Topic.
 * @author Mark Pollack
 */
public class TopicInfo extends DestinationInfo {

	public TopicInfo(String name)
	{
		setName(name);
	}
}
