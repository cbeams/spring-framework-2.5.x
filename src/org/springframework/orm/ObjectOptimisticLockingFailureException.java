package org.springframework.orm;

import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Exception thrown on an optimistic locking violation for a mapped object.
 * Provides information about the persistent class and the identifier.
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class ObjectOptimisticLockingFailureException extends OptimisticLockingFailureException {

	private Class persistentClass;

	private Object identifier;

	public ObjectOptimisticLockingFailureException(Class persistentClass, Object identifier) {
		this(persistentClass, identifier, "Object of class '" + persistentClass.getName() +
		                                  "' with identifier [" + identifier + ": optimistic locking failed", null);
	}

	public ObjectOptimisticLockingFailureException(Class persistentClass, Object identifier,
																								 String msg, Throwable ex) {
		super(msg, ex);
		this.persistentClass = persistentClass;
		this.identifier = identifier;
	}

	/**
	 * Return the persistent class of the object that was not found.
	 */
	public Class getPersistentClass() {
		return persistentClass;
	}

	/**
	 * Return the identifier of the object that was not found.
	 */
	public Object getIdentifier() {
		return identifier;
	}

}
