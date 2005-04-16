/*
 * Copyright 2002-2005 the original author or authors.
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

import com.ibatis.db.sqlmap.MappedStatement;
import com.ibatis.db.sqlmap.SqlMap;
import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.ibatis.support.SqlMapDaoSupport;

/**
 * @author Juergen Hoeller
 * @since 28.11.2003
 */
public class SqlMapTests extends TestCase {

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
	
	public void testSqlMapTemplate() throws SQLException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.close();
		conControl.setVoidCallable(1);
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
	
	public void testSqlMapDaoSupport() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		SqlMapDaoSupport testDao = new SqlMapDaoSupport() {
		};
		testDao.setDataSource(ds);
		assertEquals(ds, testDao.getDataSource());

		SqlMap map = new SqlMap();
		testDao.setSqlMap(map);
		assertEquals(map, testDao.getSqlMap());

		SqlMapTemplate template = new SqlMapTemplate();
		template.setDataSource(ds);
		template.setSqlMap(map);
		testDao.setSqlMapTemplate(template);
		assertEquals(template, testDao.getSqlMapTemplate());

		testDao.afterPropertiesSet();
	}

}
