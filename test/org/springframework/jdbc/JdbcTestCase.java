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
 * @version $Id: JdbcTestCase.java,v 1.3 2003-11-03 16:59:42 johnsonr Exp $
 */
public abstract class JdbcTestCase extends TestCase {

	protected MockControl ctrlDataSource;
	protected DataSource mockDataSource;
	protected MockControl ctrlConnection;
	protected Connection mockConnection;
	
	/**
	 * Set to true if the user wants verification, indicated
	 * by a call to replay(). We need to make this optional,
	 * otherwise we setUp() will always result in verification failures
	 */
	private boolean shouldVerify;

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
		this.shouldVerify = false;
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

		// We shouldn't verify unless the user called replay()
		if (shouldVerify()) {
			ctrlDataSource.verify();
			ctrlConnection.verify();
		}
	}

	protected boolean shouldVerify() {
		return this.shouldVerify;
	}

	protected void replay() {
		this.shouldVerify = true;
		ctrlDataSource.replay();
		ctrlConnection.replay();
	}

}
