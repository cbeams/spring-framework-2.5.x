package org.springframework.samples.jpetstore.dao.ibatis;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;

public class MsSqlOrderDao extends SqlMapOrderDao {

  /**
   * Whacked out MS SQL Server hack to allow Item ID to be retrieved so
   * that we can use it to link the foreign key of the Line Items!
   */
  public void insertOrder(Order order) throws DataAccessException {
    Integer orderId = (Integer) getSqlMapTemplate().executeQueryForObject("msSqlServerInsertOrder", order);
    order.setOrderId(orderId.intValue());
    getSqlMapTemplate().executeUpdate("insertOrderStatus", order);
    for (int i = 0; i < order.getLineItems().size(); i++) {
      LineItem lineItem = (LineItem) order.getLineItems().get(i);
      lineItem.setOrderId(order.getOrderId());
      getSqlMapTemplate().executeUpdate("insertLineItem", lineItem);
    }
  }
  
}
