package org.springframework.beans;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Combined exception, composed of individual binding propertyAccessExceptions.
 * An object of this class is created at the beginning of the binding
 * process, and errors added to it as necessary.
 *
 * <p>The binding process continues when it encounters application-level
 * propertyAccessExceptions, applying those changes that can be applied and storing
 * rejected changes in an object of this class.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 18 April 2001
 * @version $Id: PropertyAccessExceptionsException.java,v 1.4 2004-03-03 18:34:49 jhoeller Exp $
 */
public class PropertyAccessExceptionsException extends BeansException {

	/** BeanWrapper wrapping the target object for binding */
	private final BeanWrapper beanWrapper;

	/** List of PropertyAccessException objects */
	private final PropertyAccessException[] propertyAccessExceptions;

	/**
	 * Create new empty PropertyAccessExceptionsException.
	 * We'll add errors to it as we attempt to bind properties.
	 */
	protected PropertyAccessExceptionsException(BeanWrapper beanWrapper,
	                                            PropertyAccessException[] propertyAccessExceptions) {
		super("");
		this.beanWrapper = beanWrapper;
		this.propertyAccessExceptions = propertyAccessExceptions;
	}

	/**
	 * Return the BeanWrapper that generated this exception.
	 */
	public BeanWrapper getBeanWrapper() {
		return beanWrapper;
	}

	/**
	 * Return the object we're binding to.
	 */
	public Object getBindObject() {
		return this.beanWrapper.getWrappedInstance();
	}

	/**
	 * If this returns 0, no errors were encountered during binding.
	 */
	public int getExceptionCount() {
		return this.propertyAccessExceptions.length;
	}

	/**
	 * Return an array of the propertyAccessExceptions stored in this object.
	 * Will return the empty array (not null) if there were no errors.
	 */
	public PropertyAccessException[] getPropertyAccessExceptions() {
		return this.propertyAccessExceptions;
	}

	/**
	 * Return the exception for this field, or null if there isn't one.
	 */
	public PropertyAccessException getPropertyAccessException(String propertyName) {
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			PropertyAccessException pae = this.propertyAccessExceptions[i];
			if (propertyName.equals(pae.getPropertyChangeEvent().getPropertyName())) {
				return pae;
			}
		}
		return null;
	}

	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.toString());
		sb.append("; nested propertyAccessExceptions are: ");
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			PropertyAccessException pae = this.propertyAccessExceptions[i];
			sb.append("[");
			sb.append(pae.getClass().getName());
			sb.append(": ");
			sb.append(pae.getMessage());
			sb.append(']');
			if (i < this.propertyAccessExceptions.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public void printStackTrace(PrintStream ps) {
		ps.println(this);
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			PropertyAccessException pae = this.propertyAccessExceptions[i];
			pae.printStackTrace(ps);
		}
	}

	public void printStackTrace(PrintWriter pw) {
		pw.println(this);
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			PropertyAccessException pae = this.propertyAccessExceptions[i];
			pae.printStackTrace(pw);
		}
	}

	public String toString() {
		return "PropertyAccessExceptionsException (" + getExceptionCount() + " errors)";
	}

}
