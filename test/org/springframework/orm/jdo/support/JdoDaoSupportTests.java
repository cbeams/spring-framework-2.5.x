package org.springframework.orm.jdo.support;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManagerFactory;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.orm.jdo.JdoTemplate;

/**
 * @author Juergen Hoeller
 * @since 30.07.2003
 */
public class JdoDaoSupportTests extends TestCase {

	public void testJdoDaoSupportWithPersistenceManagerFactory() throws Exception {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		pmfControl.replay();
		final List test = new ArrayList();
		JdoDaoSupport dao = new JdoDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setPersistenceManagerFactory(pmf);
		dao.afterPropertiesSet();
		assertEquals("Correct PersistenceManagerFactory", pmf, dao.getPersistenceManagerFactory());
		assertEquals("Correct JdoTemplate", pmf, dao.getJdoTemplate().getPersistenceManagerFactory());
		assertEquals("initDao called", test.size(), 1);
		pmfControl.verify();
	}

	public void testJdoDaoSupportWithJdoTemplate() throws Exception {
		JdoTemplate template = new JdoTemplate();
		final List test = new ArrayList();
		JdoDaoSupport dao = new JdoDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setJdoTemplate(template);
		dao.afterPropertiesSet();
		assertEquals("Correct JdoTemplate", template, dao.getJdoTemplate());
		assertEquals("initDao called", test.size(), 1);
	}

}
