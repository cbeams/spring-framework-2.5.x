package org.springframework.web.servlet.config;

import junit.framework.TestCase;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.tiles.TilesJstlView;

public class ViewResolversBeanDefinitionParserTests extends TestCase {
	
	public void testParserWithTwoResolversAndDefaults() {
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/web/servlet/config/viewResolvers-defaults.xml");
		
		ConfigurableListableBeanFactory bf = 
			(ConfigurableListableBeanFactory)context.getAutowireCapableBeanFactory();
		
		String[] vrNames = bf.getBeanNamesForType(ViewResolver.class);
		assertEquals(2, vrNames.length);
		
		// test bean name view resolver
		RootBeanDefinition definition = (RootBeanDefinition)bf.getBeanDefinition(vrNames[0]);
		assertEquals(BeanNameViewResolver.class, definition.getBeanClass());
		assertPropertyValue(new Integer(0), definition, "order");
		
		// test internal resource view resolver with attributes set
		definition = (RootBeanDefinition)bf.getBeanDefinition(vrNames[1]);
		assertEquals(InternalResourceViewResolver.class, definition.getBeanClass());
		assertPropertyValue(InternalResourceView.class, definition, "viewClass");
		assertPropertyValue("/WEB-INF/jsp/", definition, "prefix");
		assertPropertyValue(".jsp", definition, "suffix");
		assertPropertyValue("true", definition, "cache");
		assertPropertyValue("true", definition, "redirectHttp10Compatible");
		assertPropertyValue("true", definition, "redirectContextRelative");
		assertNull(definition.getPropertyValues().getPropertyValue("content-type"));
		assertNull(definition.getPropertyValues().getPropertyValue("requestContextAttribute"));
	}
	
	public void testParserWithOneResolversAndAttributes() {
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
		"org/springframework/web/servlet/config/viewResolvers-attributes.xml");

		ConfigurableListableBeanFactory bf = 
			(ConfigurableListableBeanFactory)context.getAutowireCapableBeanFactory();
		
		String[] vrNames = bf.getBeanNamesForType(ViewResolver.class);
		assertEquals(1, vrNames.length);
		
		RootBeanDefinition definition = (RootBeanDefinition)bf.getBeanDefinition(vrNames[0]);
		
		assertEquals(InternalResourceViewResolver.class, definition.getBeanClass());
		assertPropertyValue(TilesJstlView.class, definition, "viewClass");
		assertPropertyValue("false", definition, "cache");
		assertPropertyValue("/WEB-INF/jsp/", definition, "prefix");
		assertPropertyValue(".jsp", definition, "suffix");
		assertPropertyValue("false", definition, "redirectHttp10Compatible");
		assertPropertyValue("false", definition, "redirectContextRelative");
		assertPropertyValue("text/html", definition, "contentType");
		assertPropertyValue("requestContext", definition, "requestContextAttribute");		
	}
		 

	private void assertPropertyValue(Object assertionValue, RootBeanDefinition definition, String property) {
		assertNotNull(definition.getPropertyValues().getPropertyValue(property));
		assertEquals(assertionValue, definition.getPropertyValues().getPropertyValue(property).getValue());
	}

}
