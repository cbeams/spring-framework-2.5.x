package org.springframework.autobuilds.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.autobuilds.jpetstore.domain.Category;

public interface CategoryDao {

	List getCategoryList() throws DataAccessException;

  Category getCategory(String categoryId) throws DataAccessException;

}
