package org.springframework.context.support;

import java.io.Serializable;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the MessageSourceResolvable interface.
 * Easy way to store all the necessary values needed to resolve
 * messages from a MessageSource.
 * @author Tony Falabella
 * @author Juergen Hoeller
 * @version $Id: MessageSourceResolvableImpl.java,v 1.2 2004-02-04 18:44:07 jhoeller Exp $
 */
public class MessageSourceResolvableImpl implements MessageSourceResolvable, Serializable {

  private final String[] codes;

  private final Object[] arguments;

	private final String defaultMessage;

  /**
   * Create a new instance, using multiple codes and a
   * default message.
   * @see MessageSourceResolvable#getCodes
   */
	public MessageSourceResolvableImpl(String[] codes, Object[] arguments, String defaultMessage) {
    this.codes = codes;
    this.arguments = arguments;
    this.defaultMessage = defaultMessage;
  }

	/**
	 * Create a new instance, using multiple codes.
	 * @see MessageSourceResolvable#getCodes
	 */
  public MessageSourceResolvableImpl(String[] codes, Object[] arguments) {
    this(codes, arguments, null);
  }

	/**
	 * Copy constructor: Create a new instance from another resolvable.
	 */
  public MessageSourceResolvableImpl(MessageSourceResolvable resolvable) {
    this(resolvable.getCodes(), resolvable.getArguments(), resolvable.getDefaultMessage());
  }

  public String[] getCodes() {
    return codes;
  }

	/**
	 * Return the default code of this resolvable,
	 * i.e. the last one in the codes array.
	 */
	public String getCode() {
		return (codes != null && codes.length > 0) ? codes[codes.length-1] : null;
	}

	public Object[] getArguments() {
	  return arguments;
	}

	public String getDefaultMessage() {
	  return defaultMessage;
	}

  protected String resolvableToString() {
    StringBuffer msgBuff = new StringBuffer();
    msgBuff.append("codes=[" + StringUtils.arrayToDelimitedString(getCodes(), ",") + "]; arguments=[");
    if (arguments == null) {
      msgBuff.append("null");
    }
    else {
      for (int i = 0; i < getArguments().length; i++) {
        msgBuff.append("(" + getArguments()[i].getClass().getName() +
                       ")[" + getArguments()[i] + "]");
	      if (i < getArguments().length-1)
		      msgBuff.append(", ");
      }
    }
    msgBuff.append("]; defaultMessage=[" + getDefaultMessage() + "]");
    return msgBuff.toString();
  }

	public String toString() {
		return resolvableToString();
	}

}
