package org.springframework.orm.jdo;

import javax.jdo.JDOException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * JDO-specific subclass of DataAccessException, for Hibernate system
 * errors that do not match any concrete org.springframework.dao exceptions.
 * Used by PersistenceManagerFactoryUtils and JdoTemplate.
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
 * @see JdoTemplate#convertJdoAccessException
 * @see org.springframework.dao.DataAccessException
 */
public class JdoSystemException extends UncategorizedDataAccessException {

	public JdoSystemException(JDOException ex) {
		super(ex.getMessage(), ex);
	}
	
}
