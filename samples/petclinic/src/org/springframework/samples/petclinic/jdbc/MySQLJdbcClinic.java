/*
 * MySQLJdbcClinic.java
 *
 */

package org.springframework.samples.petclinic.jdbc;

/**
 * MySQL JDBC implementation of the Clinic interface.
 * Defines the identity query for MySQL.
 *
 * @author Juergen Hoeller
 */
public class MySQLJdbcClinic extends AbstractJdbcClinic {

	protected String getIdentityQuery() {
		return "select last_insert_id()";
	}

}
