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
package org.springframework.rules.functions;

import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.UnaryProcedure;

/**
 * Only execute the specified procedure if a provided constraint is also true.
 * 
 * @author keith
 */
public class ConstrainedUnaryProcedure implements UnaryProcedure {
    private UnaryProcedure procedure;
    private UnaryPredicate constraint;

    public ConstrainedUnaryProcedure(UnaryProcedure procedure,
            UnaryPredicate constraint) {
        this.procedure = procedure;
        this.constraint = constraint;
    }

    /**
     * Will only invoke the procedure against the provided argument if the
     * constraint permits; else no action will be taken.
     * 
     * @see org.springframework.rules.UnaryProcedure#run(java.lang.Object)
     */
    public void run(Object argument) {
        if (constraint.test(argument)) {
            procedure.run(argument);
        }
    }

}