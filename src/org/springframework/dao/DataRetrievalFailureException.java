package org.springframework.dao;

/**
 * Exception thrown if certain expected data could not be retrieved, e.g.
 * when looking up specific data via a known identifier. This exception
 * will be thrown either by O/R mapping tools or by DAO implementations.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class DataRetrievalFailureException extends DataAccessException {

	public DataRetrievalFailureException(String msg) {
		super(msg);
	}

	public DataRetrievalFailureException(String msg, Throwable ex) {
		super(msg, ex);
	}
	
}
