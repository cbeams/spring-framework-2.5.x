/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.selector;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * A simple, not robust, implementation of a MapMessage to perform unit tests.  It does
 * not support nested MapMessages.  
 * 
 * Basic setter/getter for the MapMessage is working.  
 * Check the source code to see what is implemented.
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class SimpleMapMessage implements MapMessage
{
    /**
     * Store the body of the map message here.
     */
    private Map _bodyMap;

    public SimpleMapMessage()
    {
        _bodyMap = new HashMap();

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String arg0) throws JMSException
    {
        return ((Boolean) _bodyMap.get(arg0)).booleanValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getByte(java.lang.String)
     */
    public byte getByte(String arg0) throws JMSException
    {
        return ((Byte) _bodyMap.get(arg0)).byteValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getShort(java.lang.String)
     */
    public short getShort(String arg0) throws JMSException
    {
        return ((Short) _bodyMap.get(arg0)).shortValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getChar(java.lang.String)
     */
    public char getChar(String arg0) throws JMSException
    {
        return ((Character) _bodyMap.get(arg0)).charValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getInt(java.lang.String)
     */
    public int getInt(String arg0) throws JMSException
    {
        return ((Integer) _bodyMap.get(arg0)).intValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getLong(java.lang.String)
     */
    public long getLong(String arg0) throws JMSException
    {
        return ((Long) _bodyMap.get(arg0)).longValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getFloat(java.lang.String)
     */
    public float getFloat(String arg0) throws JMSException
    {
        return ((Float) _bodyMap.get(arg0)).floatValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getDouble(java.lang.String)
     */
    public double getDouble(String arg0) throws JMSException
    {
        return ((Double) _bodyMap.get(arg0)).doubleValue();
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getString(java.lang.String)
     */
    public String getString(String arg0) throws JMSException
    {
        return ((String) _bodyMap.get(arg0));
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getBytes(java.lang.String)
     */
    public byte[] getBytes(String arg0) throws JMSException
    {
        return (byte[]) _bodyMap.get(arg0);
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getObject(java.lang.String)
     */
    public Object getObject(String arg0) throws JMSException
    {
        return _bodyMap.get(arg0);
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#getMapNames()
     */
    public Enumeration getMapNames() throws JMSException
    {
        return Collections.enumeration(_bodyMap.keySet());
    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setBoolean(java.lang.String, boolean)
     */
    public void setBoolean(String arg0, boolean arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Boolean(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setByte(java.lang.String, byte)
     */
    public void setByte(String arg0, byte arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Byte(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setShort(java.lang.String, short)
     */
    public void setShort(String arg0, short arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Short(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setChar(java.lang.String, char)
     */
    public void setChar(String arg0, char arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Character(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setInt(java.lang.String, int)
     */
    public void setInt(String arg0, int arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Integer(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setLong(java.lang.String, long)
     */
    public void setLong(String arg0, long arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Long(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setFloat(java.lang.String, float)
     */
    public void setFloat(String arg0, float arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Float(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setDouble(java.lang.String, double)
     */
    public void setDouble(String arg0, double arg1) throws JMSException
    {
        _bodyMap.put(arg0, new Double(arg1));

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setString(java.lang.String, java.lang.String)
     */
    public void setString(String arg0, String arg1) throws JMSException
    {
        _bodyMap.put(arg0, arg1);

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[])
     */
    public void setBytes(String arg0, byte[] arg1) throws JMSException
    {
        _bodyMap.put(arg0, arg1);

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[], int, int)
     */
    public void setBytes(String arg0, byte[] barray, int offset, int length)
        throws JMSException
    {
        byte[] bytes = null;
        if (barray != null)
        {
            bytes = new byte[length];
            System.arraycopy(barray, offset, bytes, 0, length);
        }
        _bodyMap.put(arg0, bytes);

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#setObject(java.lang.String, java.lang.Object)
     */
    public void setObject(String arg0, Object arg1) throws JMSException
    {
        _bodyMap.put(arg0, arg1);

    }

    /* (non-Javadoc)
     * @see javax.jms.MapMessage#itemExists(java.lang.String)
     */
    public boolean itemExists(String arg0) throws JMSException
    {
        return _bodyMap.containsKey(arg0);
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSMessageID()
     */
    public String getJMSMessageID() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSMessageID(java.lang.String)
     */
    public void setJMSMessageID(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSTimestamp()
     */
    public long getJMSTimestamp() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSTimestamp(long)
     */
    public void setJMSTimestamp(long arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
     */
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
     */
    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
     */
    public void setJMSCorrelationID(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSCorrelationID()
     */
    public String getJMSCorrelationID() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSReplyTo()
     */
    public Destination getJMSReplyTo() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
     */
    public void setJMSReplyTo(Destination arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSDestination()
     */
    public Destination getJMSDestination() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
     */
    public void setJMSDestination(Destination arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSDeliveryMode()
     */
    public int getJMSDeliveryMode() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSDeliveryMode(int)
     */
    public void setJMSDeliveryMode(int arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSRedelivered()
     */
    public boolean getJMSRedelivered() throws JMSException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSRedelivered(boolean)
     */
    public void setJMSRedelivered(boolean arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSType()
     */
    public String getJMSType() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSType(java.lang.String)
     */
    public void setJMSType(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSExpiration()
     */
    public long getJMSExpiration() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSExpiration(long)
     */
    public void setJMSExpiration(long arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getJMSPriority()
     */
    public int getJMSPriority() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setJMSPriority(int)
     */
    public void setJMSPriority(int arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#clearProperties()
     */
    public void clearProperties() throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getBooleanProperty(java.lang.String)
     */
    public boolean getBooleanProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getByteProperty(java.lang.String)
     */
    public byte getByteProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getShortProperty(java.lang.String)
     */
    public short getShortProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getIntProperty(java.lang.String)
     */
    public int getIntProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getLongProperty(java.lang.String)
     */
    public long getLongProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getFloatProperty(java.lang.String)
     */
    public float getFloatProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getDoubleProperty(java.lang.String)
     */
    public double getDoubleProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getStringProperty(java.lang.String)
     */
    public String getStringProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getObjectProperty(java.lang.String)
     */
    public Object getObjectProperty(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#getPropertyNames()
     */
    public Enumeration getPropertyNames() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
     */
    public void setBooleanProperty(String arg0, boolean arg1)
        throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
     */
    public void setByteProperty(String arg0, byte arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setShortProperty(java.lang.String, short)
     */
    public void setShortProperty(String arg0, short arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setIntProperty(java.lang.String, int)
     */
    public void setIntProperty(String arg0, int arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setLongProperty(java.lang.String, long)
     */
    public void setLongProperty(String arg0, long arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
     */
    public void setFloatProperty(String arg0, float arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
     */
    public void setDoubleProperty(String arg0, double arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setStringProperty(java.lang.String, java.lang.String)
     */
    public void setStringProperty(String arg0, String arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#setObjectProperty(java.lang.String, java.lang.Object)
     */
    public void setObjectProperty(String arg0, Object arg1) throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#acknowledge()
     */
    public void acknowledge() throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody() throws JMSException
    {
        _bodyMap = new HashMap();

    }

}
