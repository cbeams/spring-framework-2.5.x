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

import org.easymock.MockControl;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jca.cci.core.CciSpecsHolder;
import org.springframework.jca.cci.core.DefaultCciSpecsHolder;
import org.springframework.jca.cci.core.CciTemplate;
import org.springframework.jca.cci.core.ConnectionCallback;
import org.springframework.jca.cci.core.InteractionCallback;
import org.springframework.jca.cci.core.OutputRecordCreator;
import org.springframework.jca.cci.core.RecordExtractor;
import org.springframework.jca.cci.core.RecordGenerator;
import org.springframework.jca.cci.core.RecordGeneratorFromFactory;

/**
 * @author Thierry TEMPLIER
 */
public class CciTemplateTests extends TestCase {

	/**
	 * Test that the "IndexedRecord createIndexedRecord(String)" method
	 * is called when "createIndexedRecord(String);"is executed.
	 * @throws ResourceException
	 */
	public void testCreateIndexedRecord() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl recordFactoryControl = MockControl.createStrictControl(RecordFactory.class);
		final RecordFactory recordFactory = (RecordFactory) recordFactoryControl.getMock();
		MockControl indexedRecordControl = MockControl.createStrictControl(IndexedRecord.class);
		final IndexedRecord indexedRecord = (IndexedRecord) indexedRecordControl.getMock();

		connectionFactory.getRecordFactory();
		connectionFactoryControl.setReturnValue(recordFactory,1);

		recordFactory.createIndexedRecord("name");
		recordFactoryControl.setReturnValue(indexedRecord,1);

		connectionFactoryControl.replay();
		recordFactoryControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.createIndexedRecord("name");

		connectionFactoryControl.verify();
		recordFactoryControl.verify();
	}

	/**
	 * Test that the "MappedRecord createMappedRecord(String)" method
	 * is called when "createMappedRecord(String);"is executed.
	 * @throws ResourceException
	 */
	public void testCreateMappedRecord() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl recordFactoryControl = MockControl.createStrictControl(RecordFactory.class);
		final RecordFactory recordFactory = (RecordFactory) recordFactoryControl.getMock();
		MockControl mappedRecordControl = MockControl.createStrictControl(MappedRecord.class);
		final MappedRecord mappedRecord = (MappedRecord) mappedRecordControl.getMock();

		connectionFactory.getRecordFactory();
		connectionFactoryControl.setReturnValue(recordFactory,1);

		recordFactory.createMappedRecord("name");
		recordFactoryControl.setReturnValue(mappedRecord,1);

		connectionFactoryControl.replay();
		recordFactoryControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.createMappedRecord("name");

		connectionFactoryControl.verify();
		recordFactoryControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,record,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputOutput() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection,1);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction,1);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,inputRecord,outputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,record,extract,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputOutputExtractor() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		extractor.extractData(outputRecord);
		extractorControl.setReturnValue(null);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		extractorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,inputRecord,outputRecord,extractor);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		extractorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrue() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl recordControl = MockControl.createStrictControl(Record.class);
		final Record record = (Record)recordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		CciTemplate ct = new CciTemplate(connectionFactory);
		try {
			ct.execute(specs,inputRecord,true);
			assertTrue(false);
		} catch(DataAccessResourceFailureException ex) {
			assertTrue(true);
		}
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueWithCreator() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl creatorControl = MockControl.createStrictControl(OutputRecordCreator.class);
		final OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		creator.createOutputRecord();
		creatorControl.setReturnValue(outputRecord);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		creatorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory,creator);
		ct.execute(specs,inputRecord,true);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		creatorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,false);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputFalse() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputRecord);
		interactionControl.setReturnValue(outputRecord,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,inputRecord,false);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,extractor,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputExtractorTrue() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		CciTemplate ct = new CciTemplate(connectionFactory);
		try {
			ct.execute(specs,inputRecord,extractor,true);
			assertTrue(false);
		} catch(DataAccessResourceFailureException ex) {
			assertTrue(true);
		}
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,extractor,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputExtractorTrueWithCreator() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();
		MockControl creatorControl = MockControl.createStrictControl(OutputRecordCreator.class);
		final OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		creator.createOutputRecord();
		creatorControl.setReturnValue(outputRecord);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		extractor.extractData(outputRecord);
		extractorControl.setReturnValue(obj);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		extractorControl.replay();
		creatorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory,creator);
		ct.execute(specs,inputRecord,extractor,true);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		extractorControl.verify();
		creatorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,extractor,false);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputExtractorFalse() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputRecord);
		interactionControl.setReturnValue(outputRecord,1);

		extractor.extractData(outputRecord);
		extractorControl.setReturnValue(obj);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		extractorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,inputRecord,extractor,false,false);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		extractorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,generator,record,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorOutput() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		generator.generateRecord(obj);
		generatorControl.setReturnValue(inputRecord);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);
	
		connection.createInteraction();
		connectionControl.setReturnValue(interaction);
	
		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);
	
		interaction.close();
		interactionControl.setVoidCallable(1);
	
		connection.close();
		connectionControl.setVoidCallable(1);
	
		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();
	
		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,obj,generator,outputRecord);
	
		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
	}
	
	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,generator,record,extract,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorOutputExtractor() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		generator.generateRecord(obj);
		generatorControl.setReturnValue(inputRecord);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);
	
		connection.createInteraction();
		connectionControl.setReturnValue(interaction);
	
		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);
	
		extractor.extractData(outputRecord);
		extractorControl.setReturnValue(null);
	
		interaction.close();
		interactionControl.setVoidCallable(1);
	
		connection.close();
		connectionControl.setVoidCallable(1);
	
		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();
		extractorControl.replay();
	
		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,obj,generator,outputRecord,extractor);
	
		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
		extractorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),object,generator,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorTrue() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();
		CciTemplate ct = new CciTemplate(connectionFactory);
		try {
			ct.execute(specs,obj,generator,true);
			assertTrue(false);
		} catch(DataAccessResourceFailureException ex) {
			assertTrue(true);
		}
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,generator,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorTrueWithCreator() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();
		MockControl creatorControl = MockControl.createStrictControl(OutputRecordCreator.class);
		final OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		generator.generateRecord(obj);
		generatorControl.setReturnValue(inputRecord);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		creator.createOutputRecord();
		creatorControl.setReturnValue(outputRecord);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();
		creatorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory,creator);
		ct.execute(specs,obj,generator,true);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
		creatorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,generator,false);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorFalse() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		generator.generateRecord(obj);
		generatorControl.setReturnValue(inputRecord);

		interaction.execute(interactionSpec,inputRecord);
		interactionControl.setReturnValue(outputRecord,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,obj,generator,false);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),object,generator,extractor,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorExtractorTrue() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();
		CciTemplate ct = new CciTemplate(connectionFactory);
		try {
			ct.execute(specs,obj,generator,extractor,true);
			assertTrue(false);
		} catch(DataAccessResourceFailureException ex) {
			assertTrue(true);
		}
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,generator,extractor,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorExtractorTrueWithCreator() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();
		MockControl creatorControl = MockControl.createStrictControl(OutputRecordCreator.class);
		final OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		creator.createOutputRecord();
		creatorControl.setReturnValue(outputRecord);

		generator.generateRecord(obj);
		generatorControl.setReturnValue(inputRecord);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		extractor.extractData(outputRecord);
		extractorControl.setReturnValue(obj);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();
		extractorControl.replay();
		creatorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory,creator);
		ct.execute(specs,obj,generator,extractor,true);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
		extractorControl.verify();
		creatorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,generator,extractor,false);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputGeneratorExtractorFalse() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		generator.generateRecord(obj);
		generatorControl.setReturnValue(inputRecord);

		interaction.execute(interactionSpec,inputRecord);
		interactionControl.setReturnValue(outputRecord,1);

		extractor.extractData(outputRecord);
		extractorControl.setReturnValue(obj);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();
		extractorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,obj,generator,extractor,false);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
		extractorControl.verify();
	}

	/**
	 * Test that the "boolean execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,record,true);"
	 * is executed.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputOutputConnectionSpec() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputRecord = (Record)inputRecordControl.getMock();
		MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
		final Record outputRecord = (Record)outputRecordControl.getMock();

		MockControl connectionSpecControl = MockControl.createStrictControl(ConnectionSpec.class);
		ConnectionSpec connectionSpec = (ConnectionSpec)connectionSpecControl.getMock();
		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(connectionSpec,interactionSpec);

		connectionFactory.getConnection(connectionSpec);
		connectionFactoryControl.setReturnValue(connection,1);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction,1);

		interaction.execute(interactionSpec,inputRecord,outputRecord);
		interactionControl.setReturnValue(true,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,inputRecord,outputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
	}

	/**
	 * Test that the CCI ResultSet is closed at the end of the
	 * execution of "template.execute(record,false);".
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputOutputResultsSetFalse() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl recordControl = MockControl.createStrictControl(Record.class);
		final Record record = (Record)recordControl.getMock();
		MockControl resultsetControl = MockControl.createStrictControl(ResultSet.class);
		final ResultSet resultset = (ResultSet)resultsetControl.getMock();
		MockControl generatorControl=MockControl.createStrictControl(RecordGenerator.class);
		RecordGenerator generator=(RecordGenerator)generatorControl.getMock();
		MockControl extractorControl=MockControl.createStrictControl(RecordExtractor.class);
		RecordExtractor extractor=(RecordExtractor)extractorControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object obj=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		generator.generateRecord(obj);
		generatorControl.setReturnValue(record);

		interaction.execute(interactionSpec,record);
		interactionControl.setReturnValue(resultset,1);

		extractor.extractData(resultset);
		extractorControl.setReturnValue(obj);

		resultset.close();
		resultsetControl.setVoidCallable(1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		generatorControl.replay();
		extractorControl.replay();
		resultsetControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,obj,generator,extractor,false);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		generatorControl.verify();
		extractorControl.verify();
		resultsetControl.verify();
	}

	/**
	 * Test that the CCI Connection callback is called during the
	 * execution of "template.execute(holder,callback);".
	 * @throws ResourceException
	 */
	public void testTemplateExecuteConnectionCallback() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl connectionCallbackControl = MockControl.createStrictControl(ConnectionCallback.class);
		ConnectionCallback connectionCallback = (ConnectionCallback) connectionCallbackControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object o=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connectionCallback.doInConnection(specs,connection);
		connectionCallbackControl.setReturnValue(o);
		
		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		connectionCallbackControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,connectionCallback);

		connectionFactoryControl.verify();
		connectionControl.verify();
		connectionCallbackControl.verify();
	}

	/**
	 * Test that the CCI Interaction callback is called during the
	 * execution of "template.execute(holder,callback);".
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInteractionCallback() throws ResourceException,SQLException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl interactionCallbackControl = MockControl.createStrictControl(InteractionCallback.class);
		InteractionCallback interactionCallback = (InteractionCallback) interactionCallbackControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
		Object o=new Object();

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interactionCallback.doInInteraction(specs,interaction);
		interactionCallbackControl.setReturnValue(o);
		
		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		interactionCallbackControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		ct.execute(specs,interactionCallback);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		interactionCallbackControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueTrueWithCreator() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();
		MockControl creatorControl = MockControl.createStrictControl(OutputRecordCreator.class);
		final OutputRecordCreator creator = (OutputRecordCreator)creatorControl.getMock();

		MockControl inputOutputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputOutputRecord = (Record)inputOutputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputOutputRecord,inputOutputRecord);
		interactionControl.setReturnValue(true,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();
		creatorControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory,creator);
		Record tmpOutputRecord=(Record)ct.execute(specs,inputOutputRecord,true,true);
		assertSame(tmpOutputRecord,inputOutputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
		creatorControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputTrueTrue() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputOutputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputOutputRecord = (Record)inputOutputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputOutputRecord,inputOutputRecord);
		interactionControl.setReturnValue(true,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		Record tmpOutputRecord=(Record)ct.execute(specs,inputOutputRecord,true,true);
		assertSame(tmpOutputRecord,inputOutputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),record,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputFalseTrue() throws ResourceException {
		MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
		final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
		MockControl connectionControl = MockControl.createStrictControl(Connection.class);
		Connection connection = (Connection) connectionControl.getMock();
		MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
		Interaction interaction = (Interaction) interactionControl.getMock();

		MockControl inputOutputRecordControl = MockControl.createStrictControl(Record.class);
		final Record inputOutputRecord = (Record)inputOutputRecordControl.getMock();

		MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
		InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();

		CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);

		connectionFactory.getConnection();
		connectionFactoryControl.setReturnValue(connection);

		connection.createInteraction();
		connectionControl.setReturnValue(interaction);

		interaction.execute(interactionSpec,inputOutputRecord);
		interactionControl.setReturnValue(null,1);

		interaction.close();
		interactionControl.setVoidCallable(1);

		connection.close();
		connectionControl.setVoidCallable(1);

		connectionFactoryControl.replay();
		connectionControl.replay();
		interactionControl.replay();

		CciTemplate ct = new CciTemplate(connectionFactory);
		Record tmpOutputRecord=(Record)ct.execute(specs,inputOutputRecord,false,true);
		assertSame(tmpOutputRecord,inputOutputRecord);

		connectionFactoryControl.verify();
		connectionControl.verify();
		interactionControl.verify();
	}

	/**
	 * Test that the "Record execute(InteractionSpec,Record,Record)" method
	 * is called when "ct.execute(new CciSpecs(null,null),obj,true);" 
	 * is executed when a output record creator is specified.
	 * @throws ResourceException
	 */
	public void testTemplateExecuteInputExtractorFactoryRecord() throws ResourceException {
	  MockControl connectionFactoryControl = MockControl.createStrictControl(ConnectionFactory.class);
	  final ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryControl.getMock();
	  MockControl connectionControl = MockControl.createStrictControl(Connection.class);
	  Connection connection = (Connection) connectionControl.getMock();
	  MockControl interactionControl = MockControl.createStrictControl(Interaction.class);
	  Interaction interaction = (Interaction) interactionControl.getMock();
	  MockControl recordGeneratorFromFactoryControl=MockControl.createStrictControl(RecordGeneratorFromFactory.class); 
	  RecordGeneratorFromFactory recordGeneratorFromFactory=(RecordGeneratorFromFactory)recordGeneratorFromFactoryControl.getMock(); 
	
	  MockControl inputRecordControl = MockControl.createStrictControl(Record.class);
	  final Record inputRecord = (Record)inputRecordControl.getMock();
	  MockControl outputRecordControl = MockControl.createStrictControl(Record.class);
	  final Record outputRecord = (Record)outputRecordControl.getMock();
	
	  MockControl interactionSpecControl = MockControl.createStrictControl(InteractionSpec.class);
	  InteractionSpec interactionSpec = (InteractionSpec)interactionSpecControl.getMock();
	
	  CciSpecsHolder specs=new DefaultCciSpecsHolder(null,interactionSpec);
	  Object obj=new Object();
	
	  recordGeneratorFromFactory.setConnectionFactory(connectionFactory);
	  recordGeneratorFromFactoryControl.setVoidCallable(1);

	  recordGeneratorFromFactory.generateRecord(obj);
	  recordGeneratorFromFactoryControl.setReturnValue(inputRecord,1);
	
	  connectionFactory.getConnection();
	  connectionFactoryControl.setReturnValue(connection,1);
		
	  connection.createInteraction();
	  connectionControl.setReturnValue(interaction);
		
	  interaction.execute(interactionSpec,inputRecord,outputRecord);
	  interactionControl.setReturnValue(true,1);
		
	  interaction.close();
	  interactionControl.setVoidCallable(1);
		
	  connection.close();
	  connectionControl.setVoidCallable(1);
		
	  connectionFactoryControl.replay();
	  connectionControl.replay();
	  interactionControl.replay();
	  recordGeneratorFromFactoryControl.replay();
		
	  CciTemplate ct = new CciTemplate(connectionFactory);
	  ct.execute(specs,obj,recordGeneratorFromFactory,outputRecord);
		
	  connectionFactoryControl.verify();
	  connectionControl.verify();
	  interactionControl.verify();
	  recordGeneratorFromFactoryControl.verify();

	}
}