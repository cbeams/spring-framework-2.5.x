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
 * Adapts access to a domain model aspect (generally a property) to the value
 * model interface. The aspect access strategy is pluggable.
 * 
 * @author Keith Donald
 */
public class AspectAdapter extends AbstractValueModel {
    private String aspect;

    private MutableAspectAccessStrategy aspectAccessStrategy;

    public AspectAdapter(MutableAspectAccessStrategy aspectAccessStrategy,
            String aspect) {
        if (aspectAccessStrategy.getDomainObjectHolder() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[Aspect Adapter for aspect '" + aspect
                        + "' attaching to mutable domain object holder.]");
            }
            aspectAccessStrategy.getDomainObjectHolder().addValueListener(
                    new ValueListener() {
                        public void valueChanged() {
                            if (logger.isDebugEnabled()) {
                                logger
                                        .debug("[Notifying any dependents for '"
                                                + AspectAdapter.this.aspect
                                                + "' the '"
                                                + AspectAdapter.this.aspect
                                                + "' aspect value may have changed; target domain object changed]");
                            }
                            AspectAdapter.this.fireValueChanged();
                        }
                    });
        }
        this.aspectAccessStrategy = aspectAccessStrategy;
        this.aspect = aspect;
    }

    public void addValueListener(ValueListener l) {
        super.addValueListener(l);
        aspectAccessStrategy.addValueListener(l, aspect);
    }

    public void removeValueListener(ValueListener l) {
        super.removeValueListener(l);
        aspectAccessStrategy.removeValueListener(l, aspect);
    }

    public Object get() {
        return aspectAccessStrategy.getValue(aspect);
    }

    public void set(Object value) {
        aspectAccessStrategy.setValue(aspect, value);
    }
}