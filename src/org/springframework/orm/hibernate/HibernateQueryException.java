package org.springframework.orm.hibernate;

import net.sf.hibernate.QueryException;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Exception thrown on invalid HQL query syntax.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class HibernateQueryException extends InvalidDataAccessResourceUsageException {

	public HibernateQueryException(QueryException ex) {
		super(ex.getMessage(), ex);
	}

	/**
	 * Return the HQL query string that was invalid.
	 */
	public String getQueryString() {
		return ((QueryException) getRootCause()).getQueryString();
	}

}
