package org.springframework.orm.jdo;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * JDO exception that gets thrown on invalid API usage.
 * @author Juergen Hoeller
 * @since 03.06.2003
 */
public class JdoUsageException extends InvalidDataAccessApiUsageException {

	public JdoUsageException(String s, Throwable ex) {
		super(s, ex);
	}

}
