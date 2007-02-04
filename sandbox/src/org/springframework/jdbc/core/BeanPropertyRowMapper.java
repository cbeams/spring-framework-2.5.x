/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * RowMapper implementation that converts a row into a new instance
 * of the specified mapped class.  Column values are mapped based on matching
 * the column name as obtained from result set metadata to public setters for
 * the corresponding properties.  The names are matched either directly or by transforming
 * a name separating the parts with underscores to the same name using "camel" case.
 *
 * @author Thomas Risberg
 * @since 2.1
 */
public class BeanPropertyRowMapper implements RowMapper {

	private Class mappedClass;

	private Constructor defaultConstruct;

	private Map mappedFields;


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
		setMappedClass(mappedClass);
	}

	/**
	 * Set the class that each row should be mapped to.
	 */
	public void setMappedClass(Class mappedClass) {
		this.mappedClass = mappedClass;
		try {
			this.defaultConstruct = mappedClass.getConstructor((Class[]) null);
		}
		catch (NoSuchMethodException ex) {
			throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to access default constructor of ").append(mappedClass.getName()).toString(), ex);
		}
		this.mappedFields = new HashMap();
		Field[] f = mappedClass.getDeclaredFields();
		for (int i = 0; i < f.length; i++) {
			PersistentField pf = new PersistentField();
			pf.setFieldName(f[i].getName());
			pf.setSetterName(setterName(f[i].getName()));
			pf.setJavaType(f[i].getType());
			this.mappedFields.put(f[i].getName().toLowerCase(), pf);
			String underscoredName = underscoreName(f[i].getName());
			if (!f[i].getName().toLowerCase().equals(underscoredName)) {
				this.mappedFields.put(underscoredName, pf);
			}
			}
	}


	/**
	 * Extract the values for all columns in the current row.
	 * <p>Utilizes public setters and result set metadata.
	 * @see java.sql.ResultSetMetaData
	 */
	public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
		if (this.mappedClass == null)
			throw new InvalidDataAccessApiUsageException("Target class was not specified - this must be specified");
		Object result;
		try {
			result = this.defaultConstruct.newInstance((Object[]) null);
		}
		catch (IllegalAccessException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + this.mappedClass.getName(), e);
		}
		catch (InvocationTargetException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + this.mappedClass.getName(), e);
		}
		catch (InstantiationException e) {
			throw new DataAccessResourceFailureException("Failed to load class " + this.mappedClass.getName(), e);
		}
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			String field = meta.getColumnName(i).toLowerCase();
			PersistentField fieldMeta = (PersistentField) this.mappedFields.get(field);
			if (fieldMeta != null) {
				try {
					Object value = null;
					Class fieldType = fieldMeta.getJavaType();
					Method m = result.getClass().getMethod(fieldMeta.getSetterName(), new Class[]{fieldType});
					if (fieldType.equals(String.class)) {
						value = rs.getString(field);
					}
					else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
						value = new Byte(rs.getByte(field));
					}
					else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
						value = new Short(rs.getShort(field));
					}
					else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
						value = new Integer(rs.getInt(field));
					}
					else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
						value = new Long(rs.getLong(field));
					}
					else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
						value = new Float(rs.getFloat(field));
					}
					else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
						value = new Double(rs.getDouble(field));
					}
					else if (fieldType.equals(BigDecimal.class)) {
						value = rs.getBigDecimal(field);
					}
					else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
						value = (rs.getBoolean(field)) ? Boolean.TRUE : Boolean.FALSE;
					}
					else if (fieldType.equals(Date.class)) {
						if (fieldMeta.getSqlType() == Types.DATE) {
							value = rs.getDate(field);
						}
						else if (fieldMeta.getSqlType() == Types.TIME) {
							value = rs.getTime(field);
						}
						else {
							value = rs.getTimestamp(field);
						}
					}
					if (m != null) {
						m.invoke(result, new Object[]{value});
					}
				}
				catch (NoSuchMethodException e) {
					throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to map field ").append(fieldMeta.getFieldName()).append(".").toString(), e);
				}
				catch (IllegalAccessException e) {
					throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to map field ").append(fieldMeta.getFieldName()).append(".").toString(), e);
				}
				catch (InvocationTargetException e) {
					throw new DataAccessResourceFailureException(new StringBuffer().append("Failed to map field ").append(fieldMeta.getFieldName()).append(".").toString(), e);
				}
			}
		}
		return result;
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case.  Any upper case letters are
	 * converted to lower case with a preceding undercore.
	 * @param name The string containing original name
	 * @return The name converted
	 */
	public static String underscoreName(String name) {
		StringBuffer result = new StringBuffer();
		if (name != null && name.length() > 0) {
			result.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				if (s.equals(s.toUpperCase())) {
					result.append("_");
					result.append(s.toLowerCase());
				}
				else {
					result.append(s);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Convert a field name to a setter name with the correct case.
	 * @param fieldName The string containing original name
	 * @return The name converted
	 */
	private String setterName(String fieldName) {
		StringBuffer setterName = new StringBuffer();
		setterName.append("set");
		if (fieldName != null && fieldName.length() > 0) {
			setterName.append(fieldName.substring(0, 1).toUpperCase());
			if (fieldName.length() > 1) {
				setterName.append(fieldName.substring(1));
			}
		}
		return setterName.toString();
	}


	private class PersistentField {

		private String fieldName;

		private String setterName;

		private Class javaType;

		private int sqlType;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getSetterName() {
			return setterName;
		}

		public void setSetterName(String setterName) {
			this.setterName = setterName;
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
