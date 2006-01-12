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

package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessResourceFailureException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * RowMapper implementation that converts a row into a new instance
 * of the specified mapped class.  Column values are mapped based on matching
 * the column name as obtained from result set metadata to public setters for
 * the corresponding properties.  The names are matched either directly or by transforming
 * a name separating the parts with underscores to the same name using "camel" case.
 *
 * @author Thomas Risberg
 * @since 2.0
 */
public class ActiveRowMapper implements RowMapper {

	private Class mappedClass;

	private Constructor defaultConstruct;

	private Map mappedFields;


	/**
	 * Create a new ActiveRowMapper.
	 * @see #setMappedClass
	 */
	public ActiveRowMapper() {
	}

	/**
	 * Create a new ActiveRowMapper.
	 * @param mappedClass the class that each row should be mapped to.
	 */
	public ActiveRowMapper(Class mappedClass) {
		setMappedClass(mappedClass);
	}

	/**
	 * Set the class that each row should be mapped to.
	 */
	public void setMappedClass(Class mappedClass) {
		this.mappedClass = mappedClass;
		try {
			defaultConstruct = mappedClass.getConstructor(null);
		} catch (NoSuchMethodException e) {
			throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to access default constructor of ").append(mappedClass.getName()).toString(), e);
		}
		mappedFields = new HashMap();
		Field[] f = mappedClass.getDeclaredFields();
		for (int i = 0; i < f.length; i++) {
			PersistentField pf = new PersistentField();
			pf.setFieldName(f[i].getName());
			pf.setColumnName(underscoreName(f[i].getName()));
			pf.setJavaType(f[i].getType());
			mappedFields.put(pf.getColumnName(), pf);
		}
	}


	/**
	 * Extract the values for all columns in the current row.
	 * <p>Utilizes public setters and result set metadata.
	 * @see java.sql.ResultSetMetaData
	 */

	public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Object result;
		try {
			result = defaultConstruct.newInstance(null);
		} catch (IllegalAccessException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + mappedClass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + mappedClass.getName(), e);
		} catch (InstantiationException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + mappedClass.getName(), e);
		}
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			String field = meta.getColumnName(i).toLowerCase();
			PersistentField fieldMeta = (PersistentField)mappedFields.get(field);
			if (fieldMeta != null) {
				Object value = null;
				Method m = null;
				try {
					if (fieldMeta.getJavaType().equals(String.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {String.class});
						value = rs.getString(i);
					}
					else if (fieldMeta.getJavaType().equals(Byte.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Byte.class});
						value = new Byte(rs.getByte(i));
					}
					else if (fieldMeta.getJavaType().equals(Short.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Short.class});
						value = new Short(rs.getShort(i));
					}
					else if (fieldMeta.getJavaType().equals(Integer.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Integer.class});
						value = new Integer(rs.getInt(i));
					}
					else if (fieldMeta.getJavaType().equals(Long.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Long.class});
						value = new Long(rs.getLong(i));
					}
					else if (fieldMeta.getJavaType().equals(Float.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Float.class});
						value = new Float(rs.getFloat(i));
					}
					else if (fieldMeta.getJavaType().equals(Double.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Double.class});
						value = new Double(rs.getDouble(i));
					}
					else if (fieldMeta.getJavaType().equals(BigDecimal.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {BigDecimal.class});
						value = rs.getBigDecimal(i);
					}
					else if (fieldMeta.getJavaType().equals(Boolean.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Boolean.class});
						value = (rs.getBoolean(i)) ? Boolean.TRUE : Boolean.FALSE;
					}
					else if (fieldMeta.getJavaType().equals(Date.class)) {
						m = result.getClass().getMethod(setterName(fieldMeta.getColumnName()), new Class[] {Date.class});
						if (fieldMeta.getSqlType() == Types.DATE) {
							value = rs.getDate(i);
						}
						else if (fieldMeta.getSqlType() == Types.TIME) {
							value = rs.getTime(i);
						}
						else {
							value = rs.getTimestamp(i);
						}
					}
					if (m != null) {
						m.invoke(result , new Object[] {value});
					}
				} catch (NoSuchMethodException e) {
					throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to map field ").append(fieldMeta.getFieldName()).append(".").toString(), e);
				} catch (IllegalAccessException e) {
					throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to map field ").append(fieldMeta.getFieldName()).append(".").toString(), e);
				} catch (InvocationTargetException e) {
					throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to map field ").append(fieldMeta.getFieldName()).append(".").toString(), e);
				}
			}
		}
		return result;
	}

	public static String underscoreName(String name) {
        // This is a 1.4 and later method - do we need to support 1.3?
        return name.substring(0,1).toLowerCase() + name.substring(1).replaceAll("([A-Z])", "_$1").toLowerCase();
	}

	private String setterName(String columnName) {
		StringTokenizer tokenizer = new StringTokenizer(columnName, "_");
		StringBuffer propertyName = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			propertyName.append(token.substring(0, 1).toUpperCase());
			propertyName.append(token.substring(1));
		}
		return "set" + propertyName.toString();
	}

	private class PersistentField {

		private String fieldName;
		private String columnName;
		private Class javaType;
		private int sqlType;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public Class getJavaType() {
			return javaType;
		}

		public void setJavaType(Class javaType) {
			this.javaType = javaType;
		}

		public int getSqlType() {
			return sqlType;
		}

		public void setSqlType(int sqlType) {
			this.sqlType = sqlType;
		}

	}

}
