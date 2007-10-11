
package org.springframework.samples.petclinic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link Owner}.
 *
 * @author Ken Krebs
 */
@RunWith(JUnit4ClassRunner.class)
public class OwnerTests {

	@Test
	public void testHasPet() {
		Owner owner = new Owner();
		Pet fido = new Pet();
		fido.setName("Fido");
		assertNull(owner.getPet("Fido"));
		assertNull(owner.getPet("fido"));
		owner.addPet(fido);
		assertEquals(fido, owner.getPet("Fido"));
		assertEquals(fido, owner.getPet("fido"));
	}
}
