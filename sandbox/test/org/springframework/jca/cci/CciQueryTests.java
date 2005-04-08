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

package org.springframework.jca.cci;

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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jca.cci.core.OutputRecordCreator;
import org.springframework.jca.cci.object.MappingRecordQuery;

/**
 * @author Thierry TEMPLIER
 */
public class CciQueryTests extends TestCase {

	private class MappingRecordQueryImpl extends MappingRecordQuery {
		protected QueryCallDetector callDetector;

		public MappingRecordQueryImpl() {
			super();
		}

		public MappingRecordQueryImpl(ConnectionFactory connectionFactory, ConnectionSpec connectionSpec,
																	InteractionSpec interactionSpec) {
			super(connectionFactory,connectionSpec,interactionSpec);
		}

		public MappingRecordQueryImpl(ConnectionFactory connectionFactory, ConnectionSpec connectionSpec,
									InteractionSpec interactionSpec,OutputRecordCreator outputCreator) {
			super(connectionFactory,connectionSpec,interactionSpec,outputCreator);
		}

		public void setCallDetector(QueryCallDetector callDetector) {
			this.callDetector=callDetector;
		}

		protected void initSpec(ConnectionSpec connectionSpec,InteractionSpec interactionSpec)
																		throws ResourceException {
			if( callDetector!=null ) {
				callDetector.callInitSpec();		 
			}
		}

		protected Record generateRecord(Object obj) {
			System.out.println("generateRecord ("+callDetector+" - "+obj+")");
			if( callDetector!=null ) {
				callDetector.callGenerateRecord();		 
			}
			return (Record)obj;
		}

		protected Object mapRecord(Record rc) throws ResourceException {
			System.out.println("mapRecord ("+callDetector+" - "+rc+")");
			if( callDetector!=null ) {
				callDetector.callMapRecord();		 
			}
			return rc;
		}
	}

	private class MappingRecordQueryGenerateImpl extends MappingRecordQueryImpl {
		private Record record;

		public MappingRecordQueryGenerateImpl() {
			super();
		}

		public MappingRecordQueryGenerateImpl(ConnectionFactory connectionFactory, ConnectionSpec connectionSpec,
																	InteractionSpec interactionSpec) {
			super(connectionFactory,connectionSpec,interactionSpec);
		}

		public MappingRecordQueryGenerateImpl(ConnectionFactory connectionFactory, ConnectionSpec connectionSpec,
									InteractionSpec interactionSpec,OutputRecordCreator outputCreator) {
			super(connectionFactory,connectionSpec,interactionSpec,outputCreator);
		}

		public void setGeneratedRecord(Record record) {
			this.record=record;
		}

		protected Record generateRecord(Object obj) {
			if( callDetector!=null ) {
				callDetector.callGenerateRecord();		 
			}
			return record;
		}
	}

	/*public CciQueryTests(String method) {
		super(method);
	}

	public static TestSuite suite() {
		TestSuite suite=new TestSuite();
		suite.addTest(new CciQueryTests("testTemplateExecuteInputOuptut"));
		return suite;
	}*/

	/**
	 * Test that the "IndexedRecord createIndexedRecord(String)" method
	 * is called when "createIndexedRecord(String);"is executed.
	 * @throws ResourceException
	 */
	public void testCreateIndexedRecord() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl recordFactoryControl = MockControl.createControl(RecordFactory.class);
		final RecordFactory recordFactory = (RecordFactory) recordFactoryControl.getMock();
		MockControl indexedRecordControl = MockControl.createControl(IndexedRecord.class);
		final IndexedRecord indexedRecord = (IndexedRecord) indexedRecordControl.getMock();

		connectionFactory.getRecordFactory();
		connectionFactoryControl.setReturnValue(recordFactory,1);

		recordFactory.createIndexedRecord("name");
		recordFactoryControl.setReturnValue(indexedRecord,1);

		connectionFactoryControl.replay();
		recordFactoryControl.replay();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,null);
		query.createIndexedRecord("name");

		connectionFactoryControl.verify();
		recordFactoryControl.verify();
	}

	/**
	 * Test that the "MappedRecord createMappedRecord(String)" method
	 * is called when "createMappedRecord(String);"is executed.
	 * @throws ResourceException
	 */
	public void testCreateMappedRecord() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl recordFactoryControl = MockControl.createControl(RecordFactory.class);
		final RecordFactory recordFactory = (RecordFactory) recordFactoryControl.getMock();
		MockControl mappedRecordControl = MockControl.createControl(MappedRecord.class);
		final MappedRecord mappedRecord = (MappedRecord) mappedRecordControl.getMock();

		connectionFactory.getRecordFactory();
		connectionFactoryControl.setReturnValue(recordFactory,1);

		recordFactory.createMappedRecord("name");
		recordFactoryControl.setReturnValue(mappedRecord,1);

		connectionFactoryControl.replay();
		recordFactoryControl.replay();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,null);
		query.createMappedRecord("name");

		connectionFactoryControl.verify();
		recordFactoryControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record)" method
	 * is called when "query.run(record,record,false);" is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputOuptut() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl inputRecordControl = MockControl.createControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec);
		((MappingRecordQueryImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection,1);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction,1);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		callDetectorControl.replay();

		query.run(inputRecord,outputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		callDetectorControl.verify();
	}

	/**
	 * Test that an DataAccessResourceFailureException is thrown 
	 * when "query.run(record,true);" is executed and an
	 * output record creator is not specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueWithoutCreator() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl inputRecordControl = MockControl.createControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec);
		try {
			query.run(inputRecord,true);
			assertTrue(false);
		} catch(DataAccessResourceFailureException ex) {
			assertTrue(true);
		}
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record,Record)" method
	 * is called when "query.run(record,true);" is executed  when a
	 * output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueWithCreator() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputRecordControl = MockControl.createControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();
		MockControl creatorControl = MockControl.createControl(OutputRecordCreator.class);
		OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec,creator);
		((MappingRecordQueryImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		creator.createOutputRecord();
		creatorControl.setReturnValue(outputRecord);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		creatorControl.replay();
		callDetectorControl.replay();

		query.run(inputRecord,true);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		creatorControl.verify();
		callDetectorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record)" method
	 * is called when "query.run(record,false);" is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputFalse() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputRecordControl = MockControl.createControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec);
		((MappingRecordQueryImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputRecord);
		interactionControl.setReturnValue(outputRecord,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		callDetectorControl.replay();

		query.run(inputRecord,false);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		callDetectorControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record,Record)" method
	 * is called when "query.run(object,record);" is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputObjectOuptut() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputRecordControl = MockControl.createControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryGenerateImpl(connectionFactory,null,interactionSpec);
		((MappingRecordQueryGenerateImpl)query).setGeneratedRecord(inputRecord);
		((MappingRecordQueryGenerateImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection,1);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction,1);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		callDetectorControl.replay();

		query.run(obj,outputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		callDetectorControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record,Record)" method
	 * is called when "query.run(record,true,true);" is executed with the same
	 * instance of input and output records.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueUseInputAsOutput() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputOutputRecordControl = MockControl.createControl(Record.class);
		final Record inputOutputRecord = (Record)inputOutputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec);
		((MappingRecordQueryImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputOutputRecord,inputOutputRecord);
		interactionControl.setReturnValue(true,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		callDetectorControl.replay();

		Record tmpOutputRecord=(Record)query.run(inputOutputRecord,true,true);
		assertSame(tmpOutputRecord,inputOutputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		callDetectorControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record)" method
	 * is called when "query.run(record,false,true);" is executed and
	 * the result is put by the connector implementation in the input
	 * record.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputFalseUseInputAsOutput() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputOutputRecordControl = MockControl.createControl(Record.class);
		final Record inputOutputRecord = (Record)inputOutputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec);
		((MappingRecordQueryImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputOutputRecord);
		interactionControl.setReturnValue(null,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		callDetectorControl.replay();

		Record tmpOutputRecord=(Record)query.run(inputOutputRecord,false,true);
		assertSame(tmpOutputRecord,inputOutputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		callDetectorControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record,Record)" method
	 * is called when "query.run(record,true);" is executed  when a
	 * output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueTrueWithCreator() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputOutputRecordControl = MockControl.createControl(Record.class);
		final Record inputOutputRecord = (Record)inputOutputRecordControl.getMock();
		MockControl creatorControl = MockControl.createControl(OutputRecordCreator.class);
		OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl interactionSpecControl = MockControl.createControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl callDetectorControl = MockControl.createControl(QueryCallDetector.class);
		QueryCallDetector callDetector = (QueryCallDetector)callDetectorControl.getMock();

		MappingRecordQuery query=new MappingRecordQueryImpl(connectionFactory,null,interactionSpec,creator);
		((MappingRecordQueryImpl)query).setCallDetector(callDetector);
		Object obj=new Object();

		callDetector.callGenerateRecord();
		callDetectorControl.setVoidCallable(1);

		callDetector.callInitSpec();
		callDetectorControl.setVoidCallable(1);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputOutputRecord,inputOutputRecord);
		interactionControl.setReturnValue(true,1);

		callDetector.callMapRecord();
		callDetectorControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		creatorControl.replay();
		callDetectorControl.replay();

		query.run(inputOutputRecord,true,true);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		creatorControl.verify();
		callDetectorControl.verify();
	}

}
