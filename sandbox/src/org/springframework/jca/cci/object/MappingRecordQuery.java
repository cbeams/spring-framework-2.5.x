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

package org.springframework.jca.cci.object;

import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.ResultSet;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jca.cci.DataAccessInitFailureException;
import org.springframework.jca.cci.connection.ConnectionFactoryUtils;
import org.springframework.jca.cci.core.OutputRecordCreator;
import org.springframework.jca.cci.support.CciUtils;

/**
 * Reusable query in which concrete subclasses must implement the abstract
 * mapRecord(Record) method to convert each record into an object.
 *
 * @author Thierry TEMPLIER
 */
public abstract class MappingRecordQuery {
	private ConnectionFactory connectionFactory;
	private ConnectionSpec connectionSpec;
	private InteractionSpec interactionSpec;
	private OutputRecordCreator outputCreator;

	/**
	 * Constructor that allows use as a JavaBean.
	 */
	public MappingRecordQuery() {
	}

	/**
	 * Convenient constructor with ConnectionFactory and specifications
	 * ( connection and interactions ).
	 * @param connectionFactory ConnectionFactory to use to obtain connections
	 * @param connectionSpec Specification to configure the connection
	 * @param interactionSpec Specification to configure the interaction
	 */
	public MappingRecordQuery(ConnectionFactory connectionFactory, ConnectionSpec connectionSpec, InteractionSpec interactionSpec) {
		this(connectionFactory,connectionSpec,interactionSpec,null);
	}

	/**
	 * Convenient constructor with ConnectionFactory and specifications
	 * ( connection and interactions ).
	 * @param connectionFactory ConnectionFactory to use to obtain connections
	 * @param connectionSpec Specification to configure the connection
	 * @param interactionSpec Specification to configure the interaction
	 * @param outputCreator Object to create output record
	 */
	public MappingRecordQuery(ConnectionFactory connectionFactory, ConnectionSpec connectionSpec, InteractionSpec interactionSpec,OutputRecordCreator outputCreator) {
		this.connectionFactory=connectionFactory;
		this.connectionSpec=connectionSpec;
		this.interactionSpec=interactionSpec;
		this.outputCreator=outputCreator;
	}

	/**
	 * Method used to initialize properties on connection and interaction
	 * with ConnectionSpec and InteractionSpec.
	 * @param connectionSpec Specification to configure the connection
	 * @param interactionSpec Specification to configure the interaction
	 * @throws ResourceException
	 */
	protected abstract void initSpec(ConnectionSpec connectionSpec,InteractionSpec interactionSpec) throws ResourceException; 

	/**
	 * Create an indexed record from the RecordFactory.
	 * @param name record name
	 * @return the record
	 */
	public IndexedRecord createIndexedRecord(String name) {
		return CciUtils.createIndexedRecord(getConnectionFactory(),name);
	}

	/**
	 * Create an mapped record from the RecordFactory.
	 * @param name record name
	 * @return the record
	 */
	public MappedRecord createMappedRecord(String name) {
		return CciUtils.createMappedRecord(getConnectionFactory(),name);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call. The method don't use input as
	 * output record.
	 * @param inObject input data
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 */
	public Object run(Object inObject,boolean giveResponseInParameter) {
		return run(inObject,giveResponseInParameter,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param inObject input data
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output data extracted with the mapRecord method
	 */
	public Object run(Object inObject,boolean giveResponseInParameter,boolean useInputAsOutput) {
		return internalRun(generateRecord(inObject),null,giveResponseInParameter,useInputAsOutput);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param inObject input data
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @return the output data extracted with the mapRecord method
	 */
	public Object run(Object inObject,Record outRecord) {
		return internalRun(generateRecord(inObject),outRecord,true,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param inObject input data
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @return the output data extracted with the mapRecord method
	 */
	public Object run(Record inObject,Record outRecord) {
		return internalRun(generateRecord(inObject),outRecord,true,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param inRecord input record
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output data extracted with the mapRecord method
	 */
	private Object internalRun(Record inRecord,Record outRecord,boolean giveResponseInParameter,boolean useInputAsOutput) {
		Connection connection=null;
		Interaction interaction=null;

		/* If useInputAsOutput is true, set the output record with the input
		   record */
		if( useInputAsOutput ) {
			outRecord=inRecord;
		}

		/* Check the paramters in the cas of a call of
		   boolean execute(InteractionSpec,Record,Record) */
		if( giveResponseInParameter && outRecord==null && outputCreator==null ) {
			throw new DataAccessResourceFailureException("The instance of the output record must be given in the CCI execute method, but is null");
		}

		try {
			initSpec(connectionSpec,interactionSpec);
		} catch(ResourceException ex) {
			throw new DataAccessInitFailureException("Error during the initialization of the connection and interaction specs",ex);
		}

		try {
			if( connectionSpec==null ) {
				connection=ConnectionFactoryUtils.getConnection(getConnectionFactory());
			} else {
				connection=ConnectionFactoryUtils.getConnection(getConnectionFactory(),connectionSpec);
			}
			interaction=connection.createInteraction();
			if( giveResponseInParameter ) {
				/* Use the CCI execute method with output record as parameter */
				if( outRecord==null ) {
					outRecord=outputCreator.createOutputRecord();
				}
				interaction.execute(interactionSpec,inRecord,outRecord);
			} else {
				/* Use the CCI execute method which returns the output record */
				if( useInputAsOutput ) {
					interaction.execute(interactionSpec,inRecord);
				} else {
					outRecord=interaction.execute(interactionSpec,inRecord);
				}
			}
			return mapRecord(outRecord);
		} catch(ResourceException ex) {
			throw new DataAccessResourceFailureException("Error during the call of CCI",ex);
		} catch(SQLException ex) {
			throw new DataAccessResourceFailureException("Error during the call of CCI resultset",ex);
		} finally {
			if( outRecord instanceof ResultSet ) {
				CciUtils.closeResultSet((ResultSet)outRecord);
			}
			CciUtils.closeInteration(interaction);
			ConnectionFactoryUtils.closeConnectionIfNecessary(connection,getConnectionFactory());
		}
	}

	/**
	 * Subclasses must implement this method to convert the record returned
	 * by the cci's execute method into an object of the result type.
	 * @param rc Record returned by the cci's execute method
	 * @return an object of the result type
	 * @throws ResourceException if there's an error extracting data.
	 * Subclasses can simply not catch ResourceException, relying on the
	 * framework to clean up.
	 */
	protected abstract Object mapRecord(Record rc) throws ResourceException,SQLException;

	/**
	 * Subclasses must implement this method to generate a input record
	 * from an object.
	 * @param obj object based from
	 * @return the generated record
	 * @throws ResourceException
	 */
	protected abstract Record generateRecord(Object obj);

	/**
	 * @return
	 */
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * @return
	 */
	public ConnectionSpec getConnectionSpec() {
		return connectionSpec;
	}

	/**
	 * @return
	 */
	public InteractionSpec getInteractionSpec() {
		return interactionSpec;
	}

	/**
	 * @param factory
	 */
	public void setConnectionFactory(ConnectionFactory factory) {
		connectionFactory = factory;
	}

	/**
	 * @param spec
	 */
	public void setConnectionSpec(ConnectionSpec spec) {
		connectionSpec = spec;
	}

	/**
	 * @param spec
	 */
	public void setInteractionSpec(InteractionSpec spec) {
		interactionSpec = spec;
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
