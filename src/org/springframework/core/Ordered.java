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

package org.springframework.core;

/**
 * Interface that can be implemented by objects that should be
 * orderable, e.g. in a Collection. The actual order can be
 * interpreted as prioritization, the first object (with the
 * lowest order value) having the highest priority.
 *
 * @author Juergen Hoeller
 * @since 07.04.2003
 */
public interface Ordered {

  /**
   * Return the order value of this object,
   * higher value meaning greater in terms of sorting.
   * Normally starting with 0 or 1, Integer.MAX_VALUE
   * indicating greatest.
   * Same order values will result in arbitrary positions
   * for the affected objects.
   *
   * <p>Higher value can be interpreted as lower priority,
   * consequently the first object has highest priority
   * (somewhat analogous to Servlet "load-on-startup" values).
   *
   * @return the order value
   */
	public int getOrder();
}
