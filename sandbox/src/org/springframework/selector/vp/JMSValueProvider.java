package org.springframework.selector.vp;

import org.springframework.selector.parser.IValueProvider;
import org.springframework.selector.parser.Identifier;
import org.springframework.selector.parser.NumericValue;

// JMS
import javax.jms.Message;
import javax.jms.MapMessage;
import javax.jms.JMSException;

import java.util.StringTokenizer;

/**
 * Value provider for JMS. As an extension to the JMS specification, this implementation allows values to be 
 * extracted from <tt>JMS</tt> message <tt>body</tt> using a <tt>dot</tt> notation.<p>
 * The <tt>dot</tt> notation provides reference to message body fields. For example, <tt>.order.quantity</tt> 
 * would access the <tt>quantity</tt> field of the nested sub-message field <tt>order</tt>, if it exists. If the 
 * field does not exist, it returns <tt>null</tt>.
 * @author Jawaid Hakim.
 */
public class JMSValueProvider implements IValueProvider
{
    /**
     * Factory.
     * @param msg Message to extract values from.
     * @return Instance.
     */
    public static JMSValueProvider valueOf(Message msg)
    {
        return new JMSValueProvider(msg);
    }

    /**
     * Ctor.
     * @param msg <tt>Message</tt> from which to extract values.<p>
     * For example, <tt>order.quantity</tt> would select the <tt>quantity</tt> field of the 
     * nested sub-message field <tt>order</tt> if it exists. If the field does not exist, it 
     * returns <tt>null</tt>.
     * @see #getValue(org.springframework.selector.parser.Identifier, Object)
     */
    private JMSValueProvider(Message msg)
    {
        msg_ = msg;
    }

    /**
     * Get the value of the specified identifier.
     * @param identifier Field identifier.
     * @param correlation Application correlation data. May be <tt>null</tt>.
     * @return Value of the specified identifier. Returns <tt>null</tt> if value
     * of the specified identifier is not found.
     */
    public Object getValue(Object identifier, Object correlation)
    {
        try
        {
        	/**
        	 * Wrap numeric values in NumericValue instance
        	 */
            Object value = (getValue(msg_, (Identifier)identifier));
			if ((value instanceof Integer))
				return new NumericValue((Integer)value);
        		
			if ((value instanceof Float))
				return new NumericValue((Float)value);

			if ((value instanceof Double))
				return new NumericValue((Double)value);

			if ((value instanceof Long))
				return new NumericValue((Long)value);

			if ((value instanceof Short))
				return new NumericValue((Short)value);

			if ((value instanceof Byte))
				return new NumericValue((Byte)value);

			return value;           
        }
        catch (JMSException ex)
        {
            throw new IllegalArgumentException(ex.toString());
        }
    }

    private static Object getValue(Message msg, Identifier identifier) throws JMSException
    {
        if (identifier.isJMSHeader())
            return getHeaderValue(msg, identifier);

        String fldName = identifier.getIdentifier();

        // Leading '.' is there to indicate that this identifier references a nested
        // message field (not a property)
        int ind = fldName.indexOf('.');
        if (ind >= 0)
        {
            // Skip over leading '.' if any
            if (ind == 0)
                fldName = fldName.substring(1);

            for (StringTokenizer strTok = new StringTokenizer(identifier.getIdentifier(), ".", false); strTok.hasMoreTokens();)
            {
                if (!(msg instanceof MapMessage))
                    return null;
                    
                MapMessage mapMsg = (MapMessage) msg;

                fldName = strTok.nextToken();
                Object nestedMsg = mapMsg.getObject(fldName);
                if (strTok.hasMoreTokens())
                {
                    if (!(nestedMsg instanceof MapMessage))
                        return null;
                        
                    msg = (Message) nestedMsg;
                }
                else
                    return nestedMsg;
            }
        }

		// Must be a property
        if (msg.propertyExists(fldName))
        {
            return msg.getObjectProperty(fldName);
        }
        return null;
    }

    private static Object getHeaderValue(Message msg, Identifier identifier) throws JMSException
    {
        String prop = identifier.getIdentifier();
		if (prop.equals("JMSMessageID"))
			return msg.getJMSMessageID();
        else if (prop.equals("JMSPriority"))
            return new Integer(msg.getJMSPriority());
        else if (prop.equals("JMSTimestamp"))
            return new Long(msg.getJMSTimestamp());
        else if (prop.equals("JMSCorrelationID"))
            return msg.getJMSCorrelationID();
		else if (prop.equals("JMSDeliveryMode"))
			return new Integer(msg.getJMSDeliveryMode());
        else if (prop.equals("JMSRedelivered"))
            return new Boolean(msg.getJMSRedelivered());
		else if (prop.equals("JMSType"))
			return msg.getJMSType();
        else if (prop.equals("JMSExpiration"))
            return new Long(msg.getJMSExpiration());

        throw new java.lang.IllegalStateException("Invalid JMS property referenced: " + prop);
    }

    private final Message msg_;
}
