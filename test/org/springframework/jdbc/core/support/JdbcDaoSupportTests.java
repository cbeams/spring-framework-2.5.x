package org.springframework.jdbc.core.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * @author Juergen Hoeller
 * @since 30.07.2003
 */
public class JdbcDaoSupportTests extends TestCase {

	public void testJdbcDaoSupport() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.getMetaData();
		conControl.setReturnValue(null, 1);
		con.close();
		conControl.setVoidCallable(1);
		conControl.replay();
		dsControl.replay();
		final List test = new ArrayList();
		JdbcDaoSupport dao = new JdbcDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setDataSource(ds);
		dao.afterPropertiesSet();
		assertEquals("Correct SessionFactory", dao.getDataSource(), ds);
		assertEquals("Correct HibernateTemplate", dao.getJdbcTemplate().getDataSource(), ds);
		assertEquals("initDao called", test.size(), 1);
	}

}
