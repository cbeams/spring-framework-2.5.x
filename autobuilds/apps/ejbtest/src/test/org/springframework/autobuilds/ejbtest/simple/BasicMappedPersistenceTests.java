/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.autobuilds.ejbtest.simple;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junitx.extensions.TestSetup;
import junitx.util.ResourceManager;

import org.springframework.autobuilds.ejbtest.dbutil.mapper.Mapper;
import org.springframework.autobuilds.ejbtest.dbutil.mapper.MapperFactory;
import org.springframework.autobuilds.ejbtest.domain.User1;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

/**
 * Test which checks that basic Mapper persistence is working.
 * 
 * Note that this test should not be considered the right way to use mapper objects in
 * a real app, since this test is concerned with persistence only. The same mapper
 * classes in real life would normally be used by service objects wrapped with
 * declarative transactions.
 * 
 * @author Colin Sampaleanu
 */
public class BasicMappedPersistenceTests extends TestCase {

  public static final String CONTEXT = ClassUtils.classPackageAsResourcePath(BasicMappedPersistenceTests.class)
      + "/BasicMappedPersistenceTestContext.xml";

  // ids for storing contexts as quasi singletons between individual tests. Hibernate SessionFactory
  // setup would kill us otherwise
  public static final String APP_CONTEXT_ID = "BasicMappedPersistenceTests.appContext";
  public static final String APP_CONTEXT2_ID = "BasicMappedPersistenceTests.appContext2";

  // --- attributes
  // per-TestSuite specific vars, via ResourceManager!
  ApplicationContext appContext;

  ApplicationContext appContext2;

  // normal vars
  // --- methods
  // --- attributes
  public static Test suite() {
    return new TestSetup(new TestSuite(BasicMappedPersistenceTests.class)) {

      protected void setUp() throws Exception {
        try {
          ApplicationContext appContext;
          appContext = new ClassPathXmlApplicationContext(new String[] {CONTEXT});
          ResourceManager.addResource(APP_CONTEXT_ID, appContext);
          ApplicationContext appContext2;
          appContext2 = new ClassPathXmlApplicationContext(new String[] {CONTEXT});
          ResourceManager.addResource(APP_CONTEXT2_ID, appContext2);
        }
        catch (RuntimeException t) {
          // just for debugging since Eclipse swallows these
          throw t;
        }
        catch (Error e) {
          // just for debugging since Eclipse swallows these
          throw e;
        }
      }

      protected void tearDown() throws Exception {
        ResourceManager.removeResource(APP_CONTEXT_ID);
      }
    };
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    appContext = (ApplicationContext) ResourceManager.getResource(APP_CONTEXT_ID);
    appContext2 = (ApplicationContext) ResourceManager.getResource(APP_CONTEXT2_ID);
  }

  /**
   * A basic test to ensure object creation, saving, and cascading is working
   * Note that this test doesn't use transactions in a typical fashion, that is
   * normally, you would probably only use one transaction per method, but here
   * we actually want to force out all our changes to the db so that the db
   * would give an exception on any constraint violations. Note that short of
   * getting a whole new sesion though, the subsequent read-back is still going
   * to give us data from the session cache.
   * 
   * @throws Exception
   */
  public void testBasicCreationSaveAndCascade() throws Exception {

    final Mapper mapper = (Mapper) appContext.getBean("root-mapper");
    PlatformTransactionManager tm = (PlatformTransactionManager) appContext
        .getBean("myTransactionManager");

    TransactionTemplate transactionTemplate = new TransactionTemplate(tm);

    final User1[] savedUser1 = {null};
    final HashMap objMap = new HashMap();

    transactionTemplate.execute(new TransactionCallback() {

      public Object doInTransaction(TransactionStatus status) {
      	
      	User1 user1 = new User1(null, "user1", "pass1");
      	mapper.save(user1);
      	savedUser1[0] = user1;

        return null;
      }
    });

    // now ensure the objects can be read back ok
    final Mapper mapper2 = (Mapper) appContext2.getBean("root-mapper");
    final MapperFactory mapperFactory2 = mapper2.getMapperFactory();
    PlatformTransactionManager tm2 = (PlatformTransactionManager) appContext2
        .getBean("myTransactionManager");

    TransactionTemplate transactionTemplate2 = new TransactionTemplate(tm2);

    transactionTemplate2.execute(new TransactionCallback() {

      public Object doInTransaction(TransactionStatus status) {

        // User1
      	User1 user1 = (User1) mapper2.load(User1.class, savedUser1[0].getId());
      	assertNotNull(user1);

        return null;
      }
    });
  }
}
