package org.springframework.samples.petclinic.hibernate;

import org.springframework.samples.petclinic.AbstractClinicTests;

/**
 *  Live Unit tests for HsqlJdbcClinic implementations.
 * 	"applicationContext-jdbc.xml" determines which implementation is live-tested.
 *
 *  @author Juergen Hoeller
 */
public class HibernateClinicTests extends AbstractClinicTests {

	protected String getContextLocation() {
		return "/org/springframework/samples/petclinic/hibernate/applicationContext-hibernate.xml";
	}

}
