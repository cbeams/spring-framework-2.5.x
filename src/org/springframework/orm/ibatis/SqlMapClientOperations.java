package org.springframework.orm.ibatis;

import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.event.RowHandler;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of iBATIS SqlMapClient operations.
 * Implemented by SqlMapClientTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides SqlMapClientTemplate's convenience methods that mirror SqlMapSession's
 * execution methods. See the SqlMapSession javadocs for details on those methods.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see SqlMapClientTemplate
 * @see com.ibatis.sqlmap.client.SqlMapClient
 */
public interface SqlMapClientOperations {

	Object queryForObject(String statementName, Object parameterObject) throws DataAccessException;

	Object queryForObject(String statementName, Object parameterObject,	Object resultObject)
			throws DataAccessException;

	List queryForList(String statementName, Object parameterObject) throws DataAccessException;

	List queryForList(String statementName, Object parameterObject, int skipResults,
										int maxResults) throws DataAccessException;

	List queryForList(String statementName, Object parameterObject, RowHandler rowHandler)
			throws DataAccessException;

	Map queryForMap(String statementName, Object parameterObject, String keyProperty)
			throws DataAccessException;

	Map queryForMap(String statementName, Object parameterObject, String keyProperty,
									String valueProperty) throws DataAccessException;

	Object insert(String statementName, Object parameterObject) throws DataAccessException;

	int update(String statementName, Object parameterObject) throws DataAccessException;

	int delete(String statementName, Object parameterObject) throws DataAccessException;

}
