package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapDaoSupport;
import org.springframework.samples.jpetstore.dao.AccountDao;
import org.springframework.samples.jpetstore.domain.Account;

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
