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

import java.util.Map;

/**
 * @author Keith Donald
 */
public interface FormModel {
    public ValueModel getFormObjectHolder();

    public Object getFormObject();

    public ValueModel getDisplayValueModel(String formPropertyPath);

    public ValueModel getValueModel(String formPropertyPath);

    public String getDisplayValue(String formPropertyPath);

    public Object getValue(String formPropertyPath);

    public Map getErrors();

    public boolean getHasErrors();

    public boolean getBufferChangesDefault();

    public boolean isDirty();

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public void commit();

    public void revert();

    public void addValidationListener(ValidationListener listener);

    public void removeValidationListener(ValidationListener listener);

    public void addValueListener(String formPropertyPath, ValueListener listener);

    public void removeValueListener(String formPropertyPath, ValueListener listener);

    public void addCommitListener(CommitListener listener);

    public void removeCommitListener(CommitListener listener);

}