package org.springframework.orm.jdo;

import javax.jdo.JDOException;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * JDO-specific subclass of DataAccessException, for JDO usage exception
 * that do not match any concrete org.springframework.dao exceptions.
 * Used by PersistenceManagerFactoryUtils and JdoTemplate.
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
 * @see JdoTemplate#convertJdoAccessException
 * @see org.springframework.dao.DataAccessException
 */
public class JdoUsageException extends InvalidDataAccessApiUsageException {

	public JdoUsageException(JDOException ex) {
		super(ex.getMessage(), ex);
	}

}
