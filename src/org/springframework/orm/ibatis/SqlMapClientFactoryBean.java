package org.springframework.orm.ibatis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean that creates an iBATIS Database Layer SqlMapClient as singleton
 * in the current bean factory, possibly for use with SqlMapClientTemplate.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see SqlMapClientTemplate#setSqlMapClient
 */
public class SqlMapClientFactoryBean implements FactoryBean, InitializingBean {

	private Resource configLocation;

	private Properties sqlMapClientProperties;

	private SqlMapClient sqlMapClient;

	/**
	 * Set the location of the iBATIS SqlMapClient config file as class path resource.
	 * A typical value is "WEB-INF/sql-map-config.xml".
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set optional properties to be passed into the SqlMapClientBuilder.
	 * @see com.ibatis.sqlmap.client.SqlMapClientBuilder#buildSqlMapClient(java.io.Reader, java.util.Properties)
	 */
	public void setSqlMapClientProperties(Properties sqlMapClientProperties) {
		this.sqlMapClientProperties = sqlMapClientProperties;
	}

	public void afterPropertiesSet() throws IOException {
		if (this.configLocation == null) {
			throw new IllegalArgumentException("configLocation must be set");
		}
		InputStream is = this.configLocation.getInputStream();
		this.sqlMapClient = (this.sqlMapClientProperties != null) ?
				SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is), this.sqlMapClientProperties) :
				SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is));
	}

	public Object getObject() {
		return this.sqlMapClient;
	}

	public Class getObjectType() {
		return (this.sqlMapClient != null ? this.sqlMapClient.getClass() : SqlMapClient.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
