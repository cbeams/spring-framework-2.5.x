package org.springframework.orm.jdo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.ClassLoaderUtils;

/**
 * FactoryBean that creates a local JDO PersistenceManager instance.
 * Behaves like a PersistenceManagerFactory instance when used as bean
 * reference, e.g. for JdoTemplate's persistenceManagerFactory property.
 * Note that switching to JndiObjectFactoryBean is just a matter of
 * configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying data source) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings can either be read from a properties file,
 * specified as "configLocation", or completely via this class. Properties
 * specified as "jdoProperties" here will override any settings in a file.
 *
 * <p>This PersistenceManager handling strategy is most appropriate for
 * applications that solely use JDO for data access. In this case,
 * JdoTransactionManager is required for transaction demarcation, as
 * JTA support isn't possible if JDO isn't installed as JCA connector.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTemplate#setPersistenceManagerFactory
 * @see JdoTransactionManager#setPersistenceManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class LocalPersistenceManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private String configLocation;

	private Properties jdoProperties;

	private PersistenceManagerFactory persistenceManagerFactory;

	/**
	 * Set the location of the JDO properties config file as classpath
	 * resource, e.g. "/kodo.properties".
	 * <p>Note: Can be omitted when all necessary properties are
	 * specified locally via this bean.
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set JDO properties, like "javax.jdo.PersistenceManagerFactoryClass".
	 * <p>Can be used to override values in a JDO properties config file,
	 * or to specify all necessary properties locally.
	 */
	public void setJdoProperties(Properties jdoProperties) {
		this.jdoProperties = jdoProperties;
	}

	/**
	 * Initialize the PersistenceManagerFactory for the given location.
	 * @throws IllegalArgumentException in case of illegal property values
	 * @throws IOException if the properties could not be loaded from the given location
	 * @throws JDOException in case of JDO initialization errors
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, IOException, JDOException {
		if (this.configLocation == null && this.jdoProperties == null) {
			throw new IllegalArgumentException("Either configLocation (e.g. '/kodo.properties') or jdoProperties must be set");
		}

		Properties prop = new Properties();

		if (this.configLocation != null) {
			// load JDO properties from given location
			String resourceLocation = this.configLocation;
			InputStream in = ClassLoaderUtils.getResourceAsStream(resourceLocation);
			if (in == null) {
				throw new DataAccessResourceFailureException("Cannot open config location: " + resourceLocation);
			}
			prop.load(in);
		}

		if (this.jdoProperties != null) {
			// add given JDO properties
			prop.putAll(this.jdoProperties);
		}

		// build factory instance
		this.persistenceManagerFactory = newPersistenceManagerFactory(prop);
	}

	/**
	 * Subclasses can override this to perform custom initialization of the
	 * PersistenceManagerFactory instance, creating it via the given Properties
	 * that got prepared by this LocalPersistenceManagerFactoryBean
	 * <p>The default implementation invokes JDOHelper's getPersistenceManagerFactory.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom PersistenceManagerFactory implementation.
	 * @param prop Properties prepared by this LocalPersistenceManagerFactoryBean
	 * @return the PersistenceManagerFactory instance
	 * @see javax.jdo.JDOHelper#getPersistenceManagerFactory
	 */
	protected PersistenceManagerFactory newPersistenceManagerFactory(Properties prop) {
		return JDOHelper.getPersistenceManagerFactory(prop);
	}

	/**
	 * Return the singleton PersistenceManagerFactory.
	 */
	public Object getObject() {
		return this.persistenceManagerFactory;
	}

	public Class getObjectType() {
		return (this.persistenceManagerFactory != null) ?
		    this.persistenceManagerFactory.getClass() : PersistenceManagerFactory.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Close the PersistenceManagerFactory on context shutdown.
	 */
	public void destroy() {
		logger.info("Closing JDO PersistenceManagerFactory of LocalPersistenceManagerFactoryBean [" + this + "]");
		this.persistenceManagerFactory.close();
	}

}
