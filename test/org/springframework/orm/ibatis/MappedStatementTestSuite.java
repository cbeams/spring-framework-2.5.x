package org.springframework.orm.ibatis;

import java.io.IOException;
import java.io.InputStream;

import com.ibatis.db.sqlmap.SqlMap;
import junit.framework.TestCase;

/**
 * @author Juergen Hoeller
 * @since 28.11.2003
 */
public class MappedStatementTestSuite extends TestCase {

	public void testSqlMapFactoryBean() throws IOException {
		final SqlMap sqlMap = new SqlMap();
		SqlMapFactoryBean factory = new SqlMapFactoryBean() {
			protected SqlMap buildSqlMap(InputStream is) {
				assertNotNull(is);
				return sqlMap;
			}
		};
		factory.setConfigLocation("org/springframework/orm/ibatis/sql-map-config.xml");
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() == sqlMap);
	}

	public void testSqlMapFactoryBeanWithConfigNotFound() {
		SqlMapFactoryBean factory = new SqlMapFactoryBean();
		factory.setConfigLocation("example/sql-map-config.xml");
		try {
			factory.afterPropertiesSet();
			fail("Should have thrown IOException");
		}
		catch (IOException ex) {
			// expected
		}
	}

}
