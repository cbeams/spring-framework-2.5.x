package org.springframework.orm.ibatis;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibatis.db.sqlmap.MappedStatement;
import com.ibatis.db.sqlmap.SqlMap;
import junit.framework.TestCase;
import org.easymock.MockControl;

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

	public void testMappedStatementTemplate() throws SQLException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 2);
		con.getMetaData();
		conControl.setReturnValue(null, 1);
		con.close();
		conControl.setVoidCallable(2);
		dsControl.replay();
		conControl.replay();

		final MappedStatement stmt = new MappedStatement();
		SqlMap map = new SqlMap() {
			public MappedStatement getMappedStatement(String name) {
				if ("stmt".equals(name)) {
					return stmt;
				}
				return null;
			}
		};

		MappedStatementTemplate template = new MappedStatementTemplate();
		template.setDataSource(ds);
		template.setSqlMap(map);
		template.afterPropertiesSet();
		Object result = template.execute("stmt", new MappedStatementCallback() {
			public Object doInMappedStatement(MappedStatement s, Connection c) {
				assertTrue(stmt == s);
				assertTrue(con == c);
				return "done";
			}
		});
		assertEquals("done", result);
		dsControl.verify();
		conControl.verify();
	}

}
