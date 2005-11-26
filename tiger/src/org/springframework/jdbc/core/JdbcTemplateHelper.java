package org.springframework.jdbc.core;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;

/**
 * Java 5 and above wrapper for the classic Spring JdbcTemplate, taking advantage of 
 * varargs and autoboxing, and exposing only 
 * the most commonly required operations to simplify JdbcTemplate
 * usage.
 * <b>
 * Use the getClassicJdbcTemplate() method if you need to invoke 
 * less commonly used methods. This includes any methods specifying SQL types,
 * methods using less commonly used callbacks such as RowCallbackHandler,
 * updates with PreparedStatementSetters, rather than arg lists, stored
 * procedures and batch operations.
 * 
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 1.3
 */
public class JdbcTemplateHelper implements SimpleJdbcOperations { 
	
	/**
	 * The JdbcTemplate we are wrapping.
	 */
	private JdbcOperations classicTemplate;
	
	public JdbcTemplateHelper(DataSource ds) {
		this.classicTemplate = new JdbcTemplate(ds);
	}
	
	public JdbcTemplateHelper(JdbcOperations jt) {
		this.classicTemplate = jt;
	}
	
	public void setDataSource(DataSource ds) {
		this.classicTemplate = new JdbcTemplate(ds);
	}

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * less commonly used methods. Can also be used to obtain
	 * the DataSource.
	 * @return JdbcTemplate wrapped by this class.
	 */
	public JdbcOperations getJdbcOperations() {
		return classicTemplate;
	}
	
	
	public int queryForInt(String sql, Object ... args) throws DataAccessException {
		return args == null || args.length == 0 ?
					classicTemplate.queryForInt(sql) :
					classicTemplate.queryForInt(sql, args);
	}
	
	public long queryForLong(String sql, Object ... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
					classicTemplate.queryForLong(sql) :
					classicTemplate.queryForLong(sql, args);
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(String sql, Class<T> requiredType, Object ... args) throws DataAccessException {
		return (T)((args == null || args.length == 0) ?
				classicTemplate.queryForObject(sql, requiredType) :
				classicTemplate.queryForObject(sql, args, requiredType));
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(String sql, ParameterizedRowMapper<T> rm, Object ... args) throws DataAccessException {
		return (T)((args == null || args.length == 0)?
				classicTemplate.queryForObject(sql, rm):
				classicTemplate.queryForObject(sql, args, rm));
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> query(String sql, ParameterizedRowMapper<T> rm, Object ... args) throws DataAccessException {
		return (List<T>)((args == null || args.length == 0) ?
				classicTemplate.query(sql, rm) :
				classicTemplate.query(sql, args, rm));
	}
	
	public int update(String sql, Object ... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
				classicTemplate.update(sql) :
				classicTemplate.update(sql, args);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> queryForMap(String sql, Object ... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
				classicTemplate.queryForMap(sql) :
				classicTemplate.queryForMap(sql, args);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForList(String sql, Object ... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
				classicTemplate.queryForList(sql) :
				classicTemplate.queryForList(sql, args);
	}
	
}
