package org.springframework.samples.petclinic.toplink;

import java.io.IOException;
import java.io.Writer;

import oracle.toplink.exceptions.ValidationException;
import oracle.toplink.platform.database.HSQLPlatform;
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

	public boolean supportsNativeSequenceNumbers() {
		return true;
	}

	public boolean shouldNativeSequenceAcquireValueAfterInsert() {
		return true;
	}

	public boolean shouldUseJDBCOuterJoinSyntax() {
		return false;
	}

	public ValueReadQuery buildSelectQueryForNativeSequence() {
		return new ValueReadQuery("CALL IDENTITY()");
	}

	public void printFieldIdentityClause(Writer writer) throws ValidationException {
		try {
			writer.write(" IDENTITY");
		}
		catch (IOException ioException) {
			throw new ValidationException();
		}
	}

}
