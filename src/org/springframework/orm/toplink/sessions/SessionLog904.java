/*
 * Created on Jan 2, 2005
 *
 */
package org.springframework.orm.toplink.sessions;

import java.io.Writer;

import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.SessionLog;
import oracle.toplink.sessions.SessionLogEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * This class can be used to redirect TopLink 9.0.4.x Session logging to an commons logging framework.
 * Most of the methods in this class are ignored.  Users should switch to filtering log messages using whatever
 * logging framework is configured in commons-logging.  
 * 
 * This class currently sends all TopLink Session logging to a Logger called 'toplink'.
 *
 * @author jclark
 */
public class SessionLog904 implements SessionLog
{
    /** TopLink Session log is redirected to a logger called 'toplink' */
    private Log logger = LogFactory.getLog("toplink");

    /**
     * send log entries to whatever Logger is currently configured in commons logging
     */
    public void log(SessionLogEntry entry)
    {
        Session session = entry.getSession();
    	String sessionName = "Session";
    	if (session.isUnitOfWork()) {
    		sessionName = "UnitOfWork";
    	} else if (session.isServerSession()) {
    		sessionName = "ServerSession";
    	} else if (session.isClientSession()) {
    		sessionName = "ClientSession";
    	} else if (session.isSessionBroker()) {
    		sessionName = "SessionBroker";
    	} else if (session.isRemoteSession()) {
    		sessionName = "RemoteSession";
    	} else if (session.isDatabaseSession()) {
    		sessionName = "DatabaseSession";
    	}
    	
    	String sessionString = sessionName+"("+String.valueOf(System.identityHashCode(session))+")";

        if (entry.isDebug())
        {
            this.logger.debug(sessionString+" "+entry.getMessage());
        }
        else if (entry.hasException())
        {
            this.logger.warn(sessionString+" "+entry.getException().getMessage(),entry.getException());
        }
        else
        {
            this.logger.info(sessionString+" "+entry.getMessage());
        }
    }
    
    public void setShouldLogDebug(boolean flag)
    {
    }
    public void setShouldLogExceptions(boolean flag)
    {
    }
    public void setShouldLogExceptionStackTrace(boolean flag)
    {
    }
    public void setShouldPrintConnection(boolean flag)
    {
    }
    public void setShouldPrintDate(boolean flag)
    {
    }
    public void setShouldPrintSession(boolean flag)
    {
    }
    public void setShouldPrintThread(boolean flag)
    {
    }
    public Writer getWriter()
    {
        return null;
    }
    public void setWriter(Writer log)
    {
    }
    public boolean shouldLogDebug()
    {
        return false;
    }
    public boolean shouldLogExceptions()
    {
        return false;
    }
    public boolean shouldLogExceptionStackTrace()
    {
        return false;
    }
    public boolean shouldPrintConnection()
    {
        return false;
    }
    public boolean shouldPrintDate()
    {
        return false;
    }
    public boolean shouldPrintSession()
    {
        return false;
    }
    public boolean shouldPrintThread()
    {
        return false;
    }
}
