/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.orm.ibatis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.client.SqlMapSession;
import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

/**
 * @author Juergen Hoeller
 * @since 09.10.2004
 */
public class SqlMapClientTests extends TestCase {

	public void testSqlMapClientFactoryBeanWithoutConfig() {
		SqlMapClientFactoryBean factory = new SqlMapClientFactoryBean();
		// explicitly set to null, don't know why ;-)
		factory.setConfigLocation(null);
		try {
			factory.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		} catch (IOException e) {
			fail("Should have thrown IllegalArgumentException");
		}
	}

	public void testSqlMapClientTemplate() throws SQLException {
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

		MockControl smsControl = MockControl.createControl(SqlMapSession.class);
		final SqlMapSession sms = (SqlMapSession) smsControl.getMock();
		MockControl smcControl = MockControl.createControl(SqlMapClient.class);
		SqlMapClient smc = (SqlMapClient) smcControl.getMock();
		smc.openSession();
		smcControl.setReturnValue(sms);
		sms.setUserConnection(con);
		smsControl.setVoidCallable();
		sms.close();
		smsControl.setVoidCallable();
		smsControl.replay();
		smcControl.replay();

		SqlMapClientTemplate template = new SqlMapClientTemplate();
		template.setDataSource(ds);
		template.setSqlMapClient(smc);
		template.afterPropertiesSet();
		Object result = template.execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {
				assertTrue(executor == sms);
				return "done";
			}
		});
		assertEquals("done", result);
		dsControl.verify();
		conControl.verify();
		smsControl.verify();
		smcControl.verify();
	}

	public void testSqlMapClientDaoSupport() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		TestSqlMapClientDaoSupport testDao = new TestSqlMapClientDaoSupport();
		testDao.setDataSource(ds);
		assertEquals(ds, testDao.getDSource());

		MockControl smcControl = MockControl.createControl(SqlMapClient.class);
		SqlMapClient smc = (SqlMapClient) smcControl.getMock();
		smcControl.replay();

		testDao.setSqlMapClient(smc);
		assertEquals(smc, testDao.getSMap());

		SqlMapClientTemplate template = new SqlMapClientTemplate();
		template.setDataSource(ds);
		template.setSqlMapClient(smc);
		testDao.setSqlMapClientTemplate(template);
		assertEquals(template, testDao.getSMTemplate());

		testDao.afterPropertiesSet();
	}


	private class TestSqlMapClientDaoSupport extends SqlMapClientDaoSupport{

		public DataSource getDSource(){
			return super.getDataSource();
		}

		public SqlMapClient getSMap() {
			return super.getSqlMapClient();
		}

		public SqlMapClientTemplate getSMTemplate(){
			return super.getSqlMapClientTemplate();
		}
	}

}
