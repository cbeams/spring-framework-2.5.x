package org.springframework.orm.hibernate;

import net.sf.hibernate.StaleObjectStateException;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Hibernate-specific subclass of ObjectOptimisticLockingFailureException.
 * Converts Hibernate's StaleObjectStateException.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class HibernateOptimisticLockingFailureException extends ObjectOptimisticLockingFailureException {

	public HibernateOptimisticLockingFailureException(StaleObjectStateException ex) {
		super(ex.getPersistentClass(), ex.getIdentifier(), ex.getMessage());
	}

}
