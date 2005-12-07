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

package org.springframework.jdbc.core.simple;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Extension of JdbcDaoSupport to expose a SimpleJdbcTemplate.
 * Only usable with Java 5 and above.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleJdbcTemplate
 */
public class SimpleJdbcDaoSupport extends JdbcDaoSupport {
	
	private SimpleJdbcTemplate simpleJdbcTemplate;


	/**
	 * Overridden to not only create a JdbcTemplate but also a SimpleJdbcTemplate.
	 */
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(jt);
		return jt;
	}
	
	/**
	 * Return a SimpleJdbcTemplate wrapping the current JdbcTemplate.
	 */
	public SimpleJdbcTemplate getSimpleJdbcTemplate() {
	  return simpleJdbcTemplate;
	}

}
