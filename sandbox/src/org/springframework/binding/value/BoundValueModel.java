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
package org.springframework.binding.value;


/**
 * A sub-interface of ValueModel that adds support for bound javabean
 * properties. Clients may subscribe as property change listeners for the
 * "value" property. This is useful when clients need to process both the
 * "oldValue" and "newValue" on value change events.
 * 
 * @author Keith Donald
 */
public interface BoundValueModel extends ValueModel, PropertyChangePublisher {

}