package org.springframework.autobuilds.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapDaoSupport;
import org.springframework.autobuilds.jpetstore.dao.CategoryDao;
import org.springframework.autobuilds.jpetstore.domain.Category;

public class SqlMapCategoryDao extends SqlMapDaoSupport implements CategoryDao {

  public List getCategoryList() throws DataAccessException {
    return getSqlMapTemplate().executeQueryForList("getCategoryList", null);
  }

  public Category getCategory(String categoryId) throws DataAccessException {
    return (Category) getSqlMapTemplate().executeQueryForObject("getCategory", categoryId);
  }

}
