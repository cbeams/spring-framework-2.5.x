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
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResultSet;

import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jca.cci.DataAccessInitFailureException;
import org.springframework.jca.cci.connection.ConnectionFactoryUtils;
import org.springframework.jca.cci.support.CciAccessor;
import org.springframework.jca.cci.support.CciUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * <b>This is the central class in the CCI core package.</b>
 * It simplifies the use of JCA via CCI and helps to avoid common errors.
 * It executes core CCI workflow, leaving application code to provide parameters
 * to CCI and extract results. This class executes EIS queries or updates and
 * catching Resource exceptions and translating them to the generic, more
 * informative exception hierarchy defined in the org.springframework.dao package.
 *
 * <p>Code using this class need only implement callback interfaces, giving
 * them a clearly defined contract.</p>
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a ConnectionFactory reference, or get prepared in an application context
 * and given to services as bean reference. Note: The ConnectioNFactory should
 * always be configured as a bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.</p>
 *
 * @author Thierry TEMPLIER
 */
public class CciTemplate extends CciAccessor {

	/**
	 * Construct a new CciTemplate for bean usage.
	 * Note: The ConnectionFactory has to be set before using the instance.
	 * This constructor can be used to prepare a CciTemplate via a BeanFactory,
	 * typically setting the ConnectionFactory via setConnectionFactory.
	 * @see #setConnectionFactory
	 */
	public CciTemplate() {
	}

	/**
	 * Construct a new CciTemplate, given a ConnectionFactory to obtain connections from.
	 * Note: This will trigger eager initialization of the exception translator.
	 * @param connectionFactory JCA ConnectionFactory to obtain connections from
	 */
	public CciTemplate(ConnectionFactory connectionFactory) {
		setConnectionFactory(connectionFactory);
		afterPropertiesSet();
	}

	/**
	 * Construct a new CciTemplate, given a ConnectionFactory to obtain connections from.
	 * Note: This will trigger eager initialization of the exception translator.
	 * @param connectionFactory JCA ConnectionFactory to obtain connections from
	 */
	public CciTemplate(ConnectionFactory connectionFactory,OutputRecordCreator outputCreator) {
		setConnectionFactory(connectionFactory);
		setOutputCreator(outputCreator);
		afterPropertiesSet();
	}

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
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @return the output record
	 */
	public Object execute(CciSpecsHolder specs,Record inRecord,Record outRecord) {
		return internalExecute(specs,inRecord,outRecord,new RecordExtractor() {
			public Object extractData(Record record) throws DataAccessException {
				return record;
			}
		},true,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @param extractor object to convert the output record to an object
	 * @return the output data extracted with the RecordExtractor object
	 */
	public Object execute(CciSpecsHolder specs,Record inRecord,
							Record outRecord,RecordExtractor extractor) {
		return internalExecute(specs,inRecord,outRecord,extractor,true,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call. The method don't use input as
	 * output record.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @return the output record
	 */
	public Object execute(CciSpecsHolder specs,Record inRecord,
							boolean giveResponseInParameter) {
		return execute(specs,inRecord,giveResponseInParameter,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output record
	 */
	public Object execute(CciSpecsHolder specs,Record inRecord,
							boolean giveResponseInParameter,boolean useInputAsOutput) {
		return execute(specs,inRecord,new RecordExtractor() {
			public Object extractData(Record record) throws DataAccessException {
				return record;
			}
		},giveResponseInParameter,useInputAsOutput);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call. The method don't use input as
	 * output record.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param extractor object to convert the output record to an object
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @return the output data extracted with the RecordExtractor object
	 */
	public Object execute(CciSpecsHolder specs,Record inRecord,RecordExtractor extractor,
							boolean giveResponseInParameter) {
		return execute(specs,inRecord, extractor,giveResponseInParameter,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param extractor object to convert the output record to an object
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output data extracted with the RecordExtractor object
	 */
	public Object execute(CciSpecsHolder specs,Record inRecord,RecordExtractor extractor,
							boolean giveResponseInParameter,boolean useInputAsOutput) {
		return internalExecute(specs,inRecord,null,extractor,giveResponseInParameter,useInputAsOutput);
	}

	/**
	 * Generate the input record from a record generator and
	 * manage ResourceException thrown during the call of the
	 * generateRecord method.
	 * @param recordGenerator
	 * @param inObject
	 * @return the input record generated
	 */
	private Record generateInputRecord(RecordGenerator recordGenerator,Object inObject) {
		//Manage the RecordGeneratorFromFactory
		if( recordGenerator instanceof RecordGeneratorFromFactory ) {
			((RecordGeneratorFromFactory)recordGenerator).setConnectionFactory(
			                                                getConnectionFactory());
		}

		//Generate the input record
		try {
			return recordGenerator.generateRecord(inObject);
		} catch(ResourceException ex) {
			throw new DataAccessResourceFailureException("Unable to generate the input record",ex);
		}
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inObject input data
	 * @param recordGenerator object to convert the input data to a record
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @return the output record
	 */
	public Object execute(CciSpecsHolder specs,Object inObject,
							RecordGenerator recordGenerator,Record outRecord) {
		if( inObject instanceof Record ) {
			return internalExecute(specs,(Record)inObject,outRecord,new RecordExtractor() {
				public Object extractData(Record record) throws DataAccessException {
					return record;
				}
			},true,false);
		} else {
			return internalExecute(specs,generateInputRecord(recordGenerator,inObject),outRecord,new RecordExtractor() {
				public Object extractData(Record record) throws DataAccessException {
					return record;
				}
			},true,false);
		}
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inObject input data
	 * @param recordGenerator object to convert the input data to a record
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @param extractor object to convert the output record to an object
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @return the output data extracted with the RecordExtractor object
	 */
	public Object execute(CciSpecsHolder specs,Object inObject,
							RecordGenerator recordGenerator,
							Record outRecord,RecordExtractor extractor) {
		if( inObject instanceof Record ) {
			return internalExecute(specs,(Record)inObject,outRecord,extractor,true,false);
		} else {
			return internalExecute(specs,generateInputRecord(recordGenerator,inObject),outRecord,extractor,true,false);
		}
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call. The method don't use input as
	 * output record.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inObject input data
	 * @param recordGenerator object to convert the input data to a record
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @return the output record
	 */
	public Object execute(CciSpecsHolder specs,Object inObject,RecordGenerator recordGenerator,
							boolean giveResponseInParameter) {
		return execute(specs,inObject,recordGenerator,giveResponseInParameter,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inObject input data
	 * @param recordGenerator object to convert the input data to a record
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output record
	 */
	public Object execute(CciSpecsHolder specs,Object inObject,RecordGenerator recordGenerator,
							boolean giveResponseInParameter,boolean useInputAsOutput) {
		if( inObject instanceof Record ) {
			return execute(specs,(Record)inObject,new RecordExtractor() {
				public Object extractData(Record record) throws DataAccessException {
					return record;
				}
			},giveResponseInParameter,useInputAsOutput);
		} else {
			return execute(specs,generateInputRecord(recordGenerator,inObject),new RecordExtractor() {
				public Object extractData(Record record) throws DataAccessException {
					return record;
				}
			},giveResponseInParameter,useInputAsOutput);
		}
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call. The method don't use input as
	 * output record.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inObject input data
	 * @param recordGenerator object to convert the input data to a record
	 * @param extractor object to convert the output record to an object
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output data extracted with the RecordExtractor object
	 */
	public Object execute(CciSpecsHolder specs,Object inObject,RecordGenerator recordGenerator,
							RecordExtractor extractor,boolean giveResponseInParameter) {
		return execute(specs,inObject,recordGenerator,extractor,giveResponseInParameter,false);
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inObject input data
	 * @param recordGenerator object to convert the input data to a record
	 * @param extractor object to convert the output record to an object
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output data extracted with the RecordExtractor object
	 */
	public Object execute(CciSpecsHolder specs,Object inObject,RecordGenerator recordGenerator,
							RecordExtractor extractor,boolean giveResponseInParameter,boolean useInputAsOutput) {
		if( inObject instanceof Record ) {
			return internalExecute(specs,(Record)inObject,null,extractor,giveResponseInParameter,useInputAsOutput);
		} else {
			return internalExecute(specs,generateInputRecord(recordGenerator,inObject),null,extractor,giveResponseInParameter,useInputAsOutput);
		}
	}

	/**
	 * Execute a request on an EIS with CCI. The flag determines
	 * the method execute to call.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param inRecord input record
	 * @param outRecord output record ( could be null if execute doesn't need it )
	 * @param extractor object to convert the output record to an object
	 * @param giveResponseInParameter flag to determine the execute method to call 
	 * @param useInputAsOutput flag to determine to use input record as output record
	 * @return the output data extracted with the RecordExtractor object
	 */
	private Object internalExecute(CciSpecsHolder specs,Record inRecord,Record outRecord,RecordExtractor extractor,
									boolean giveResponseInParameter,boolean useInputAsOutput) {
		if( specs==null ) {
			throw new DataAccessInitFailureException("The instance of CciSpecsHolder is null");
		}

		/* If useInputAsOutput is true, set the output record with the input
		   record */
		if( useInputAsOutput ) {
			outRecord=inRecord;
		}

		/* Check the paramters in the cas of a call of
		   boolean execute(InteractionSpec,Record,Record) */
		OutputRecordCreator outputCreator=getOutputCreator();
		if( giveResponseInParameter && outRecord==null && outputCreator==null ) {
			throw new DataAccessResourceFailureException("The instance of the output record must be given in the CCI execute method, but is null");
		}

		ConnectionSpec connectionSpec=specs.getConnectionSpec();
		InteractionSpec interactionSpec=specs.getInteractionSpec();
		try {
			specs.initSpecs(connectionSpec,interactionSpec);
		} catch(ResourceException ex) {
			throw new DataAccessInitFailureException("Error during the initialization of the connection and interaction specs",ex);
		}

		Connection connection=null;
		Interaction interaction=null;
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
			return extractor.extractData(outRecord);
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
	 * Execute a request on an EIS with CCI using the connection
	 * directly.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param action action using the CCI connection
	 * @return the output data
	 */
	public Object execute(CciSpecsHolder specs,ConnectionCallback action) {
		if( specs==null ) {
			throw new DataAccessInitFailureException("The instance of CciSpecsHolder is null");
		}

		ConnectionSpec connectionSpec=specs.getConnectionSpec();
		InteractionSpec interactionSpec=specs.getInteractionSpec();
		try {
			specs.initSpecs(connectionSpec,interactionSpec);
		} catch(ResourceException ex) {
			throw new DataAccessInitFailureException("Error during the initialization of the connection and interaction specs",ex);
		}

		Connection connection=null;
		try {
			if( connectionSpec==null ) {
				connection=ConnectionFactoryUtils.getConnection(getConnectionFactory());
			} else {
				connection=ConnectionFactoryUtils.getConnection(getConnectionFactory(),connectionSpec);
			}
			return action.doInConnection(specs,connection);
		} catch(ResourceException ex) {
			throw new DataAccessResourceFailureException("Error during the call of CCI",ex);
		} catch(SQLException ex) {
			throw new DataAccessResourceFailureException("Error during the call of CCI resultset",ex);
		} finally {
			ConnectionFactoryUtils.closeConnectionIfNecessary(connection,getConnectionFactory());
		}
	}

	/**
	 * Execute a request on an EIS with CCI using the interaction
	 * directly.
	 * @param specs container for ConnectionSpec and InteractionSpec
	 * @param action action using the CCI interaction
	 * @return the output data
	 */
	public Object execute(CciSpecsHolder specs,InteractionCallback action) {
		if( specs==null ) {
			throw new DataAccessInitFailureException("The instance of CciSpecsHolder is null");
		}

		ConnectionSpec connectionSpec=specs.getConnectionSpec();
		InteractionSpec interactionSpec=specs.getInteractionSpec();
		try {
			specs.initSpecs(connectionSpec,interactionSpec);
		} catch(ResourceException ex) {
			throw new DataAccessInitFailureException("Error during the initialization of the connection and interaction specs",ex);
		}

		Connection connection=null;
		Interaction interaction=null;
		try {
			if( connectionSpec==null ) {
				connection=ConnectionFactoryUtils.getConnection(getConnectionFactory());
			} else {
				connection=ConnectionFactoryUtils.getConnection(getConnectionFactory(),connectionSpec);
			}
			interaction=connection.createInteraction();
			return action.doInInteraction(specs,interaction);
		} catch(ResourceException ex) {
			throw new DataAccessResourceFailureException("Error during the call of CCI",ex);
		} catch(SQLException ex) {
			throw new DataAccessResourceFailureException("Error during the call of CCI resultset",ex);
		} finally {
			CciUtils.closeInteration(interaction);
			ConnectionFactoryUtils.closeConnectionIfNecessary(connection,getConnectionFactory());
		}
	}
}
