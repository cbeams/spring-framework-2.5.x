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
package org.springframework.web.flow;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.web.util.SessionKeyUtils;

/**
 * @author Keith Donald
 */
public abstract class AbstractFlowTests extends AbstractTransactionalSpringContextTests {

    private Flow flow;

    protected FlowSessionExecutionStack flowSessionExecutionStack;

    protected void setFlow(Flow flow) {
        Assert.notNull(flow, "Flow is required for this test");
        this.flow = flow;
    }

    protected void assertModelAttributePresent(Map attributeMap, String attributeName) {
        assertNotNull("The model attribute '" + attributeMap + "' is not present", attributeMap.get(attributeName));
    }

    protected void assertModelAttributeInstanceOf(Map attributeMap, String attributeName, Class clazz) {
        assertModelAttributePresent(attributeMap, attributeName);
        Assert.isInstanceOf(clazz, attributeMap.get(attributeName));
    }

    protected void assertModelAttributeEquals(Map attributeMap, String attributeName, Object attributeValue) {
        if (attributeValue != null) {
            assertModelAttributeInstanceOf(attributeMap, attributeName, attributeValue.getClass());
        }
        assertEquals("The model attribute '" + attributeName + "' must equal '" + attributeValue + "'", attributeValue,
                attributeMap.get(attributeName));
    }

    protected void assertModelCollectionAttributeSize(Map attributeMap, String attributeName, int size) {
        assertModelAttributeInstanceOf(attributeMap, attributeName, Collection.class);
        assertEquals("The model collection attribute '" + attributeName + "' must have " + size + " elements", size,
                ((Collection)attributeMap.get(attributeName)).size());
    }

    protected void assertModelAttributePropertyEquals(Map attributeMap, String attributeName, String propertyName,
            Object propertyValue) {
        assertModelAttributePresent(attributeMap, attributeName);
        Object value = attributeMap.get(attributeName);
        Assert.isTrue(!BeanUtils.isSimpleProperty(value.getClass()), "Attribute value must be a bean");
        BeanWrapper wrapper = new BeanWrapperImpl(value);
        assertEquals(propertyValue, wrapper.getPropertyValue(propertyName));
    }

    protected String generateUniqueFlowSessionId(FlowSessionExecutionStack stack) {
        return SessionKeyUtils.generateMD5SessionKey(String.valueOf(stack.hashCode()), true);
    }

    protected FlowSessionExecutionStack createFlowSessionExecutionStack() {
        return new FlowSessionExecutionStack();
    }

    protected FlowEventProcessor getEventProcessor() {
        return flow;
    }

}