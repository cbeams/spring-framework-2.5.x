/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.xml;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Unit tests for the XmlBeanDefinitionReader class.
 *
 * @author Rick Evans
 * @since 03.01.2006
 */
public final class XmlBeanDefinitionReaderTests extends TestCase {

    private MockControl mock;
    private BeanDefinitionRegistry registry;

    public void setup() {
        this.mock = MockControl.createControl(BeanDefinitionRegistry.class);
        this.registry = (BeanDefinitionRegistry) mock.getMock();
    }

    public void testSetParserClassSunnyDay() throws Exception {
        new XmlBeanDefinitionReader(this.registry).setParserClass(DefaultXmlBeanDefinitionParser.class);
    }

    public void testSetParserClassToNull() throws Exception {
        try {
            new XmlBeanDefinitionReader(this.registry).setParserClass(null);
            fail("An IllegalArgumentException must have been thrown (null parserClass).");
        } catch (final IllegalArgumentException expected) {
        }
    }

    public void testSetParserClassToUnsupportedParserType() throws Exception {
        try {
            new XmlBeanDefinitionReader(this.registry).setParserClass(String.class);
            fail("An IllegalArgumentException must have been thrown (usupported parserClass).");
        } catch (final IllegalArgumentException expected) {
        }
    }

}
