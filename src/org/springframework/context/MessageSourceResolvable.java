/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.context;

/**
 * Interface for objects that are suitable for message resolution
 * in a MessageSource, e.g. validation errors.
 * @author Tony Falabella
 * @author Juergen Hoeller
 * @see MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 * @see org.springframework.validation.ObjectError
 */
public interface MessageSourceResolvable {

	/**
	 * Return the codes to be used to resolve this message, in the order that
	 * they should get tried. The last code will therefore be the default one.
	 * @return a String code associated with this message
	 */
	public String[] getCodes();

	/**
	 * Return the array of arguments to be used to resolve this message.
	 * @return an array of objects to be used as parameters to replace
	 * placeholders within the code message text
	 * @see <a href="http://java.sun.com/j2se/1.3/docs/api/java/text/MessageFormat.html">java.text.MessageFormat</a>
	 */
	public Object[] getArguments();

	/**
	 * Return the default message to be used to resolve this message.
	 * @return the default message, or null if no default
	 */
	public String getDefaultMessage();

}
