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

package org.springframework.jca.cci.object.commarea;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jca.cci.core.OutputRecordCreator;
import org.springframework.jca.cci.object.MappingRecordQuery;

/**
 * Query specific for COMMAREA based record.
 * 
 * @param Thierry TEMPLIER
 */
public abstract class MappingCommAreaQuery extends MappingRecordQuery {

	/**
	 * 
	 * @param connectionFactory ConnectionFactory to use to obtain connections
	 * @param connectionSpec Specification to configure the connection
	 * @param interactionSpec Specification to configure the interaction
	 */
	public MappingCommAreaQuery(ConnectionFactory connectionFactory,ConnectionSpec connectionSpec,InteractionSpec interactionSpec) {
		super(connectionFactory,connectionSpec,interactionSpec,null);
	}

	/**
	 * 
	 * @param connectionFactory ConnectionFactory to use to obtain connections
	 * @param connectionSpec Specification to configure the connection
	 * @param interactionSpec Specification to configure the interaction
	 * @param outputCreator Creator of output records
	 */
	public MappingCommAreaQuery(ConnectionFactory connectionFactory,ConnectionSpec connectionSpec,InteractionSpec interactionSpec,OutputRecordCreator outputCreator) {
		super(connectionFactory,connectionSpec,interactionSpec,outputCreator);
	}

	/**
	 * @see org.springframework.jca.cci.object.MappingRecordQuery#mapRecord(javax.resource.cci.Record)
	 */
	protected Object mapRecord(Record rc) throws ResourceException,SQLException {
		CommAreaRecord commAreaRecord=(CommAreaRecord)rc;
		try {
			return mapBytes(commAreaRecord.getBytes());
		} catch(UnsupportedEncodingException ex) {
			throw new DataRetrievalFailureException("The encoding is not supported",ex);
		}
	}

	/**
	 * Method used to convert the COMMAREA's bytes to an object.
	 * @param bytes COMMAREA's bytes
	 * @return the ouput data
	 * @throws UnsupportedEncodingException
	 */
	protected abstract Object mapBytes(byte[] bytes) throws UnsupportedEncodingException;

	/**
	 * @see org.springframework.jca.cci.object.MappingRecordQuery#generateRecord(java.lang.Object)
	 */
	protected Record generateRecord(Object obj) {
		try {
			return new CommAreaRecord(generateBytes(obj));
		} catch(UnsupportedEncodingException ex) {
			throw new DataRetrievalFailureException("The encoding is not supported",ex);
		}
	}

	/**
	 * Method used to convert an object into bytes.
	 * @param obj input data
	 * @return COMMAREA's bytes 
	 * @throws UnsupportedEncodingException
	 */
	protected abstract byte[] generateBytes(Object obj) throws UnsupportedEncodingException;
}
