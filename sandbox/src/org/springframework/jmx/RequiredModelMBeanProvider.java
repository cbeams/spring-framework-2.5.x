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
package org.springframework.jmx;

import javax.management.MBeanException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * Implementation of the <code>ModelMBeanProvider</code> interface that
 * returns an instance of the <code>RequiredModelMBean</code> class
 * that all JMX implementations are required to supply.
 * @author robh
 */
public class RequiredModelMBeanProvider implements ModelMBeanProvider {

    /**
     * Returns an instance of <code>RequiredModelMBean</code>.
     */
   public ModelMBean getModelMBean()  throws MBeanException{
       return new RequiredModelMBean();
   }

}
