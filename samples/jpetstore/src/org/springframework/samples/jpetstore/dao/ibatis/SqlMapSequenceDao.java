package org.springframework.samples.jpetstore.dao.ibatis;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

public class SqlMapSequenceDao extends SqlMapClientDaoSupport {

  /**
   * This is a generic sequence ID generator that is based on a database
   * table called 'SEQUENCE', which contains two columns (NAME, NEXTID).
   * This approach should work with any database.
   * @param name the name of the sequence
   * @return the next ID
   */
  public int getNextId(String name) throws DataAccessException {
    Sequence sequence = new Sequence(name, -1);
    sequence = (Sequence) getSqlMapClientTemplate().queryForObject("getSequence", sequence);
    if (sequence == null) {
      throw new DataRetrievalFailureException("Error: A null sequence was returned from the database (could not get next " +
      			name + " sequence).");
    }
    Object parameterObject = new Sequence(name, sequence.getNextId() + 1);
    getSqlMapClientTemplate().update("updateSequence", parameterObject);
    return sequence.getNextId();
  }
}
