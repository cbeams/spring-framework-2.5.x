package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.springframework.core.ErrorCoded;
import org.springframework.core.HasRootCause;

/**
 * Exception used by PropertyVetoException to wrap failures.
 * Clients can throw these.
 * @author Rod Johnson
 * @version $Id: ErrorCodedPropertyVetoException.java,v 1.2 2004-01-06 22:24:25 jhoeller Exp $
 */
public class ErrorCodedPropertyVetoException extends PropertyVetoException implements ErrorCoded, HasRootCause {

	public static final String TYPE_MISMATCH_ERROR_CODE = "typeMismatch";

	private static final String METHOD_INVOCATION_ERROR_CODE = "methodInvocation";

	private String errorCode;

	private Throwable rootCause;

	/**
	 * Create new <code>ErrorCodedPropertyVetoException</code>.
	 * This signature will be called when either the caller has an object
	 * that has an ErrorCoded interface and they are calling us with that,
	 * or if they want to use the ErrorCoded ability of this exception.
	 * <p>Note: Message passed in will already have been "resolved".
	 * We will take the string passed in literally as is.
	 * This means that the caller of this method either created a literal string
	 * and passed it to us, OR the caller looked up the string value for a
	 * message themself in a MessageSource BEFORE calling us.
	 */
	public ErrorCodedPropertyVetoException(String msg, PropertyChangeEvent event, String errorCode) {
		super(msg, event);
		this.errorCode = errorCode;
		// No root cause
	}

	/**
	 * Only have this method here so that subclasses may call this overload
	 * to get the superclass's behavior.
	 */
	/* package */
	ErrorCodedPropertyVetoException(String mesg, PropertyChangeEvent e) {
		super(mesg, e);
	}

	/* package */
	ErrorCodedPropertyVetoException(PropertyVetoException ex) {
		super(ex.getMessage(), ex.getPropertyChangeEvent());
		if (ex instanceof ErrorCoded) {
			errorCode = ((ErrorCoded) ex).getErrorCode();
		}
		rootCause = ex;
	}

	/* package */
	ErrorCodedPropertyVetoException(TypeMismatchException ex) {
		super(ex.getMessage(), ex.getPropertyChangeEvent());
		rootCause = ex.getRootCause();
		errorCode = TYPE_MISMATCH_ERROR_CODE;
	}

	/* package */
	ErrorCodedPropertyVetoException(MethodInvocationException ex) {
		super(ex.getMessage(), ex.getPropertyChangeEvent());
		rootCause = ex.getRootCause();
		errorCode = METHOD_INVOCATION_ERROR_CODE;
	}

	/** Implementation of ErrorCoded interface.
	 * Return the error code associated with this failure.
	 * The GUI can render this anyway it pleases, allowing for Int8ln etc.
	 * @return a String error code associated with this failure
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/** Implementation of HasRootCause */
	public Throwable getRootCause() {
		return rootCause;
	}

	public String toString() {
		return "ErrorCodedPropertyVetoException: message=[" + getMessage() + "]; errorCode=[" + getErrorCode() + "]";
	}
}
