/*
 * Copyright 2002-2005 the original author or authors.
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
/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * An ordered chain of <code>TransitionCriteria</code>.
 * @author Keith Donald
 */
public class TransitionCriteriaChain implements TransitionCriteria {

	/**
	 * The ordered chain.
	 */
	private LinkedList chain = new LinkedList();

	/**
	 * Creates an initially empty transition criteria chain. 
	 */
	public TransitionCriteriaChain() {
		
	}

	/**
	 * Creates an transition criteria chain with the specified criteria. 
	 * @param criteria the criteria
	 */
	public TransitionCriteriaChain(TransitionCriteria[] criteria) {
		chain.addAll(Arrays.asList(criteria));
	}

	public void add(TransitionCriteria criteria) {
		this.chain.add(criteria);
	}

	/* Iterates over each element in the chain, continues until one returns false or the
	 * list is exhausted.
	 * @see org.springframework.web.flow.TransitionCriteria#test(org.springframework.web.flow.RequestContext)
	 */
	public boolean test(RequestContext context) {
		Iterator it = chain.iterator();
		while (it.hasNext()) {
			TransitionCriteria criteria = (TransitionCriteria)it.next();
			if (!criteria.test(context)) {
				return false;
			}
		}
		return true;
	}
}