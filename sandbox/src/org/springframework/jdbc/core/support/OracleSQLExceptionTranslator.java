/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.jdbc.core.support;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * Implementation of SQLExceptionTranslator that uses Oracle vendor code,
 * as included in the ORA-nnn methods. More precise than SQLState implementation,
 * but Oracle-specific. The JdbcTemplate class enables error handling to be
 * parameterized without making application's dependent on a particular RDBMS.
 *
 * <p>TODO: This class should handle a much wider range of Oracle error codes.
 * This can easily be done by adding additional values to the switch.
 *
 * @author Rod Johnson
 */
public class OracleSQLExceptionTranslator implements SQLExceptionTranslator {

	public DataAccessException translate(String task, String sql, SQLException sqlex) {
		switch (sqlex.getErrorCode()) {
			case 1 :
				// Unique constraint violated
				return new DataIntegrityViolationException(task + ": " + sqlex.getMessage(), sqlex);
			
			case 1400:
				// 	Can't insert null into non-nullable column
				return new DataIntegrityViolationException(task + ": " + sqlex.getMessage(), sqlex);
			case 936 : 
					// missing expression
					return new BadSqlGrammarException(task, sql, sqlex);
			case 942 :
					// table or view does not ex
			 return new BadSqlGrammarException(task, sql, sqlex);
		}

		// We couldn't identify it more precisely
		return new UncategorizedSQLException("(" + task +
			"): encountered SQLException [" + 
			sqlex.getMessage() + "]", sql, sqlex);
	}

}
