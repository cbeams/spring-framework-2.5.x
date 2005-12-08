package org.springframework.beans.factory.xml.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.config.AbstractInterceptorDrivenBeanDefinitionDecorator;

import org.w3c.dom.Element;

/**
 * @author robh
 */
public class TestNamespaceHandler extends NamespaceHandlerSupport {

    public TestNamespaceHandler() {
        registerBeanDefinitionParser("testBean", new TestBeanDefinitionParser());

        registerBeanDefinitionDecorator("set", new PropertyModifyingBeanDefinitionDecorator());
        registerBeanDefinitionDecorator("debug", new DebugBeanDefinitionDecorator());
        registerBeanDefinitionDecorator("nop", new NopInterceptorBeanDefinitionDecorator());
    }

    private static class TestBeanDefinitionParser implements BeanDefinitionParser {

        public void parse(Element element, BeanDefinitionRegistry registry) {
            RootBeanDefinition definition = new RootBeanDefinition();
            definition.setBeanClass(TestBean.class);

            MutablePropertyValues mpvs = new MutablePropertyValues();
            mpvs.addPropertyValue("name", element.getAttribute("name"));
            mpvs.addPropertyValue("age", element.getAttribute("age"));
            definition.setPropertyValues(mpvs);

            registry.registerBeanDefinition(element.getAttribute("id"), definition);
        }
    }

    private static class PropertyModifyingBeanDefinitionDecorator implements BeanDefinitionDecorator {

        public BeanDefinitionHolder decorate(Element element, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
            BeanDefinition def = definition.getBeanDefinition();

            MutablePropertyValues mpvs = (def.getPropertyValues() == null) ? new MutablePropertyValues() : def.getPropertyValues();
            mpvs.addPropertyValue("name", element.getAttribute("name"));
            mpvs.addPropertyValue("age", element.getAttribute("age"));

            ((AbstractBeanDefinition)def).setPropertyValues(mpvs);
            return definition;
        }
    }

    private static class DebugBeanDefinitionDecorator extends AbstractInterceptorDrivenBeanDefinitionDecorator {

        protected BeanDefinition createInterceptorDefinition(Element element) {
            return new RootBeanDefinition(DebugInterceptor.class);
        }
    }

    private static class NopInterceptorBeanDefinitionDecorator extends AbstractInterceptorDrivenBeanDefinitionDecorator {

        protected BeanDefinition createInterceptorDefinition(Element element) {
            return new RootBeanDefinition(NopInterceptor.class);
        }
    }
}
