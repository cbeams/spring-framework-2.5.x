package org.springframework.samples.petclinic.ojb;

import org.springframework.samples.petclinic.AbstractClinicTests;

/**
 * Live unit tests for OJB PersistenceBrokerClinic implementation.
 * "applicationContext-ojb.xml" determines the actual beans to test.
 *
 * @author Juergen Hoeller
 * @since 06.07.2004
 */
public class PersistenceBrokerClinicTests extends AbstractClinicTests {

	protected String getContextConfigLocation() {
		return "/org/springframework/samples/petclinic/ojb/applicationContext-ojb.xml";
	}

}
