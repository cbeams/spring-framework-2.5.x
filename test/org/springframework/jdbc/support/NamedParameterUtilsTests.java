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
import java.util.Map;
import java.util.HashMap;

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
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big ? ? bad ? wolf") == 3);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big?\" 'ba''ad?' ? wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders(":parameter") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big\" :parameter 'bad wolf'") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter :parameter bad wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter :newpar :parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter, :newpar, :parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big:\" 'ba''ad:p' :parameter wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("&parameter") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big\" &parameter 'bad wolf'") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big &parameter &parameter bad wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big &parameter &newparameter &parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big &parameter, &newparameter, &parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The \"big &x  \" 'ba''ad&p' &parameter wolf") == 1);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter, &newparameter, &parameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter, &sameparameter, &sameparameter bad wolf") == 2);
		assertTrue(NamedParameterUtils.countParameterPlaceholders("The big :parameter, :sameparameter, :sameparameter bad wolf") == 2);
	}

    public void testParseSql() {
        String sql = "xxx :a yyyy :b :c :a zzzzz";
        String sql2 = "xxx &a yyyy ? zzzzz";
        ParsedSql psql = NamedParameterUtils.parseSqlStatement(sql);
        assertEquals("xxx ? yyyy ? ? ? zzzzz", psql.getNewSql());
        assertEquals("a", psql.getParameters().get(0));
        assertEquals("c", psql.getParameters().get(2));
        assertEquals("a", psql.getParameters().get(3));
        assertEquals(4, psql.getParameterCount());
        assertEquals(3, psql.getNamedParameterCount());
        ParsedSql psql2 = NamedParameterUtils.parseSqlStatement(sql2);
        assertEquals("xxx ? yyyy ? zzzzz", psql2.getNewSql());
        assertEquals("a", psql2.getParameters().get(0));
        assertEquals(2, psql2.getParameterCount());
        assertEquals(1, psql2.getNamedParameterCount());
    }

    public void testSubstituteNamedParameters() {
        Map argMap = new HashMap();
        argMap.put("a","a");
        argMap.put("b","b");
        argMap.put("c","c");
        assertEquals("xxx ? ? ?", NamedParameterUtils.substituteNamedParameters("xxx :a :b :c", argMap));
        assertEquals("xxx ? ? ? xx ? ?", NamedParameterUtils.substituteNamedParameters("xxx :a :b :c xx :a :a", argMap));
    }

    public void testConvertArgMapToArray() {
        Map argMap = new HashMap();
        argMap.put("a","a");
        argMap.put("b","b");
        argMap.put("c","c");
        assertTrue(3 == NamedParameterUtils.convertArgMapToArray("xxx :a :b :c", argMap).length);
        assertTrue(5 == NamedParameterUtils.convertArgMapToArray("xxx :a :b :c xx :a :b", argMap).length);
        assertTrue(5 == NamedParameterUtils.convertArgMapToArray("xxx :a :a :a xx :a :a", argMap).length);
        assertEquals("b", NamedParameterUtils.convertArgMapToArray("xxx :a :b :c xx :a :b", argMap)[4]);
    }

    public void testConvertTypeMapToArray() {
        Map typeMap = new HashMap();
        typeMap.put("a", new Integer(1));
        typeMap.put("b", new Integer(2));
        typeMap.put("c", new Integer(3));
        assertTrue(3 == NamedParameterUtils.convertTypeMapToArray(typeMap, NamedParameterUtils.parseSqlStatement("xxx :a :b :c")).length);
        assertTrue(5 == NamedParameterUtils.convertTypeMapToArray(typeMap, NamedParameterUtils.parseSqlStatement("xxx :a :b :c xx :a :b")).length);
        assertTrue(5 == NamedParameterUtils.convertTypeMapToArray(typeMap, NamedParameterUtils.parseSqlStatement("xxx :a :a :a xx :a :a")).length);
        assertEquals(2, NamedParameterUtils.convertTypeMapToArray(typeMap, NamedParameterUtils.parseSqlStatement("xxx :a :b :c xx :a :b"))[4]);
    }

}
