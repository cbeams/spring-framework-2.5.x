package org.springframework.orm.ibatis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibatis.db.sqlmap.MappedStatement;
import com.ibatis.db.sqlmap.SqlMap;
import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Juergen Hoeller
 * @since 28.11.2003
 */
public class SqlMapTestSuite extends TestCase {

	public void testSqlMapFactoryBean() throws IOException {
		SqlMapFactoryBean factory = new SqlMapFactoryBean();
		factory.setConfigLocation(new ClassPathResource("sql-map-config.xml", getClass()));
		factory.afterPropertiesSet();
		assertTrue(factory.getObject() instanceof SqlMap);
		assertEquals(201, ((SqlMap) factory.getObject()).getStatementCacheSize());
	}

	public void testSqlMapFactoryBeanWithConfigNotFound() {
		SqlMapFactoryBean factory = new SqlMapFactoryBean();
		factory.setConfigLocation(new ClassPathResource("example/sql-map-config.xml"));
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

		SqlMapTemplate template = new SqlMapTemplate();
		template.setDataSource(ds);
		template.setSqlMap(map);
		template.afterPropertiesSet();
		Object result = template.execute("stmt", new SqlMapCallback() {
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
