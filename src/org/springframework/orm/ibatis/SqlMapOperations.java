package org.springframework.orm.ibatis;

import java.util.List;
import java.util.Map;

import com.ibatis.db.sqlmap.RowHandler;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of iBATIS SqlMap operations.
 * Implemented by SqlMapTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides SqlMapTemplate's convenience methods that mirror MappedStatement's
 * executeXXX methods. See the MappedStatement javadocs for details on those methods.
 *
 * <p>NOTE: The SqlMap/MappedStatement API is the one to use with iBATIS SQL Maps 1.x.
 * The SqlMapClient/SqlMapSession API is only available with SQL Maps 2.
 *
 * @author Juergen Hoeller
 * @since 05.02.2004
 * @see SqlMapTemplate
 * @see com.ibatis.db.sqlmap.MappedStatement
 */
public interface SqlMapOperations {

	Object executeQueryForObject(String statementName, Object parameterObject)
			throws DataAccessException;

	Object executeQueryForObject(String statementName, Object parameterObject,
															 Object resultObject) throws DataAccessException;

	List executeQueryForList(String statementName, Object parameterObject)
			throws DataAccessException;

	List executeQueryForList(String statementName, Object parameterObject,
													 int skipResults, int maxResults)
			throws DataAccessException;

	Map executeQueryForMap(String statementName, Object parameterObject,
												 String keyProperty) throws DataAccessException;

	Map executeQueryForMap(String statementName, Object parameterObject,
												 String keyProperty, String valueProperty);

	void executeQueryWithRowHandler(String statementName, Object parameterObject,
																	RowHandler rowHandler) throws DataAccessException;

	int executeUpdate(String statementName, Object parameterObject)
			throws DataAccessException;

}
