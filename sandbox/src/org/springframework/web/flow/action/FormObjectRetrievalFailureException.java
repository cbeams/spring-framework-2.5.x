package org.springframework.web.flow.action;


/**
 * Exception thrown if a form object could not be retrieved via its identifier.
 * Provides information about the persistent class and the identifier.
 * @author Keith Donald 
 * @author Juergen Hoeller
 */
public class FormObjectRetrievalFailureException extends RuntimeException {

	private String formObjectName;

	private Object formObjectClass;

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the default "not found" message.
	 * @param formObjectClass the persistent class
	 * @param formObjectName the ID of the object that should have been retrieved
	 */
	public FormObjectRetrievalFailureException(Class formObjectClass, String formObjectName) {
		this(formObjectClass, formObjectName,
				"Form object of class [" + formObjectClass.getName() + "] with formObjectName [" + formObjectName + "]: not found",
				null);
	}

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the given explicit message and exception.
	 * @param formObjectClass the persistent class
	 * @param formObjectName the ID of the object that should have been retrieved
	 * @param msg exception message
	 * @param ex source exception
	 */
	public FormObjectRetrievalFailureException(
			Class formObjectClass, String formObjectName, String msg, Throwable ex) {
		super(msg, ex);
		this.formObjectClass = formObjectClass;
		this.formObjectName = formObjectName;
	}

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the default "not found" message.
	 * @param formObjectClassName the name of the persistent class
	 * @param formObjectName the ID of the object that should have been retrieved
	 */
	public FormObjectRetrievalFailureException(String formObjectClassName, String formObjectName) {
		this(formObjectClassName, formObjectName,
				"Form object of class [" + formObjectClassName + "] with formObjectName [" + formObjectName + "]: not found",
				null);
	}

	/**
	 * Create a new ObjectRetrievalFailureException for the given object,
	 * with the given explicit message and exception.
	 * @param formObjectClassName the name of the persistent class
	 * @param formObjectName the ID of the object that should have been retrieved
	 * @param msg exception message
	 * @param ex source exception
	 */
	public FormObjectRetrievalFailureException(
			String formObjectClassName, String formObjectName, String msg, Throwable ex) {
		super(msg, ex);
		this.formObjectClass = formObjectClassName;
		this.formObjectName = formObjectName;
	}

	/**
	 * Return the persistent class of the object that was not found.
	 * If no Class was specified, this method returns null.
	 */
	public Class getFormObjectClass() {
		return (this.formObjectClass instanceof Class ? (Class) this.formObjectClass : null);
	}

	/**
	 * Return the name of the persistent class of the object that was not found.
	 * Will work for both Class objects and String names.
	 */
	public String getFormObjectClassName() {
		return (this.formObjectClass instanceof Class ?
				((Class) this.formObjectClass).getName() : this.formObjectClass.toString());
	}

	/**
	 * Return the formObjectName of the object that was not found.
	 */
	public Object getFormObjectName() {
		return formObjectName;
	}

}