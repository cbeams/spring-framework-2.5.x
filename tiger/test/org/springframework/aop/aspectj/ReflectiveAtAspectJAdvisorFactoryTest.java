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

package org.springframework.aop.aspectj;

import org.springframework.aop.aspectj.AtAspectJAdvisorFactory;
import org.springframework.aop.aspectj.ReflectiveAtAspectJAdvisorFactory;

/**
 * Tests for ReflectiveAtAspectJAdvisorFactory. 
 * Tests are inherited: we only set the test fixture here.
 * @author Rod Johnson
 * @since 1.3
 */
public class ReflectiveAtAspectJAdvisorFactoryTest extends
		AbstractAtAspectJAdvisorFactoryTests {

	@Override
	protected AtAspectJAdvisorFactory getFixture() {
		return new ReflectiveAtAspectJAdvisorFactory();
	}

}
