package org.springframework.samples.jpetstore.domain.logic;

import java.util.List;

import org.springframework.samples.jpetstore.dao.AccountDao;
import org.springframework.samples.jpetstore.dao.CategoryDao;
import org.springframework.samples.jpetstore.dao.ItemDao;
import org.springframework.samples.jpetstore.dao.OrderDao;
import org.springframework.samples.jpetstore.dao.ProductDao;
import org.springframework.samples.jpetstore.domain.Account;
import org.springframework.samples.jpetstore.domain.Category;
import org.springframework.samples.jpetstore.domain.Item;
import org.springframework.samples.jpetstore.domain.Order;
import org.springframework.samples.jpetstore.domain.Product;

/**
 * JPetStore primary business object.
 *
 * <p>Defines a default transaction attribute for all methods.
 * Note that this attribute definition is only necessary if using Commons
 * Attributes autoproxying (see the attributes directory under the root of
 * JPetStore). No attributes are required with a TransactionFactoryProxyBean,
 * as in the default applicationContext.xml in the war/WEB-INF directory.
 *
 * <p>The following attribute definition uses Commons Attributes attribute syntax.
 * @@org.springframework.transaction.interceptor.DefaultTransactionAttribute()
 *
 * @author Juergen Hoeller
 * @since 30.11.2003
 */
public class PetStoreImpl implements PetStoreFacade, OrderService {

  private AccountDao accountDao;

	private CategoryDao categoryDao;

	private ProductDao productDao;

	private ItemDao itemDao;

	private OrderDao orderDao;


	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}

	public void setItemDao(ItemDao itemDao) {
		this.itemDao = itemDao;
	}

	public void setOrderDao(OrderDao orderDao) {
		this.orderDao = orderDao;
	}


	public Account getAccount(String username) {
    return this.accountDao.getAccount(username);
  }

  public Account getAccount(String username, String password) {
		return this.accountDao.getAccount(username, password);
  }

  public void insertAccount(Account account) {
		this.accountDao.insertAccount(account);
  }

  public void updateAccount(Account account) {
		this.accountDao.updateAccount(account);
  }

  public List getUsernameList() {
		return this.accountDao.getUsernameList();
  }


  public List getCategoryList() {
    return this.categoryDao.getCategoryList();
  }

  public Category getCategory(String categoryId) {
    return this.categoryDao.getCategory(categoryId);
  }


	public List getProductListByCategory(String categoryId) {
		return this.productDao.getProductListByCategory(categoryId);
	}

	public List searchProductList(String keywords) {
		return this.productDao.searchProductList(keywords);
	}

  public Product getProduct(String productId) {
    return this.productDao.getProduct(productId);
  }


  public List getItemListByProduct(String productId) {
		return this.itemDao.getItemListByProduct(productId);
  }

  public Item getItem(String itemId) {
		return this.itemDao.getItem(itemId);
  }

  public boolean isItemInStock(String itemId) {
		return this.itemDao.isItemInStock(itemId);
  }

  public void insertOrder(Order order) {
		this.orderDao.insertOrder(order);
		this.itemDao.updateQuantity(order);
  }

  public Order getOrder(int orderId) {
    return this.orderDao.getOrder(orderId);
  }

  public List getOrdersByUsername(String username) {
    return this.orderDao.getOrdersByUsername(username);
  }

}
