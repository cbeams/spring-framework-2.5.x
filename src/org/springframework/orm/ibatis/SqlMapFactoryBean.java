package org.springframework.orm.ibatis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ibatis.db.sqlmap.SqlMap;
import com.ibatis.db.sqlmap.XmlSqlMapBuilder;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassLoaderUtils;

/**
 * FactoryBean that creates an iBATIS Database Layer SqlMap as singleton in the
 * current bean factory, possibly for use with MappedStatementTemplate.
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see MappedStatementTemplate#setSqlMap
 */
public class SqlMapFactoryBean implements FactoryBean, InitializingBean {

	private String configLocation;

	private SqlMap sqlMap;

	/**
	 * Set the location of the iBATIS SqlMap config file as class path resource.
	 * A typical value is "example/sql-map-config.xml", in the case of web
	 * applications normally to be found in WEB-INF/classes.
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	public void afterPropertiesSet() throws IOException {
		if (this.configLocation == null) {
			throw new IllegalArgumentException("configLocation must be set");
		}
		InputStream is = ClassLoaderUtils.getResourceAsStream(this.configLocation);
		if (is == null) {
			throw new IOException("Class path resource [" + this.configLocation + "] not found");
		}
		this.sqlMap = buildSqlMap(is);
	}

	/**
	 * Build the SqlMap from the given config file.
	 * Uses XmlSqlMapBuilder by default.
	 * @param is InputStream for a config file
	 * @return the SqlMap instance
	 * @see com.ibatis.db.sqlmap.XmlSqlMapBuilder
	 */
	protected SqlMap buildSqlMap(InputStream is) {
		return XmlSqlMapBuilder.buildSqlMap(new InputStreamReader(is));
	}

	public Object getObject() {
		return this.sqlMap;
	}

	public Class getObjectType() {
		return (this.sqlMap != null ? this.sqlMap.getClass() : SqlMap.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
