package org.springframework.beans.factory;

import org.springframework.beans.TestBean;

/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractListableBeanFactoryTests extends AbstractBeanFactoryTests {

	/** Subclasses must initialize this */
	protected ListableBeanFactory getListableBeanFactory() {
		BeanFactory bf = getBeanFactory();
		if (!(bf instanceof ListableBeanFactory)) {
			throw new RuntimeException("ListableBeanFactory required");
		}
		return (ListableBeanFactory) bf;
	}
	
	/**
	 * Subclasses can override this.
	 */
	public void testCount() {
		assertCount(13);
	}
	
	protected final void assertCount(int count) {
		String[] defnames = getListableBeanFactory().getBeanDefinitionNames();
		assertTrue("We should have " + count + " beans, not " + defnames.length, defnames.length == count);
	}

	public void testTestBeanCount() {
		assertTestBeanCount(7);
	}

	public void assertTestBeanCount(int count) {
		String[] defnames = getListableBeanFactory().getBeanDefinitionNames(TestBean.class);
		assertTrue("We should have " + count + " beans for class org.springframework.beans.TestBean, not " +
		           defnames.length, defnames.length == count);
	}

	public void testGetDefinitionsForNoSuchClass() {
		String[] defnames = getListableBeanFactory().getBeanDefinitionNames(String.class);
		assertTrue("No string definitions", defnames.length == 0);
	}
	
	/**
	 * Check that count refers to factory class, not
	 * bean class (we don't know what type factories may return,
	 * and it may even change over time).
	 */
	public void testGetCountForFactoryClass() {
		assertTrue("Should have 2 factories, not " + getListableBeanFactory().getBeanDefinitionNames(FactoryBean.class).length,
			getListableBeanFactory().getBeanDefinitionNames(FactoryBean.class).length == 2);
	}

	public void testContainsBeanDefinition() {
		assertTrue(getListableBeanFactory().containsBeanDefinition("rod"));
		assertTrue(getListableBeanFactory().containsBeanDefinition("roderick"));
	}

}
