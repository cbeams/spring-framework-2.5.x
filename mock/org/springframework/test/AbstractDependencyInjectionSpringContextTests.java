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

package org.springframework.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Convenient superclass for tests depending on a Spring context.
 * Really for integration testing, not unit testing. 
 * You should <i>not</i> normally use the Spring container 
 * for unit tests: simply populate your POJOs in plain 
 * JUnit tests!
 * <br>
 * This supports two modes of populating the test:
 * <li>Via Setter Dependency Injection. Simply express dependencies on objects in the 
 * test fixture, and they will be satisfied by autowiring by type.
 * <li>Via Field Injection. Declare protected variables of the required type
 * which match named beans in the context. This is autowire by name,
 * rather than type. This approach is based on an 
 * approach originated by Ara Abrahmian. Setter Dependency Injection
 * is the default: set the populateProtectedVariables property to true
 * in the constructor to switch on Field Injection.
 * <p>
 * This class will normally cache contexts based on a <i>context key</i>:
 * normally the config locations String array describing the Spring Resource descriptors
 * making up the context. Unless the setDirty() method is called by a test, the
 * context will not be reloaded, even across different subclasses of this test.
 * This is particularly beneficial if your context is slow to construct, for
 * example if you are using Hibernate and the time taken to load the mappings
 * is an issue. 
 * <p>
 * If you don't want this behaviour, you can override the contextKey() method, most likely to
 * return the test class. In conjunction with this you would probably override the buildContext()
 * method, which by default loads the locations specified in the getConfigLocations() method.
 * 
 * @author Rod Johnson
 */
public abstract class AbstractDependencyInjectionSpringContextTests extends AbstractSpringContextTests {
    
    /**
     * Key for the context.
     * This enables multiple contexts to share the same key.
     */
    private Object contextKey;
    
    private boolean populateProtectedVariables = false;
        
    /** Application context this test will run against */
    protected ConfigurableApplicationContext applicationContext;
    
    protected String[] managedVariableNames;
    
    private int loads;
    
    protected AbstractDependencyInjectionSpringContextTests(String s) {
        super(s);
    }
    
    protected AbstractDependencyInjectionSpringContextTests() {
    }
    
    public final int getLoads() {
        return loads;
    }
    
    
    /**
     * Called to say that the applicationContext instance variable is dirty and should be reloaded.
     * We need to do this if a test has modified the context (for example, by replacing
     * a bean definition).
     */
    public void setDirty() {
        setDirty(getConfigLocations());
    }
    
    /**
     * @return Returns the populateFields.
     */
    public boolean isPopulateProtectedVariables() {
        return populateProtectedVariables;
    }
    /**
     * @param populateFields The populateFields to set.
     */
    public void setPopulateProtectedVariables(boolean populateFields) {
        this.populateProtectedVariables = populateFields;
    }

    
    protected final void setUp() throws Exception {     
        
        if (contextKey == null) {
            contextKey = contextKey();
        }
        
        this.applicationContext = getContext(contextKey);
        
        if (isPopulateProtectedVariables()) {
            if (managedVariableNames == null) {
                initManagedVariableNames();
            }
            //System.out.println("POPULATED PROTECTED VARS");
            populateProtectedVariables();
        }
        else {
            applicationContext.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
        }
        
        try {
            onSetUp();
        }
        catch (Exception ex) {
            log.error("Setup error: " + ex);
            throw ex;
        }
    }

     protected ConfigurableApplicationContext loadContextLocations(String[] locations) {
         ++loads;
         return super.loadContextLocations(locations);
     }
    
    
    
    /**
     * Return a key for this context. Usually based on config locations, but
     * a subclass overriding buildContext() might want to return its class.
     * Called once and cached.
     * @return
     */
    protected Object contextKey() {
        return getConfigLocations();
    }
    
    /**
     * Subclasses can override this method in place of
     * the setUp() method, which is final in this class.
     * This implementation does nothing.
     *
     */
    protected void onSetUp() throws Exception {
    }
    
    /**
     * Reload the context if it's marked as dirty
     * @see junit.framework.TestCase#tearDown()
     */
    protected final void tearDown() throws Exception {
        try {
            onTearDown();
        }
        catch (Exception ex) {
            log.error("onTearDown error", ex);
        }               
    }
    
    /**
     * Subclasses can override this to add custom behaviour on tear down
     * @throws Exception
     */
    protected void onTearDown() throws Exception {
    }
    
    
    /**
     * Subclasses must implement this method to return the
     * classpath locations of their config files. E.g.
     * classpath:org/springframework/whatever/foo.xml
     * 
     * @return an array of config locations
     */
    protected abstract String[] getConfigLocations();
    
    
    protected void initManagedVariableNames() throws IllegalAccessException {
        LinkedList managedVarNames = new LinkedList();
        
        Class clazz = getClass();
        
        do {
            Field[] fields = clazz.getDeclaredFields();
            log.debug(fields.length + " fields on " + clazz);
            
            for (int i = 0; i < fields.length; i++) {
                // TODO go up tree but not to this class
                Field f = fields[i];
                f.setAccessible(true);
                log.debug("Candidate field " + f);
                if (!Modifier.isStatic(f.getModifiers()) && Modifier.isProtected(f.getModifiers())) {
                    Object oldValue = f.get(this);
                    if (oldValue == null) {
                        managedVarNames.add(f.getName());
                        log.info("Added managed variable '" + f.getName() + "'");
                    }
                    else {
                        log.info("Rejected managed variable '" + f.getName() + "'");
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        while (clazz != AbstractSpringContextTests.class);
        
        this.managedVariableNames = (String[]) managedVarNames.toArray(new String[managedVarNames.size()]);
    }
    
    protected void populateProtectedVariables() throws IllegalAccessException {
        for (int i = 0; i < managedVariableNames.length; i++) {
            Object bean = null;
            Field f = null;
            try {
                f = findField(getClass(), managedVariableNames[i]);
                // TODO what if not found?
                bean = applicationContext.getBean(managedVariableNames[i]);
                f.set(this, bean);
                log.info("Populated " + f);
            }
            catch (IllegalArgumentException ex) {
                log.error("Value " + bean + " not compatible with " + f);
            }
            catch (NoSuchFieldException ex) {
                log.warn("No field with name '" + managedVariableNames[i] + "'");
            }
            catch (SecurityException ex) {
                ex.printStackTrace();
            }
            catch (NoSuchBeanDefinitionException ex) {
                System.err.println("No bean with name '" + managedVariableNames[i] + "'");
            }
        }
    }
    
    private Field findField(Class clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        }
        catch (NoSuchFieldException ex) {
            Class superclass = clazz.getSuperclass();
            if (superclass != AbstractSpringContextTests.class) {
                return findField(superclass, name);
            }
            else {
                throw ex;
            }
        }
    }


}
