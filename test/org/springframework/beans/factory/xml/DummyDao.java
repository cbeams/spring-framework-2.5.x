/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.xml;

import javax.sql.DataSource;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DummyDao.java,v 1.1 2003-11-22 15:08:28 johnsonr Exp $
 */
public class DummyDao {
	
	DataSource ds;

	public DummyDao(DataSource ds) {
		this.ds = ds;
	}

}
