package org.springframework.autobuilds.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.autobuilds.jpetstore.domain.Product;

public interface ProductDao {

  List getProductListByCategory(String categoryId) throws DataAccessException;

  List searchProductList(String keywords) throws DataAccessException;

	Product getProduct(String productId) throws DataAccessException;

}
