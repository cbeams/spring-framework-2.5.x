
package org.springframework.samples.petclinic.jdbc;

import org.springframework.samples.petclinic.AbstractClinicTests;
import org.springframework.test.context.ContextConfiguration;

/**
 * <p>
 * Integration tests for the {@link HsqlJdbcClinic} implementation.
 * </p>
 * <p>
 * "JdbcClinicTests-context.xml" determines the actual beans to test.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@ContextConfiguration
public class JdbcClinicTests extends AbstractClinicTests {
}
