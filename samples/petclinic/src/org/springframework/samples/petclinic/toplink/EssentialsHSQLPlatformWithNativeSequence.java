package org.springframework.samples.petclinic.toplink;

import java.io.IOException;
import java.io.Writer;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.platform.database.HSQLPlatform;
import oracle.toplink.essentials.queryframework.ValueReadQuery;

/**
 * Subclass of TopLink Essentials's default HSQLPlatform class,
 * using native HSQLDB identity columns for id generation.
 *
 * <p>Necessary for PetClinic's default data model, which relies on
 * identity columns: this is uniformly used across all persistence
 * layer implementations (JDBC, Hibernate, OJB, and TopLink).
 *
 * @author Juergen Hoeller
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 1.2
 */
public class EssentialsHSQLPlatformWithNativeSequence extends HSQLPlatform {

	public EssentialsHSQLPlatformWithNativeSequence() {
		//setUsesNativeSequencing(true);
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

	public void printFieldIdentityClause(Writer writer) throws ValidationException {
		try {
			writer.write(" IDENTITY");
		}
		catch (IOException ex) {
			throw ValidationException.fileError(ex);
		}
	}

}
