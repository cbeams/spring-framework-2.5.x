package org.springframework.samples.petclinic.jpa;

import java.util.List;

import org.springframework.samples.petclinic.aspects.UsageLogAspect;

/**
 * Tests for the DAO variant based on the shared EntityManager approach.
 *
 * @author Rod Johnson
 */
public class EntityManagerClinicTests extends AbstractJpaClinicTests {

	private UsageLogAspect usageLogAspect;

	public void setUsageLogAspect(UsageLogAspect usageLogAspect) {
		this.usageLogAspect = usageLogAspect;
	}

	protected String[] getConfigLocations() {
		return new String[] {
			"/org/springframework/samples/petclinic/jpa/applicationContext-jpaCommon.xml",
			"/org/springframework/samples/petclinic/jpa/applicationContext-entityManager.xml"
		};
	}

	public void testUsageLogAspectIsInvoked() {
		String name1 = "Schuurman";
		String name2 = "Greenwood";
		String name3 = "Leau";
		
		assertTrue(clinic.findOwners(name1).isEmpty());
		assertTrue(clinic.findOwners(name2).isEmpty());
		
		List<String> namesRequested = usageLogAspect.getNamesRequested();
		assertTrue(namesRequested.contains(name1));
		assertTrue(namesRequested.contains(name2));
		assertFalse(namesRequested.contains(name3));
	}

}
