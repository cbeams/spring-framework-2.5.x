package org.springframework.orm.hibernate;

import net.sf.hibernate.HibernateException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Hibernate-specific subclass of DataAccessException, for Hibernate system
 * errors that do not match any concrete org.springframework.dao exceptions.
 * Used by SessionFactoryUtils and HibernateTemplate.
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see SessionFactoryUtils#convertHibernateAccessException
 * @see HibernateTemplate#convertHibernateAccessException
 * @see org.springframework.dao.DataAccessException
 */
public class HibernateSystemException extends UncategorizedDataAccessException {

	public HibernateSystemException(HibernateException ex) {
		super(ex.getMessage(), ex);
	}

}
