/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generic RowMapper implementation that converts a row into a new instance
 * of the specified mapped target class.  The mapped target class must be a top-level class
 * and it must have a default or no-arg constructor.
 *
 * Column values are mapped based on matching the column name as obtained from result set
 * metadata to public setters for the corresponding properties.  The names are matched either
 * directly or by transforming a name separating the parts with underscores to the same name
 * using "camel" case.
 *
 * Mapping is provided for fields in the target class that are defined as any of the
 * following types: String, byte, Byte, short, Short, int, Integer, long, Long, float, Float,
 * double, Double, BigDecimal, boolean, Boolean and java.util.Date.
 *
 * To facilitate mapping between columns and fields that don't have matching names, try using column 
 * aliases in the SQL statement like "select fname as first_name from customer".
 *
 * Please note that this class is designed to provide convenience rather than high performance.
 * For best performance consider using a custom RowMapper.
 *
 * @author Thomas Risberg
 * @since 2.5
 * @see RowMapper
 */
public class BeanPropertyRowMapper extends AbstractBeanPropertyRowMapper implements RowMapper {


	/**
	 * Create a new BeanPropertyRowMapper.
	 * @see #setMappedClass
	 */
	public BeanPropertyRowMapper() {
	}

	/**
	 * Create a new BeanPropertyRowMapper.
	 * @param mappedClass the class that each row should be mapped to.
	 */
	public BeanPropertyRowMapper(Class mappedClass) {
		initialize(mappedClass);
	}


	/**
	 * Static factory method to create a new ParameterizedBeanPropertyRowMapper.
	 * @param mappedClass the class that each row should be mapped to.
	 */
	public static BeanPropertyRowMapper newInstance(Class mappedClass) {
		BeanPropertyRowMapper newInstance = new BeanPropertyRowMapper();
		newInstance.setMappedClass(mappedClass);
		return newInstance;
	}


	/**
	 * Set the class that each row should be mapped to.
	 * @param mappedClass the mapped class
	 */
	public void setMappedClass(Class mappedClass) {
		doSetMappedClass(mappedClass);
	}

	/**
	 * Extract the values for all columns in the current row.
	 * <p>Utilizes public setters and result set metadata.
	 * @see java.sql.ResultSetMetaData
	 */
	public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return doMapRow(rs, rowNumber);
	}

}
