
package org.springframework.jdbc.core.support;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.TestBean;
import org.springframework.jdbc.core.MockConnectionFactory;

import com.mockobjects.sql.MockConnection;

/**
 * @author Rod Johnson
 */
public class JdbcBeanFactoryTests extends TestCase {

	/**
	 * Constructor for JdbcBeanFactoryTest.
	 * @param arg0
	 */
	public JdbcBeanFactoryTests(String arg0) {
		super(arg0);
	}
	
	public void testValid() throws Exception {
		String sql = "SELECT NAME AS NAME, PROPERTY AS PROPERTY, VALUE AS VALUE FROM T";
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();

		String[][] results = {
			{ "one", "class", "org.springframework.beans.TestBean" },
			{ "one", "age", "53" },
		};
	
		MockConnection con = MockConnectionFactory.statement(sql, results, true, null, null);
		con.setExpectedCloseCalls(2);

		ds.getConnection();
		// JdbcTemplate may ask for connection twice, for metadata
		dsControl.setReturnValue(con, MockControl.ONE_OR_MORE);
		dsControl.replay();
		
		JdbcBeanFactory bf = new JdbcBeanFactory(ds, sql);
		assertTrue(bf.getBeanDefinitionCount() == 1);
		TestBean tb = (TestBean) bf.getBean("one");
		assertTrue(tb.getAge() == 53);
	}
	

}
