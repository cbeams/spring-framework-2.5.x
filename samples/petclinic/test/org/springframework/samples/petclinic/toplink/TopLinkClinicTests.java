package org.springframework.samples.petclinic.toplink;

import org.springframework.samples.petclinic.AbstractClinicTests;

/**
 * Live unit tests for TopLinkClinic implementation.
 * "applicationContext-toplink.xml" determines the actual beans to test.
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
public class TopLinkClinicTests extends AbstractClinicTests {

	protected String getConfigPath() {
		return "applicationContext-toplink.xml";
	}

}
