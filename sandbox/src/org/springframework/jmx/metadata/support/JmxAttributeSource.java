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
package org.springframework.jmx.metadata.support;

import java.lang.reflect.Method;

/**
 * Interface used by the <code>MetadataModelMBeanInfoAssembler</code> to
 * read source level metadata from a managed resource's class.
 * @author robh
 */
public interface JmxAttributeSource {

    /**
     * Sub-classes should return an instance of <code>ManagedResource</code> if
     * the resource's class has the appropriate metadata otherwise should return
     * <code>null</code>.
     * @param cls the <code>Class</code> of the managed resource.
     * @return an instance of <code>ManagedResource</code> representing the metadata or <code>null</code> if no metadata is found.
     * @throws InvalidMetadataException if the <code>Class</code> has duplicate metadata representing a <code>ManagedResource</code>.
     */
    ManagedResource getManagedResource(Class cls) throws InvalidMetadataException;
    
    /**
     * Sub-class should return an instance of <code>ManagedAttribute</code> if
     * the supplied <code>Method</code> has the corresponding metadata otherwise should
     * return <code>null</code>.
     * @param method a <code>Method</code> of the managed resource.
     * @return an instance of <code>ManagedAttribute</code> representing the metadata or <code>null</code> if no metadata is found.
     * @throws InvalidMetadataException if the <code>Method</code> has duplicate metadata representing a <code>ManagedAttribute</code>.
     */
    ManagedAttribute getManagedAttribute(Method method)  throws InvalidMetadataException;
    
    /**
     * Sub-class should return an instance of <code>ManagedOperation</code> if
     * the supplied <code>Method</code> has the corresponding metadata otherwise should
     * return <code>null</code>.
     * @param method a <code>Method</code> of the managed resource.
     * @return an instance of <code>ManagedOperation</code> representing the metadata or <code>null</code> if no metadata is found.
     * @throws InvalidMetadataException if the <code>Method</code> has duplicate metadata representing a <code>ManagedOperation</code>.
     */
    ManagedOperation getManagedOperation(Method method)  throws InvalidMetadataException;
    
}
