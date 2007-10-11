
package org.springframework.samples.petclinic.toplink;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import org.springframework.samples.petclinic.AbstractClinicTests;
import org.springframework.samples.petclinic.Owner;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;

/**
 * <p>
 * Integration tests for the {@link TopLinkClinic} implementation.
 * </p>
 * <p>
 * &quot;TopLinkClinicTests-context.xml&quot; determines the actual beans to
 * test.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 */
@ContextConfiguration
public class TopLinkClinicTests extends AbstractClinicTests {

	protected int originalNumOwners = -1;
	protected String testMethodName = null;


	@Test
	@Rollback(false)
	@Override
	public void insertOwner() {
		this.testMethodName = "insertOwner";
		Collection<Owner> owners = this.clinic.findOwners("Schultz");
		int found = owners.size();
		this.originalNumOwners = found;
		Owner owner = new Owner();
		owner.setLastName("Schultz");
		this.clinic.storeOwner(owner);

		// assertTrue(!owner.isNew()); -- NOT TRUE FOR TOPLINK (before commit)

		// TODO Uncomment once new owners are properly persisted with TopLink,
		// or rather once the persistence of a new owner is verifiable within
		// the current UnitOfWork (without having to commit the current
		// transaction) within the integration test.
		//
		// Also need to remove the @Rollback(false) declaration as well as the
		// verifyStateAfterTransaction() method (and perhaps even the complete,
		// overridden version of insertOwner() as well).
		//
		// Note that TopLinkClinic.storeOwner(Owner) actually works fine when
		// the transaction is committed (e.g., when deployed in a web app or
		// with the rollback flag set to false).
		//
		// owners = this.clinic.findOwners("Schultz");
		// assertEquals("Verifying number of owners after inserting a new one.",
		// found + 1, owners.size());
	}

	@AfterTransaction
	public void verifyStateAfterTransaction() {
		if ("insertOwner".equals(this.testMethodName)) {
			final Collection<Owner> owners = this.clinic.findOwners("Schultz");
			assertEquals("Verifying number of owners with last name [" + "Schultz"
					+ "] after the transaction has ended.", this.originalNumOwners + 1, owners.size());
		}
	}

}
