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

package org.springframework.orm.hibernate;

import java.sql.SQLException;

import net.sf.hibernate.JDBCException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Hibernate-specific subclass of UncategorizedDataAccessException,
 * for JDBC exceptions that Hibernate rethrew.
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see SessionFactoryUtils#convertHibernateAccessException
 * @see HibernateTemplate#convertHibernateAccessException
 */
public class HibernateJdbcException extends UncategorizedDataAccessException {

	public HibernateJdbcException(JDBCException ex) {
		super("JDBC exception on Hibernate data access: " + ex.getMessage(), ex.getSQLException());
	}

	public HibernateJdbcException(SQLException ex) {
		super("Exception on direct JDBC access: " + ex.getMessage(), ex);
	}

}
