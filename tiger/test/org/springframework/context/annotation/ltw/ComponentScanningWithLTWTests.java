package org.springframework.context.annotation.ltw;

import org.springframework.test.jpa.AbstractJpaTests;

/**
 * Test to ensure that component scanning work with load-time weaver.
 * See SPR-3873 for more details.
 * 
 * @author Ramnivas Laddad
 *
 */
public class ComponentScanningWithLTWTests extends AbstractJpaTests {
	public ComponentScanningWithLTWTests() {
		setDependencyCheck(false);
	}
	
	@Override
	protected String getConfigPath() {
		return "ComponentScanningWithLTWTests.xml";
	}

	public void testLoading() {
		// do nothing as successful loading is the test
	}
}
