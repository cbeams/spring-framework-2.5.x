package org.springframework.samples.petclinic.jdbc;

import org.springframework.samples.petclinic.AbstractClinicTests;

/**
 * Live unit tests for HsqlJdbcClinic implementation.
 * "applicationContext-jdbc.xml" determines the actual beans to test.
 *
 * @author Juergen Hoeller
 */
public class JdbcClinicTests extends AbstractClinicTests {

	protected String getContextConfigLocation() {
		return "/org/springframework/samples/petclinic/jdbc/applicationContext-jdbc.xml";
	}

}
