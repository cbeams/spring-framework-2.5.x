package org.springframework.context.access;

import junit.framework.TestCase;

import org.springframework.beans.factory.access.BeanFactoryLocator;

/**
 * Tests LocatoryFactory
 * 
 * @version $Revision: 1.1 $
 * @author colin sampaleanu
 */
public class LocatorFactoryTest extends TestCase {

  /*
   * Class to test for BeanFactoryLocator getInstance()
   */
  public void testGetInstance() {
    BeanFactoryLocator bf = LocatorFactory.getInstance();
    BeanFactoryLocator bf2 = LocatorFactory.getInstance();
    assertTrue(bf.equals(bf2));
  }

  /*
   * Class to test for BeanFactoryLocator getInstance(String)
   */
  public void testGetInstanceString() {
    BeanFactoryLocator bf = LocatorFactory.getInstance("my-bean-refs.xml");
    BeanFactoryLocator bf2 = LocatorFactory.getInstance("my-bean-refs.xml");
    assertTrue(bf.equals(bf2));
  }
}
