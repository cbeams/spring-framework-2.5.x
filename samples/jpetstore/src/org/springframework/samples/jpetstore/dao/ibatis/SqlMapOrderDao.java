package org.springframework.samples.jpetstore.dao.ibatis;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ibatis.support.SqlMapDaoSupport;
import org.springframework.samples.jpetstore.dao.OrderDao;
import org.springframework.samples.jpetstore.domain.LineItem;
import org.springframework.samples.jpetstore.domain.Order;

public class SqlMapOrderDao extends SqlMapDaoSupport implements OrderDao {

  private SqlMapSequenceDao sequenceDao;

	public void setSequenceDao(SqlMapSequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	public List getOrdersByUsername(String username) throws DataAccessException {
    return getSqlMapTemplate().executeQueryForList("getOrdersByUsername", username);
  }

  public Order getOrder(int orderId) throws DataAccessException {
    Object parameterObject = new Integer(orderId);
    Order order = (Order) getSqlMapTemplate().executeQueryForObject("getOrder", parameterObject);
		if (order != null) {
    	order.setLineItems(getSqlMapTemplate().executeQueryForList("getLineItemsByOrderId", new Integer(order.getOrderId())));
		}
    return order;
  }

  public void insertOrder(Order order) throws DataAccessException {
		order.setOrderId(this.sequenceDao.getNextId("ordernum"));
		getSqlMapTemplate().executeUpdate("insertOrder", order);
		getSqlMapTemplate().executeUpdate("insertOrderStatus", order);
    for (int i = 0; i < order.getLineItems().size(); i++) {
      LineItem lineItem = (LineItem) order.getLineItems().get(i);
      lineItem.setOrderId(order.getOrderId());
      getSqlMapTemplate().executeUpdate("insertLineItem", lineItem);
    }
  }

}
