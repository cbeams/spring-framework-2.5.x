package org.springframework.context.annotation;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedRootBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Unit tests for the {@link AnnotationBeanNameGenerator} class.
 *
 * @author Rick Evans
 */
public final class AnnotationBeanNameGeneratorTests extends TestCase {

	private AnnotationBeanNameGenerator beanNameGenerator;

	@Override
	public void setUp() {
		this.beanNameGenerator = new AnnotationBeanNameGenerator();
	}

	public void testGenerateBeanNameWithNamedComponent() {
		MockControl control = MockControl.createControl(BeanDefinitionRegistry.class);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) control.getMock();

		control.replay();

		AnnotatedBeanDefinition bd = new AnnotatedRootBeanDefinition(ComponentWithName.class);
		String beanName = this.beanNameGenerator.generateBeanName(bd, registry);
		assertNotNull("The generated beanName must *never* be null.", beanName);
		assertTrue("The generated beanName must *never* be blank.", StringUtils.hasText(beanName));
		assertEquals("walden", beanName);

		control.verify();
	}

	public void testGenerateBeanNameWithNamedComponentWhereTheNameIsBlank() {
		MockControl control = MockControl.createControl(BeanDefinitionRegistry.class);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) control.getMock();

		registry.containsBeanDefinition("annotationBeanNameGeneratorTests.ComponentWithBlankName");
		control.setReturnValue(false);

		control.replay();

		AnnotatedBeanDefinition bd = new AnnotatedRootBeanDefinition(ComponentWithBlankName.class);
		String beanName = this.beanNameGenerator.generateBeanName(bd, registry);
		assertNotNull("The generated beanName must *never* be null.", beanName);
		assertTrue("The generated beanName must *never* be blank.", StringUtils.hasText(beanName));

		String expectedGeneratedBeanName = this.beanNameGenerator.buildDefaultBeanName(bd);

		assertEquals(expectedGeneratedBeanName, beanName);

		control.verify();
	}

	public void testGenerateBeanNameWithAnonymousComponentYieldsGeneratedBeanName() {
		MockControl control = MockControl.createControl(BeanDefinitionRegistry.class);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) control.getMock();

		registry.containsBeanDefinition("annotationBeanNameGeneratorTests.AnonymousComponent");
		control.setReturnValue(false);

		control.replay();

		AnnotatedBeanDefinition bd = new AnnotatedRootBeanDefinition(AnonymousComponent.class);
		String beanName = this.beanNameGenerator.generateBeanName(bd, registry);
		assertNotNull("The generated beanName must *never* be null.", beanName);
		assertTrue("The generated beanName must *never* be blank.", StringUtils.hasText(beanName));

		String expectedGeneratedBeanName = this.beanNameGenerator.buildDefaultBeanName(bd);

		assertEquals(expectedGeneratedBeanName, beanName);

		control.verify();
	}

	public void testGenerateBeanNameWithAnonymousComponentYieldsUniqueGeneratedBeanName() {
		MockControl control = MockControl.createControl(BeanDefinitionRegistry.class);
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) control.getMock();

		registry.containsBeanDefinition("annotationBeanNameGeneratorTests.AnonymousComponent");
		control.setReturnValue(true);
		registry.containsBeanDefinition("annotationBeanNameGeneratorTests.AnonymousComponent#1");
		control.setReturnValue(false);

		control.replay();

		AnnotatedBeanDefinition bd = new AnnotatedRootBeanDefinition(AnonymousComponent.class);
		String beanName = this.beanNameGenerator.generateBeanName(bd, registry);
		assertNotNull("The generated beanName must *never* be null.", beanName);
		assertTrue("The generated beanName must *never* be blank.", StringUtils.hasText(beanName));

		String expectedGeneratedBeanName = this.beanNameGenerator.buildDefaultBeanName(bd);

		// mmm, a tad brittle this :(
		assertEquals(expectedGeneratedBeanName + "#1", beanName);

		control.verify();
	}


	@Component("walden")
	private static final class ComponentWithName {
	}


	@Component(" ")
	private static final class ComponentWithBlankName {
	}


	@Component
	private static final class AnonymousComponent {
	}

}
