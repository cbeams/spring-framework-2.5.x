/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.rules.values;

/**
 * @author Keith Donald
 */
public interface NestingFormModel extends FormModel {

    public ValueModel findValueModelFor(FormModel delegatingChild,
            String formPropertyPath);

    /**
     * Create a child form model nested by this form model identified by the
     * provided name. The form object associated with the created child model is
     * the same form object managed by the parent.
     * 
     * @param childFormModelName
     * @return The child for model.
     */
    public MutableFormModel createChild(String childFormModelName);

    /**
     * Create a child form model nested by this form model identified by the
     * provided name. The form object associated with the created child model is
     * the value model at the specified parent property path.
     * 
     * @param childFormModelName
     * @param parentPropertyFormObjectPath
     * @return The child form model
     */
    public MutableFormModel createChild(String childFormModelName,
            String parentPropertyFormObjectPath);

    /**
     * Create a child form model nested by this form model identified by the
     * provided name. The form object associated with the created child model is
     * accessed via the provided value model.
     * 
     * @param childFormModelName
     * @param childFormObjectHolder
     * @return The child form model
     */
    public MutableFormModel createChild(String childFormModelName,
            ValueModel childFormObjectHolder);

    /**
     * Retrieve a child form model by name.
     * 
     * @param childModelName
     *            the contained form model's name
     * @return the child form model, or <code>null</code> if none found.
     */
    public FormModel getChildFormModel(String childModelName);

}