/**
 * User: Clinton Begin
 * Date: Jul 13, 2003
 * Time: 8:18:50 PM
 */
package org.springframework.samples.jpetstore.dao;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.jpetstore.domain.Order;

public interface OrderDao {

  List getOrdersByUsername(String username) throws DataAccessException;

  Order getOrder(int orderId) throws DataAccessException;

  void insertOrder(Order order) throws DataAccessException;

}
