package org.springframework.jdbc.core.simple;

import org.springframework.jdbc.core.AbstractBeanPropertyRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Generic ParameterizedRowMapper implementation that converts a row into a new instance
 * of the specified mapped target class.  The mapped target class must be a top-level class
 * and it must have a default or no-arg constructor.
 *
 * Uses Java 5 covariant return types to override the return type of the {@link #mapRow} method
 * to be the type parameter <code>T</code>.
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
 * @author trisberg
 * @since 2.5
 * @see ParameterizedRowMapper
 */
public class ParameterizedBeanPropertyRowMapper<T> extends AbstractBeanPropertyRowMapper implements ParameterizedRowMapper<T> {

	/**
	 * Create a new ParameterizedBeanPropertyRowMapper.
	 * @see #setMappedClass
	 */
	public ParameterizedBeanPropertyRowMapper() {
	}


	/**
	 * Static factory method to create a new ParameterizedBeanPropertyRowMapper.
	 * @param mappedClass the class that each row should be mapped to.
	 */
	public static <T> ParameterizedBeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
		ParameterizedBeanPropertyRowMapper<T> newInstance = new ParameterizedBeanPropertyRowMapper<T>();
		newInstance.setMappedClass(mappedClass);
		return newInstance;
	}


 	/**
	 * Set the class that each row should be mapped to.
	 * @param mappedClass the mapped class
	 */
	public void setMappedClass(Class<T> mappedClass) {
		doSetMappedClass(mappedClass);
	}

	/**
	 * Extract the values for all columns in the current row.
	 * <p>Utilizes public setters and result set metadata.
	 * @see java.sql.ResultSetMetaData
	 */
	@SuppressWarnings("unchecked")
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		return (T)doMapRow(rs, rowNumber);
	}

}
