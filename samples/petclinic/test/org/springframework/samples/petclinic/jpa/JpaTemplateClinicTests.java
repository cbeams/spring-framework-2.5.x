package org.springframework.samples.petclinic.jpa;

/**
 * Tests for the DAO variant based on Spring's JpaTemplate.
 * Uses TopLink Essentials (the reference implementation) for testing.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class JpaTemplateClinicTests extends AbstractJpaClinicTests {

	protected String[] getConfigPaths() {
		return new String[] {
			"applicationContext-jpaCommon.xml",
			"applicationContext-toplinkAdapter.xml",
			"applicationContext-entityManager.xml"
		};
	}

}
