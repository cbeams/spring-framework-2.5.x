/*
 * Created on Nov 25, 2004
 *
 */

package org.springframework.samples.petclinic.toplink;

import oracle.toplink.internal.databaseaccess.HSQLPlatform;
import oracle.toplink.queryframework.ValueReadQuery;

/**
 * Subclass of TopLink's default HSQLPlatform class,
 * using native HSQLDB identity columns for id generation.
 * <b>Only works on TopLink 10.1.3 and higher.</b>
 *
 * <p>Necessary for PetClinic's default data model, which relies on
 * identity columns: this is uniformly used across all persistence
 * layer implementations (JDBC, Hibernate, OJB, and TopLink).
 *
 * @author Juergen Hoeller
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 1.2
 */
public class HSQLPlatformWithNativeSequence extends HSQLPlatform {

	public HSQLPlatformWithNativeSequence() {
		setUsesNativeSequencing(true);
	}

	public boolean supportsNativeSequenceNumbers() {
		return true;
	}

	public boolean shouldNativeSequenceAcquireValueAfterInsert() {
		return true;
	}

	public ValueReadQuery buildSelectQueryForNativeSequence() {
		return new ValueReadQuery("CALL IDENTITY()");
	}

}
