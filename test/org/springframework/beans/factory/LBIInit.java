package org.springframework.beans.factory;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;

public class LBIInit {
	
	/**
	 * Create beans necessary to run tests.
	 */
	public static void createTestBeans(DefaultListableBeanFactory lbf) throws BeansException {
		Map m = new HashMap();
		// Rod is a singleton
		m.put("rod.class", "org.springframework.beans.TestBean");
		m.put("rod.name", "Rod");
		m.put("rod.age", "31");
		
		m.put("roderick.parent","rod");
		m .put("roderick.name", "Roderick");
		
		// Kerry is a singleton
		m.put("kerry.class", "org.springframework.beans.TestBean");
		m.put("kerry.name", "Kerry");
		m.put("kerry.age", "34");
		m.put("kerry.spouse(ref)", "rod");
		
		// Kathy is a type
		m.put("kathy.class", "org.springframework.beans.TestBean");
		m.put("kathy.(singleton)", "false");
		
		m.put("typeMismatch.class", "org.springframework.beans.TestBean");
		m.put("typeMismatch.name", "typeMismatch");
		m.put("typeMismatch.age", "34x");
		m.put("typeMismatch.spouse(ref)", "rod");
		m.put("typeMismatch.(singleton)","false");
		
		m.put("validEmpty.class", "org.springframework.beans.TestBean");
		
		m.put("listenerVeto.class", "org.springframework.beans.TestBean");
		
		m.put("typeMismatch.name", "typeMismatch");
		m.put("typeMismatch.age", "34x");
		m.put("typeMismatch.spouse(ref)", "rod");
		
		m.put("singletonFactory.class", "org.springframework.beans.factory.DummyFactory");
		m.put("singletonFactory.singleton", "true");
		
		m.put("prototypeFactory.class", "org.springframework.beans.factory.DummyFactory");
		m.put("prototypeFactory.singleton", "false");
		
		m.put("factoryPassThrough.class", "org.springframework.beans.factory.DummyFactory");
		m.put("factoryPassThrough.singleton", "true");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "passThrough"));
		m.put("factoryPassThrough.propertyValues", pvs);
		
		m.put("mustBeInitialized.class", "org.springframework.beans.factory.MustBeInitialized");
		
		m.put("lifecycle.class", "org.springframework.beans.factory.LifecycleBean");

		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(m);
	}

}
