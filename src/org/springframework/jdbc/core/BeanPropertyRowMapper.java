/*
 * Copyright 2002-2008 the original author or authors.
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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link} RowMapper implementation that converts a row into a new instance
 * of the specified mapped target class. The mapped target class must be a
 * top-level class and it must have a default or no-arg constructor.
 *
 * <p>Column values are mapped based on matching the column name as obtained from result set
 * metadata to public setters for the corresponding properties.  The names are matched either
 * directly or by transforming a name separating the parts with underscores to the same name
 * using "camel" case.
 *
 * <p>Mapping is provided for fields in the target class that are defined as any of the
 * following types: String, byte, Byte, short, Short, int, Integer, long, Long, float, Float,
 * double, Double, BigDecimal, boolean, Boolean and java.util.Date.
 *
 * <p>To facilitate mapping between columns and fields that don't have matching names,
 * try using column aliases in the SQL statement like "select fname as first_name from customer".
 *
 * <p>Please note that this class is designed to provide convenience rather than high performance.
 * For best performance consider using a custom RowMapper.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 * @see RowMapper
 */
public class BeanPropertyRowMapper implements RowMapper {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** The class we are mapping to */
	protected Class mappedClass;

	/** Map of the fields we provide mapping for */
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
		initialize(mappedClass);
	}


	/**
	 * Set the class that each row should be mapped to.
	 */
	public void setMappedClass(Class mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		}
		else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
						mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}
	}

	/**
	 * Initialize the mapping metadata for the given class.
	 * @param mappedClass the mapped class.
	 */
	protected void initialize(Class mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new HashMap();
		Class metaDataClass = mappedClass;
		while (metaDataClass != null) {
			Field[] f = metaDataClass.getDeclaredFields();
			for (int i = 0; i < f.length; i++) {
				PersistentField pf = new PersistentField();
				pf.setFieldName(f[i].getName());
				pf.setJavaType(f[i].getType());
				this.mappedFields.put(f[i].getName().toLowerCase(), pf);
				String underscoredName = underscoreName(f[i].getName());
				if (!f[i].getName().toLowerCase().equals(underscoredName)) {
					this.mappedFields.put(underscoredName, pf);
				}
			}
			// try any superclass
			metaDataClass = metaDataClass.getSuperclass();
		}
	}

	/**
	 * Convert a name in camelCase to an underscored name in lower case.
	 * Any upper case letters are converted to lower case with a preceding underscore.
	 * @param name the string containing original name
	 * @return the converted name
	 */
	private String underscoreName(String name) {
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
	 * Get the class that we are mapping to.
	 */
	public final Class getMappedClass() {
		return this.mappedClass;
	}


	/**
	 * Extract the values for all columns in the current row.
	 * <p>Utilizes public setters and result set metadata.
	 * @see java.sql.ResultSetMetaData
	 */
	public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		Object result = BeanUtils.instantiateClass(this.mappedClass);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			String column = JdbcUtils.lookupColumnName(rsmd, i).toLowerCase();
			PersistentField fieldMeta = (PersistentField) this.mappedFields.get(column);
			if (fieldMeta != null) {
				BeanWrapper bw = new BeanWrapperImpl(result);
				bw.setWrappedInstance(result);
				fieldMeta.setSqlType(rsmd.getColumnType(i));
				Object value = null;
				Class fieldType = fieldMeta.getJavaType();
				if (fieldType.equals(String.class)) {
					value = rs.getString(column);
				}
				else if (fieldType.equals(byte.class) || fieldType.equals(Byte.class)) {
					value = new Byte(rs.getByte(column));
				}
				else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
					value = new Short(rs.getShort(column));
				}
				else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
					value = new Integer(rs.getInt(column));
				}
				else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
					value = new Long(rs.getLong(column));
				}
				else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
					value = new Float(rs.getFloat(column));
				}
				else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
					value = new Double(rs.getDouble(column));
				}
				else if (fieldType.equals(BigDecimal.class)) {
					value = rs.getBigDecimal(column);
				}
				else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
					value = (rs.getBoolean(column)) ? Boolean.TRUE : Boolean.FALSE;
				}
				else if (fieldType.equals(java.util.Date.class) ||
						fieldType.equals(java.sql.Timestamp.class) ||
						fieldType.equals(java.sql.Time.class) ||
						fieldType.equals(Number.class)) {
					value = JdbcUtils.getResultSetValue(rs, rs.findColumn(column));
				}
				if (value != null) {
					if (bw.isWritableProperty(fieldMeta.getFieldName())) {
						try {
							if (logger.isDebugEnabled() && rowNumber == 0) {
								logger.debug("Mapping column named \"" + column + "\"" +
										" containing values of SQL type " + fieldMeta.getSqlType() +
										" to property \"" + fieldMeta.getFieldName() + "\"" +
										" of type " + fieldMeta.getJavaType());
							}
							bw.setPropertyValue(fieldMeta.getFieldName(), value);
						}
						catch (NotWritablePropertyException ex) {
							throw new DataRetrievalFailureException(
									"Unable to map column " + column + " to property " + fieldMeta.getFieldName(), ex);
						}
					}
					else {
						if (rowNumber == 0) {
							logger.warn("Unable to access the setter for " + fieldMeta.getFieldName() +
									".  Check that " + "set" + StringUtils.capitalize(fieldMeta.getFieldName()) +
									" is declared and has public access.");
						}
					}
				}
			}
		}
		return result;
	}


	/**
	 * Static factory method to create a new BeanPropertyRowMapper.
	 * @param mappedClass the class that each row should be mapped to
	 * @deprecated as of Spring 2.5.2; use the standard constructor instead
	 * @see #BeanPropertyRowMapper(Class)
	 */
	public static BeanPropertyRowMapper newInstance(Class mappedClass) {
		return new BeanPropertyRowMapper(mappedClass);
	}


	/**
	 * A PersistentField represents the info we are interested in knowing about
	 * the fields and their relationship to the columns of the database table.
	 */
	private static class PersistentField {

		private String fieldName;

		private Class javaType;

		private int sqlType;

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public String getFieldName() {
			return this.fieldName;
		}

		public void setJavaType(Class javaType) {
			this.javaType = javaType;
		}

		public Class getJavaType() {
			return this.javaType;
		}

		public void setSqlType(int sqlType) {
			this.sqlType = sqlType;
		}

		public int getSqlType() {
			return this.sqlType;
		}
	}

}
