/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.support.incrementer;

import org.springframework.dao.DataAccessException;

/**
 * Interface that defines contract of incrementing any data store field's
 * maximum value. Works much like a sequence number generator.
 *
 * <p>Typical implementations can use RDBMS SQL, native RDBMS sequences,
 * and/or Stored Procedures to do the job.
 *
 * @author Dmitriy Kopylenko
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @version $Id: DataFieldMaxValueIncrementer.java,v 1.2 2004-02-27 08:28:37 jhoeller Exp $
 */
public interface DataFieldMaxValueIncrementer {

	/**
	 * Increments data store field's max value as int.
	 * @return int next data store value such as <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException
	 */
	int nextIntValue() throws DataAccessException;

	/**
	 * Increments data store field's max value as long.
	 * @return int next data store value such as <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException
	 */
	long nextLongValue() throws DataAccessException;

	/**
	 * Increments data store field's max value as String.
	 * @return next data store value such as <b>max + 1</b>
	 * @throws org.springframework.dao.DataAccessException
	 */
	String nextStringValue() throws DataAccessException;

}
