package org.springframework.jdbc.core;

/**
 * Interface to be implemented by objects that can provide SQL strings.
 *
 * <p>Typically implemented by statement creators that want to expose the
 * SQL they use to create their statements, to allow for better contextual
 * information in case of exceptions.
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @see PreparedStatementCreator
 * @see CallableStatementCreator
 */
public interface SqlProvider {

	/**
	 * Return the SQL string for this object,
	 * typically the SQL used for creating statements.
	 * @return the SQL string, or null
	 */
	String getSql();

}
