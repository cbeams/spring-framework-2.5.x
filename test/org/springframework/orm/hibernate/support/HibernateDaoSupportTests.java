package org.springframework.orm.hibernate.support;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.hibernate.SessionFactory;
import org.easymock.MockControl;

import org.springframework.orm.hibernate.HibernateTemplate;

/**
 * @author Juergen Hoeller
 * @since 30.07.2003
 */
public class HibernateDaoSupportTests extends TestCase {

	public void testHibernateDaoSupportWithSessionFactory() throws Exception {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		sfControl.replay();
		final List test = new ArrayList();
		HibernateDaoSupport dao = new HibernateDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setSessionFactory(sf);
		dao.afterPropertiesSet();
		assertEquals("Correct SessionFactory", sf, dao.getSessionFactory());
		assertEquals("Correct HibernateTemplate", sf, dao.getHibernateTemplate().getSessionFactory());
		assertEquals("initDao called", test.size(), 1);
		sfControl.verify();
	}

	public void testHibernateDaoSupportWithHibernateTemplate() throws Exception {
		HibernateTemplate template = new HibernateTemplate();
		final List test = new ArrayList();
		HibernateDaoSupport dao = new HibernateDaoSupport() {
			protected void initDao() {
				test.add("test");
			}
		};
		dao.setHibernateTemplate(template);
		dao.afterPropertiesSet();
		assertEquals("Correct HibernateTemplate", template, dao.getHibernateTemplate());
		assertEquals("initDao called", test.size(), 1);
	}

}
