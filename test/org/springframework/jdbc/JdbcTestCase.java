/*
 * JdbcTestCase.java
 *
 * Copyright (C) 2002 by Interprise Software.  All rights reserved.
 */
package org.springframework.jdbc;

import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * @task enter type comments
 * 
 * @author <a href="mailto:tcook@interprisesoftware.com">Trevor D. Cook</a>
 * @version $Id: JdbcTestCase.java,v 1.1 2003-09-24 13:43:20 beanie42 Exp $
 */
public abstract class JdbcTestCase extends TestCase {

	protected MockControl ctrlDataSource;
	protected DataSource mockDataSource;
	protected MockControl ctrlConnection;
	protected Connection mockConnection;

	/**
	 * 
	 */
	public JdbcTestCase() {
		super();
	}

	/**
	 * @param name
	 */
	public JdbcTestCase(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		ctrlConnection = MockControl.createControl(Connection.class);
		mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.getMetaData();
		ctrlConnection.setDefaultReturnValue(null);
		mockConnection.close();
		ctrlConnection.setDefaultVoidCallable();

		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		ctrlDataSource.setDefaultReturnValue(mockConnection);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		ctrlDataSource.verify();
		ctrlConnection.verify();
	}

	protected void replay() {
		ctrlDataSource.replay();
		ctrlConnection.replay();
	}

}
