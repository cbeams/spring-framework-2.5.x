/*
@license@
  */ 

package org.springframework.orm.toplink;

import java.io.IOException;

import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.tools.sessionconfiguration.XMLLoader;
import oracle.toplink.tools.sessionmanagement.SessionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.toplink.sessions.SessionLog904;

/**
 * TopLink Session Configuration is done using a sessions.xml file.  The most convenient way
 * to create the sessions.xml file is to use the Oracle Toplink SessionsEditor workbench.  The sessions.xml file
 * contains all runtime configuration and points to a second XML or Class resource from which to load the actual
 * TopLink metadata.
 * 
 * The TopLinkSessionFactoryBean loads the sessions.xml file during initialization in order to boostrap the TopLink
 * ServerSession.  The name of the Session to be loaded and the name of the actual resource, if different from sessions.xml, 
 * can be configured on the SessionFactoryBean.  All resources (ie sessions.xml and Mapping Workbench metadata) are loaded
 * using <code>ClassLoader.getResourceAsStream</code> calls so users may need to configure which ClassLoader 
 * will have correct visibility.  This is especially important in J2EE environments where the TopLink metadata might be deployed
 * to a different location than the Spring configuration.  The ClassLoader used to search for the TopLink metadata will default to the 
 * the context ClassLoader for the current Thread.
 *
 * TopLink logging can be redirected to Commons logging by passing a logger name to the method 
 * <code>setRedirectTopLinkLogging(String loggerName)</code>.  Otherwise, TopLink uses it's own default SessionLog, whose levels
 * are configured in the sessions.xml file.
 * 
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 */
public class SessionFactoryBean implements FactoryBean, InitializingBean,
    DisposableBean
{
    /**
     * The default location of the sessions.xml Toplink configuration file
     */
    public static final String DEFAULT_SESSIONS_XML = "sessions.xml";

    /**
     * The default session name to look for in the sessions.xml
     */
    public static final String DEFAULT_SESSION_NAME = "Session";

    /**
     * The location of the sessions Toplink configuration file. This location
     * will be loaded with the classloader that loads this class.
     */
    private String sessionsConfig = DEFAULT_SESSIONS_XML;

    /**
     * The session name to look for in the sessions.xml configuration file
     */
    private String sessionName = DEFAULT_SESSION_NAME;
    
    /**
     * The ClassLoader to use to load the sessions.xml (and Project XML file). 
     * If nothing is set here, then we will try to use the ClassLoader that loaded
     * SessionFactoryBean.class
     */
    private ClassLoader sessionClassLoader;
    
    /**
     * set to true if all TopLink Session logging should be redirected to a commons-logging Log.  This
     * can be useful if you want the TopLink logs to be merged with the rest of the Spring framework's 
     * logging.
     */
    private boolean redirectLogging = false;
    /**
     * the name of the Commons-Logging Logger that should be used for all TopLink Session logging.
     */
    private String logName = null;
    
    protected SessionFactory sessionFactory;
    /**
     * The log for the bean
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Set the sessions.xml toplink configuration file to configure toplink
     * sessions. This method uses the classpath for locating the configuration
     * file. Toplink recommends placing the sessions.xml file in the META-INF
     * directory of the jar/war file and passing META-INF/sessions.xml as the
     * parameter.
     * 
     * @param sessionsConfig
     *            The location of the sessions.xml file (can be in a package)
     */
    public void setSessionsConfig(String sessionsConfig)
    {
        this.sessionsConfig = sessionsConfig;
    }

    /**
     * Set the session name to use from the sessions.xml configuration file. The
     * default Toplink configuration is 'Session'
     * 
     * @param sessionName
     *            The name of the session that is configured to be used with
     *            this bean
     */
    public void setSessionName(String sessionName)
    {
        this.sessionName = sessionName;
    }

    /**
     * Set the ClassLoader that should be used to lookup the sessions.xml Resource.  This ClassLoader will
     * also subsequently be used to load the Project metadata and will also be used, in the TopLink ConversionManager,
     * to load all TopLink Domain classes.  If this is not appropriate, users can configure a preLogin SessionEvent to alter
     * the ConversionManager ClassLoader that TopLink will use at runtime.
     * 
     * @param sessionClassLoader The sessionClassLoader to set.
     */
    public void setSessionClassLoader(ClassLoader sessionClassLoader)
    {
        this.sessionClassLoader = sessionClassLoader;
    }
    
    /**
     * Use this property if you want all TopLink Session logging to be redirected to 
     * a commons-logging Log.  This can be useful if you want the TopLink logs to 
     * be merged with the rest of the Spring framework's logging.
     * 
     * @param category - name of the Logger that will be used for Session logging
     */
    public void setRedirectTopLinkLogging(String category)
    {
        this.logName = category;
        this.redirectLogging = true;
    }

    /**
     * Initialize the SessionManager and Toplink configured session for the
     * given or the default location.
     * 
     * @throws IllegalArgumentException
     *             in case of illegal property values
     */
    public void afterPropertiesSet() throws IllegalArgumentException,
        IOException
    {
        //logger.info("initializing a SessionFactoryBean for SpringTopLink integration version ("+Version.class.getPackage().getImplementationVersion()+")");
        logger.info("initializing a SessionFactoryBean for SpringTopLink");
        if (sessionsConfig == null)
        {
            sessionsConfig = DEFAULT_SESSIONS_XML;
        }
        // Load the configuration file from the given location
        XMLLoader loader = new XMLLoader(this.sessionsConfig);
        // Get the session manager
        SessionManager sm = SessionManager.getManager();
        // Initialize the session factory using the session name
        // and the configuration file and try to login to the database
        
        // use either the ClassLoader which loaded this class or the one set by the user
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (this.sessionClassLoader!=null)
        {
            classLoader = this.sessionClassLoader;
        }
        
        DatabaseSession session = (DatabaseSession)sm.getSession(
                loader, 
                sessionName,
                classLoader, false, false);
        
        if(!session.isServerSession())
        {
            logger.fatal("The current sessions.xml file is configured to use a single-threaded DatabaseSession instead of a ServerSession");
            logger.fatal("The TopLink SessionFactory is designed to be used in a multi-threaded environment");
            throw new IllegalArgumentException("sessions.xml should not be configured to use a single-threaded DatabaseSession");
        }
        
        if (this.redirectLogging)
        {
            session.setSessionLog(new SessionLog904());
            session.logMessages();
        }
        
        if (!session.isConnected())
        {
            session.login();
        }

        // this is the actual SessionFactory
        this.sessionFactory = new SessionFactory(session);
    }

    /**
     * Return the singleton Toplink session
     * 
     * @return The Toplink session (usually a ServerSession object)
     */
    public Object getObject()
    {
        return this.sessionFactory;
    }

    /**
     * Get the type of the object
     * 
     * @return The class of the actual Toplink session
     */
    public Class getObjectType()
    {
		return (this.sessionFactory != null) ? this.sessionFactory.getClass() : SessionFactory.class;
    }

    /**
     * This should be a singleton
     * 
     * @return always true
     */
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * Close the preconfigured session
     */
    public void destroy()
    {
        this.sessionFactory.close();
    }
}