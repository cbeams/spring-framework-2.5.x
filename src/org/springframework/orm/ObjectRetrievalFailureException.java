package org.springframework.orm;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Exception thrown if a mapped object could not be retrieved via its identifier.
 * Provides information about the persistent class and the identifier. 
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class ObjectRetrievalFailureException extends DataRetrievalFailureException {

	private Class persistentClass;

	private Object identifier;

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the default "not found" message.
	 * @param persistentClass the persistent class
	 * @param identifier the ID of the object that should have been retrieved
	 */
	public ObjectRetrievalFailureException(Class persistentClass, Object identifier) {
		this(persistentClass, identifier, "Object of class '" + persistentClass.getName() +
																			"' with identifier [" + identifier + ": not found", null);
	}

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the given explicit message.
	 * @param persistentClass the persistent class
	 * @param identifier the ID of the object that should have been retrieved
	 * @param msg exception message
	 */
	public ObjectRetrievalFailureException(Class persistentClass, Object identifier, String msg, Throwable ex) {
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
