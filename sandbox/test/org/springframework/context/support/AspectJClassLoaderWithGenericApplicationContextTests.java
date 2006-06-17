package org.springframework.context.support;

import junit.framework.TestCase;
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.instrument.classloading.ShadowingClassLoader;
import org.springframework.test.CallMonitor;
import org.springframework.util.ClassUtils;

public class AspectJClassLoaderWithGenericApplicationContextTests extends TestCase {

    private static final String AOP_CONFIG_LOCATION_PROPERTY = "org.aspectj.weaver.loadtime.configuration";

    protected void setUp() throws Exception {
        CallMonitor.resetAll();
    }

    public void testCreateApplicationContextWithBeanFactoryAndAspectJClassLoader() {
        // Set the AspectJ aop.xml location to this package. This is only required to get
        // a cleaner setup for this test.
        String originalLocation = System.getProperty(AOP_CONFIG_LOCATION_PROPERTY);
        System.setProperty(
            AOP_CONFIG_LOCATION_PROPERTY,
            getClass().getPackage().getName().replace('.', '/') + "/aop.xml");

        // Create ShadowingClassLoader (a.k.a. the demonic classloader). This classloader
        // will use the AspectJ load-time weaver internally, this is where the magic of
        // load-time weaving happens. Since the BeanFactory and ApplicationContext will
        // use this classloader classes that are loaded can be woven by AspectJ.

        // Two additional exclusions are required on top of those defined by ShadowingClassLoader:
        // + org.springframework.beans.factory.: many things go wrong without this exclusion
        // + org.springframework.test.CallMonitor: to prevent the class is loaded by two classloaders (one here and the other one in TestBeanAdvice)
        ShadowingClassLoader shadowingClassLoader = new ShadowingClassLoader(ClassUtils.getDefaultClassLoader()) {
            protected boolean isClassNameExcludedFromShadowing(String className) {
                return
                    className.startsWith("org.springframework.beans.factory.") ||
                    className.startsWith(CallMonitor.class.getName());
            }
        };
        // Register the AspectJ load-time weaver with the ShadowingClassLoader.
        shadowingClassLoader.addTransformer(new ClassPreProcessorAgentAdapter());
        // Create a GenericApplicationContext instance that uses the BeanFactory internally.
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        try {
            // Now lets load an XML file.
            XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);
            // Set the ShadowingClassLoader on the BeanDefinitionReader. This is required because otherwise
            // classes defined in BeanDefinitions will be loaded by the regular classloader and thus not be
            // woven by AspectJ.
            beanDefinitionReader.setBeanClassLoader(shadowingClassLoader);
            beanDefinitionReader.loadBeanDefinitions("classpath:org/springframework/context/support/testbean-context.xml");
            // Refresh the ApplicationContext.
            applicationContext.refresh();
            // Assert that TestBeanAdvice has kicked in
            CallMonitor.assertCounter(1, "TestBean.setBeanFactory");
        } finally {
            // Be a good citizen and restore the system-wide property to its original value
            if (originalLocation != null) {
                System.setProperty(AOP_CONFIG_LOCATION_PROPERTY, originalLocation);
            } else {
                System.setProperty(AOP_CONFIG_LOCATION_PROPERTY, "");
            }
        }
    }
}
