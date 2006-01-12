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

package org.springframework.jdbc.support;

import java.sql.Types;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class NamedParameterUtilsTests extends TestCase {

	public void testCountParameterPlaceholders() {
		assertTrue(NamedParameterUtils.countParameterPlaceholders(null) == 0);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("") == 0);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("?") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big\" ? 'bad wolf'") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big ?? bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big?\" 'ba''ad?' ? wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders(":parameter") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big\" :parameter 'bad wolf'") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter :parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big:\" 'ba''ad?' :parameter wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("&parameter") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big\" &parameter 'bad wolf'") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big &parameter &parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big &x  \" 'ba''ad?' &parameter wolf") == 1);
	}

}
