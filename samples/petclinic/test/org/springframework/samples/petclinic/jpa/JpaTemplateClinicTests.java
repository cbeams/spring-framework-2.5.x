package org.springframework.samples.petclinic.jpa;

/**
 * Tests for the DAO variant based on Spring's JpaTemplate.
 *
 * @author Rod Johnson
 */
public class JpaTemplateClinicTests extends AbstractJpaClinicTests {

	protected String[] getConfigLocations() {
		return new String[] {
			"/org/springframework/samples/petclinic/jpa/applicationContext-jpaCommon.xml",
			"/org/springframework/samples/petclinic/jpa/applicationContext-jpaTemplate.xml"
		};
	}

}
