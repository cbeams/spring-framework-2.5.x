package org.springframework.samples.petclinic.hibernate;

import org.springframework.samples.petclinic.AbstractClinicTests;

/**
 * Live unit tests for HibernateClinic implementation.
 * "applicationContext-hibernate.xml" determines the actual beans to test.
 *
 * @author Juergen Hoeller
 */
public class HibernateClinicTests extends AbstractClinicTests {

	protected String getContextConfigLocation() {
		return "/org/springframework/samples/petclinic/hibernate/applicationContext-hibernate.xml";
	}

}
