package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapDaoSupport;
import org.springframework.samples.jpetstore.dao.AccountDao;
import org.springframework.samples.jpetstore.domain.Account;

/**
 * In this and other DAOs in this package, a DataSource property
 * is inherited from the SqlMapDaoSupport convenience superclass
 * supplied by Spring. DAOs don't need to extend such
 * superclasses, but it saves coding in many cases. There are
 * analogous superclasses for JDBC (JdbcDaoSupport), Hibernate
 * (HibernateDaoSupport), JDO (JdoDaoSupport) etc.
 * <p>
 * This and other DAOs are configured using Dependency Injection.
 * This means, for example, that Spring can source the DataSource
 * from a local class, such as the Commons DBCP BasicDataSource,
 * or from JNDI, concealing the JNDI lookup from application code.
 * 
 * @author Juergen Hoeller
 */
public class SqlMapAccountDao extends SqlMapDaoSupport implements AccountDao {

  public Account getAccount(String username) throws DataAccessException {
    return (Account) getSqlMapTemplate().executeQueryForObject("getAccountByUsername", username);
  }

  public Account getAccount(String username, String password) throws DataAccessException {
    Account account = new Account();
    account.setUsername(username);
    account.setPassword(password);
    return (Account) getSqlMapTemplate().executeQueryForObject("getAccountByUsernameAndPassword", account);
  }

  public void insertAccount(Account account) throws DataAccessException {
    getSqlMapTemplate().executeUpdate("insertAccount", account);
    getSqlMapTemplate().executeUpdate("insertProfile", account);
    getSqlMapTemplate().executeUpdate("insertSignon", account);
  }

  public void updateAccount(Account account) throws DataAccessException {
    getSqlMapTemplate().executeUpdate("updateAccount", account);
    getSqlMapTemplate().executeUpdate("updateProfile", account);
    if (account.getPassword() != null && account.getPassword().length() > 0) {
      getSqlMapTemplate().executeUpdate("updateSignon", account);
    }
  }

	public List getUsernameList() throws DataAccessException {
		return getSqlMapTemplate().executeQueryForList("getUsernameList", null);
	}

}
