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

package org.springframework.jca.cci.core;

import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.RecordFactory;

import org.springframework.jca.cci.support.CciUtils;

/**
 * Default implementation of the RecordGeneratorFromFactory interface.

 *  * @author Thierry Templier
 */
public abstract class DefaultRecordGeneratorFromFactory implements RecordGeneratorFromFactory {
	private ConnectionFactory connectionFactory;

	public DefaultRecordGeneratorFromFactory() {
	}

	/**
	 * @see org.springframework.jca.cci.core.RecordGeneratorFromRecordFactory#createIndexedRecord(java.lang.String)
	 */
	public IndexedRecord createIndexedRecord(String name) {
		return CciUtils.createIndexedRecord(connectionFactory,name);
	}

	/**
	 * @see org.springframework.jca.cci.core.RecordGeneratorFromRecordFactory#createMappedRecord(java.lang.String)
	 */
	public MappedRecord createMappedRecord(String name) {
		return CciUtils.createMappedRecord(connectionFactory,name);
	}

	/**
	 * @see org.springframework.jca.cci.core.RecordGeneratorFromRecordFactory#setRecordFactory(javax.resource.cci.RecordFactory)
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory=connectionFactory;
	}

}
