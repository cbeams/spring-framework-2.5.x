/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.SqlParameter;

/**
 * Superclass for object abstractions of RDBMS stored procedures.
 * This class is abstract and its execute methods are protected, preventing use other than through
 * a subclass that offers tighter typing.
 *
 * <p>The inherited sql property is the name of the stored procedure in the RDBMS.
 * Note that JDBC 3.0 introduces named parameters, although the other features provided
 * by this class are still necessary in JDBC 3.0.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @version $Id: StoredProcedure.java,v 1.4 2003-09-17 01:14:49 trisberg Exp $
 */
public abstract class StoredProcedure extends SqlCall {

	/**
	 * Allow use as a bean.
	 */
	protected StoredProcedure() {
	}

	/**
	 * Create a new object wrapper for a stored procedure.
	 * @param ds DataSource to use throughout the lifetime
	 * of this object to obtain connections
	 * @param name name of the stored procedure in the database.
	 */
	protected StoredProcedure(DataSource ds, String name) {
		setDataSource(ds);
		setSql(name);
	}

	/**
	 * Overridden method. Add a parameter.
	 * <b>NB: Calls to addParameter must be made in the same
	 * order as they appear in the database's stored procedure parameter
	 * list.</b> Names are purely used to help mapping
	 * @param p Parameter object (as defined in the Parameter
	 * inner class)
	 */
	public void declareParameter(SqlParameter p) throws InvalidDataAccessApiUsageException {
		if (p.getName() == null)
			throw new InvalidDataAccessApiUsageException("Parameters to stored procedures must have names as well as types");
		super.declareParameter(p);
	}

	/**
	 * Execute the stored procedure. Subclasses should define a strongly typed
	 * execute method (with a meaningful name) that invokes this method, populating
	 * the input map and extracting typed values from the output map. Subclass
	 * execute methods will often take domain objects as arguments and return values.
	 * Alternatively, they can return void.
	 * @param inParams map of input parameters, keyed by name as in parameter
	 * declarations. Output parameters need not (but can be) included in this map.
	 * It is legal for map entries to be null, and this will produce the correct
	 * behavior using a NULL argument to the stored procedure.
	 * @return map of output params, keyed by name as in parameter declarations.
	 * Output parameters will appear here, with their values after the
	 * stored procedure has been called.
	 */
	public Map execute(final Map inParams) throws InvalidDataAccessApiUsageException {
		validateParameters(inParams);
		logger.debug("Executing call: " + getCallString());
		Map retValues = getJdbcTemplate().execute(newCallableStatementCreator(inParams), this.getDeclaredParameters());

		return retValues;
	}

}
