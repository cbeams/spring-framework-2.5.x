package org.springframework.autobuilds.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.autobuilds.jpetstore.domain.Order;

public interface OrderDao {

  List getOrdersByUsername(String username) throws DataAccessException;

  Order getOrder(int orderId) throws DataAccessException;

  void insertOrder(Order order) throws DataAccessException;

}
