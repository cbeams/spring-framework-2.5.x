package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapDaoSupport;
import org.springframework.samples.jpetstore.dao.CategoryDao;
import org.springframework.samples.jpetstore.domain.Category;

public class SqlMapCategoryDao extends SqlMapDaoSupport implements CategoryDao {

  public List getCategoryList() throws DataAccessException {
    return getSqlMapTemplate().executeQueryForList("getCategoryList", null);
  }

  public Category getCategory(String categoryId) throws DataAccessException {
    return (Category) getSqlMapTemplate().executeQueryForObject("getCategory", categoryId);
  }

}
