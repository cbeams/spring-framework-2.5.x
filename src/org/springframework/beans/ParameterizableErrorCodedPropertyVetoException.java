package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.springframework.core.ParameterizableErrorCoded;

/**
 * Exception used by PropertyVetosException to wrap failures.
 * Clients can throw these.
 * @author Tony Falabella
 * @version $Id: ParameterizableErrorCodedPropertyVetoException.java,v 1.2 2004-01-06 22:24:25 jhoeller Exp $
 */
public class ParameterizableErrorCodedPropertyVetoException
    extends ErrorCodedPropertyVetoException implements ParameterizableErrorCoded {

	private Object[] errorArgs = null;

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
	public ParameterizableErrorCodedPropertyVetoException(String msg, PropertyChangeEvent event,
	                                                      String errorCode, Object[] errorArgs) {
		super(msg, event, errorCode);
		this.errorArgs = errorArgs;
		// No root cause
	}

	/* package */
	ParameterizableErrorCodedPropertyVetoException(PropertyVetoException ex) {
		super(ex);
		/*
		  Although the mesg has already been resolved, still
		  save the args since someone else may want them if we rethrow
		  this exception. (ie: perhaps they will re-resolve the msg
		  from a msgCat based on the errorCode and will need the args for this
		*/
		if (ex instanceof ParameterizableErrorCoded) {
			this.errorArgs = ((ParameterizableErrorCoded) ex).getErrorArgs();
		}
		else {
			this.errorArgs = null;
		}
	}

	/* package */
	ParameterizableErrorCodedPropertyVetoException(TypeMismatchException ex) {
		super(ex);
		this.errorArgs = null;
	}

	/* package */
	ParameterizableErrorCodedPropertyVetoException(MethodInvocationException ex) {
		super(ex);
		this.errorArgs = null;
	}

	/**
	 * Implementation of ParameterizableErrorCoded interface.
	 * @return An array of objects to be used as params to replace
	 * placeholders within the errorCode message text.
	 */
	public Object[] getErrorArgs() {
		return errorArgs;
	}

	public String toString() {
		StringBuffer msgBuff = null;
		msgBuff.append("ErrorCodedPropertyVetoException: message=[" + getMessage() + "]; " +
		               "errorCode=[" + getErrorCode() + "]");
		if (this.errorArgs != null) {
			msgBuff.append("; errorArgs=[");
			for (int i = 0; i < this.errorArgs.length; i++) {
			  msgBuff.append("(" + this.errorArgs[i].getClass().getName() +
			                 ")[" + this.errorArgs[i] + "]");
				if (i < this.errorArgs.length-1)
					msgBuff.append(", ");
			}

			msgBuff.append("]");
		}
		return msgBuff.toString();
	}

}
