package org.springframework.orm.hibernate;

import net.sf.hibernate.QueryException;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Exception thrown on invalid HQL query syntax.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class HibernateQueryException extends InvalidDataAccessResourceUsageException {

	private String queryString;

	public HibernateQueryException(QueryException ex) {
		super(ex.getMessage(), ex.getCause());
		this.queryString = ex.getQueryString();
	}

	/**
	 * Return the HQL query string that was invalid.
	 */
	public String getQueryString() {
		return queryString;
	}

}
