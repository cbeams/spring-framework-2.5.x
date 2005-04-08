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

package org.springframework.jca.cci.support;

import javax.resource.cci.ConnectionFactory;

import org.springframework.jca.cci.core.OutputRecordCreator;

/**
 * Base class for CciTemplate
 * defining common properties like ConnectionFactory and OutputRecordCreator
 *
 * <p>Not intended to be used directly. See CciTemplate
 *
 * @author Thierry TEMPLIER
 * @see org.springframework.cci.core.CciTemplate
 */
public abstract class CciAccessor {

	/**
	 * Used to obtain connections throughout the lifecycle of this object.
	 * This enables this class to close connections if necessary.
	 **/
	private ConnectionFactory connectionFactory;

	private OutputRecordCreator outputCreator;

	/**
	 * Set the JCA ConnectionFactory to obtain connections from.
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Return the ConnectionFactory used by this template.
	 */
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Eagerly initialize the exception translator,
	 * creating a default one for the specified ConnectionFactory if none set.
	 */
	public void afterPropertiesSet() {
		if (getConnectionFactory() == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
	}
	
	/**
	 * @return
	 */
	public OutputRecordCreator getOutputCreator() {
		return outputCreator;
	}

	/**
	 * @param creator
	 */
	public void setOutputCreator(OutputRecordCreator creator) {
		outputCreator = creator;
	}

}