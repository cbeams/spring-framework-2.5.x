package org.springframework.validation.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * @author Daniel Miller
 */
public class Resources {
	
	private static Log log = LogFactory.getLog(Resources.class);

	public static void rejectValue(Errors errors, Field field, ValidatorAction va) {
		String fieldCode = field.getKey();
		String errorCode = Resources.getMsgKey(va, field);
		Object[] args = Resources.getArgs(va, field);
		String defaultMsg = "???" + errorCode + "???";
		
		log.debug("Rejecting value [field='" + fieldCode + "', errorCode='" + errorCode + "']");

		errors.rejectValue(fieldCode, errorCode, args, defaultMsg);
	}
	
	/**
	 * Gets the <code>ActionError</code> based on the <code>ValidatorAction</code> message and the
	 * <code>Field</code>'s arg objects.
	 * 
	 * @param request the servlet request
	 * @param va Validator action
	 * @param field the validator Field
	 */
	public static String getMsgKey(ValidatorAction va, Field field) {
		return (field.getMsg(va.getName()) != null ? field.getMsg(va.getName()) : va.getMsg());
	}

	/**
	 * <p>Gets the message arguments based on the current <code>ValidatorAction</code>
	 * and <code>Field</code>. The array returned here is an array of
	 * MessageSourceResolvable's that will be resolved at a later time.</p>
	 * 
	 * <p>Note: this implementation is especially crappy (only four arguments are supported),
	 * but it's the best we can do until the next version of commons-validator is out of
	 * beta.</p> 
	 * 
	 * @param actionName action name.
	 * @param field the validator field.
	 * @return array of message keys.
	 */
	public static Object[] getArgs(ValidatorAction va, Field field) {
		List args = new ArrayList();
		String actionName = va.getName();
		if (field.getArg0(actionName) != null) {
			args.add(0, createMessage(field.getArg0(actionName).getKey()));
		}
		if (field.getArg1(actionName) != null) {
			args.add(1, createMessage(field.getArg1(actionName).getKey()));
		}
		if (field.getArg2(actionName) != null) {
			args.add(2, createMessage(field.getArg2(actionName).getKey()));
		}
		if (field.getArg3(actionName) != null) {
			args.add(3, createMessage(field.getArg3(actionName).getKey()));
		}
		return args.toArray();
	}
	
	/**
	 * Create a MessageSourceResolvable using the string value of the parameter as
	 * a code.
	 * 
	 * @param obj Object whose string value is the code for this message.
	 * @return MessageSourceResolvable for the given Object.
	 */
	public static MessageSourceResolvable createMessage(Object obj) {
		String[] codes = new String[] { String.valueOf(obj) };
		String defaultMsg = "???" + codes[0] + "???";
		MessageSourceResolvable msg =
				new DefaultMessageSourceResolvable(codes, null, defaultMsg);
		return msg;
	}
	
	/**
	 * Get a message for the given validator action and field from the specified
	 * message source.
	 * 
	 * @param messages MessageSource from which to get the message.
	 * @param locale Locale for for this message.
	 * @param va ValidatorAction for this message.
	 * @param field Field field for this message. 
	 */
	public static String getMessage(MessageSource messages, Locale locale, ValidatorAction va, Field field) {
		String code = getMsgKey(va, field);
		Object[] args = getArgs(va, field);
		String defaultMsg = "???" + code + "???";
		return messages.getMessage(code, args, defaultMsg, locale);
	}
}
