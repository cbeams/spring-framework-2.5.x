package org.springframework.samples.petclinic.jpa;

import org.springframework.samples.petclinic.AbstractJpaClinicTests;

/**
 * Live unit tests for JpaClinic implementation.
 * "applicationContext-jpa.xml" determines the actual beans to test.
 */
public class ContainerJpaClinicTests extends AbstractJpaClinicTests {

	protected String[] getConfigLocations() {
		return new String[] { 
				"/org/springframework/samples/petclinic/jpa/container-applicationContext-jpa.xml" 
		};
	}

}
