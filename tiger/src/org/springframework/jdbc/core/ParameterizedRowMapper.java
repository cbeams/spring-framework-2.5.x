package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Extension of the {@link RowMapper} interface adding type parameterization. Use
 * Java 5 covariant return types to override the return type of the
 * {@link #mapRow} method to be the type parameter <code>T</code>.
 *
 * @author Rob Harrop
 * @see SimpleJdbcOperations#query
 * @see SimpleJdbcOperations#queryForObject(String, ParameterizedRowMapper, java.lang.Object...)
 * @see #mapRow(java.sql.ResultSet, int)
 * @param <T> the type returned by this mapper
 */
public interface ParameterizedRowMapper<T> extends RowMapper {

	/**
	 * Implementations should return the object representation of
	 * the current row in the supplied {@link ResultSet}.
	 */
	T mapRow(ResultSet rs, int rowNum) throws SQLException;
}
