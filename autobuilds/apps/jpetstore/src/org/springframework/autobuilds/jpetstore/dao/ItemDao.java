package org.springframework.autobuilds.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.autobuilds.jpetstore.domain.Item;
import org.springframework.autobuilds.jpetstore.domain.Order;

public interface ItemDao {

  public void updateQuantity(Order order) throws DataAccessException;

  boolean isItemInStock(String itemId) throws DataAccessException;

  List getItemListByProduct(String productId) throws DataAccessException;

  Item getItem(String itemId) throws DataAccessException;

}
