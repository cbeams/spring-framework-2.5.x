package org.springframework.orm.hibernate;

import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.WrongClassException;
import net.sf.hibernate.ObjectDeletedException;

import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * Hibernate-specific subclass of ObjectRetrievalFailureException.
 * Converts Hibernate's ObjectNotFoundException, ObjectDeletedException,
 * and WrongClassException.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class HibernateObjectRetrievalFailureException extends ObjectRetrievalFailureException {

	public HibernateObjectRetrievalFailureException(ObjectNotFoundException ex) {
		super(ex.getPersistentClass(), ex.getIdentifier(), ex.getMessage(), ex);
	}

	public HibernateObjectRetrievalFailureException(ObjectDeletedException ex) {
		super(null, ex.getIdentifier(), ex.getMessage(), ex);
	}

	public HibernateObjectRetrievalFailureException(WrongClassException ex) {
		super(ex.getPersistentClass(), ex.getIdentifier(), ex.getMessage(), ex);
	}

}
