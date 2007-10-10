
package org.springframework.samples.petclinic.hibernate;

import junit.framework.JUnit4TestAdapter;

import org.springframework.samples.petclinic.AbstractClinicTests;
import org.springframework.test.context.ContextConfiguration;

/**
 * <p>
 * Integration tests for the {@link HibernateClinic} implementation.
 * </p>
 * <p>
 * "HibernateClinicTests-context.xml" determines the actual beans to test.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@ContextConfiguration
public class HibernateClinicTests extends AbstractClinicTests {

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(HibernateClinicTests.class);
	}
}
