/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.util.List;

/**
 * Extension of RowCallbackHandler interfaces that saves the
 * accumulated results as a List.
 * @author Rod Johnson
 * @version $Id: ResultReader.java,v 1.3 2004-03-17 08:48:53 jhoeller Exp $
 */
public interface ResultReader extends RowCallbackHandler {
	 
	/**
	 * Return all results, disconnected from the JDBC ResultSet.
	 * Never returns null; returns the empty collection if there
	 * were no results.
	 */
	List getResults();

}
