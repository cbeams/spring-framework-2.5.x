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

	/**
	 * Create a new ObjectOptimisticLockingFailureException for the given object,
	 * with the default "optimistic locking failed" message.
	 * @param persistentClass the persistent class
	 * @param identifier the ID of the object for which the locking failed
	 */
	public ObjectOptimisticLockingFailureException(Class persistentClass, Object identifier) {
		this(persistentClass, identifier, "Object of class [" + persistentClass.getName() +
		                                  "] with identifier [" + identifier + "]: optimistic locking failed", null);
	}

	/**
	 * Create a new ObjectOptimisticLockingFailureException for the given object,
	 * with the given explicit message.
	 * @param persistentClass the persistent class
	 * @param identifier the ID of the object for which the locking failed
	 * @param msg exception message
	 * @param ex source exception
	 */
	public ObjectOptimisticLockingFailureException(Class persistentClass, Object identifier,
																								 String msg, Throwable ex) {
		super(msg, ex);
		this.persistentClass = persistentClass;
		this.identifier = identifier;
	}

	/**
	 * Return the persistent class of the object for which the locking failed.
	 */
	public Class getPersistentClass() {
		return persistentClass;
	}

	/**
	 * Return the identifier of the object for which the locking failed.
	 */
	public Object getIdentifier() {
		return identifier;
	}

}
