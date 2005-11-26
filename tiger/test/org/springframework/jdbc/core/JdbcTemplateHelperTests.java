package org.springframework.jdbc.core;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.easymock.internal.ArrayMatcher;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Rod Johnson
 * @author Rob Harrop
 */
public class JdbcTemplateHelperTests extends TestCase {


	/*
	 * Test method for 'org.springframework.jdbc.core.JdbcTemplateHelper.queryForInt(String, Object...)'
	 */
	public void testQueryForIntWithoutArgs() {
		String sql = "SELECT COUNT(0) FROM BAR";
		int expectedResult = 666;

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForInt(sql);
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);

		assertSame(jo, jth.getJdbcOperations());

		int result = jth.queryForInt(sql);
		assertEquals(expectedResult, result);

		mc.verify();
	}

	public void testQueryForIntWithArgs() {
		String sql = "SELECT COUNT(0) FROM BAR WHERE ID=? AND XY=?";
		int expectedResult = 666;
		int arg1 = 24;
		String arg2 = "foo";

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForInt(sql, new Object[]{arg1, arg2});
		mc.setDefaultMatcher(new ArrayMatcher());
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		int result = jth.queryForInt(sql, arg1, arg2);
		assertEquals(expectedResult, result);
		mc.verify();
	}

	public void testQueryForLongWithoutArgs() {
		String sql = "SELECT COUNT(0) FROM BAR";
		long expectedResult = 666;

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForLong(sql);
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		long result = jth.queryForLong(sql);
		assertEquals(expectedResult, result);

		mc.verify();
	}

	public void testQueryForLongWithArgs() {
		String sql = "SELECT COUNT(0) FROM BAR WHERE ID=? AND XY=?";
		long expectedResult = 666;
		double arg1 = 24.7;
		String arg2 = "foo";
		Object arg3 = new Object();

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForLong(sql, new Object[]{arg1, arg2, arg3});
		mc.setDefaultMatcher(new ArrayMatcher());
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		long result = jth.queryForLong(sql, arg1, arg2, arg3);
		assertEquals(expectedResult, result);
		mc.verify();
	}

	public void testQueryForObjectWithArgs() throws Exception {
		String sql = "SELECT SOMEDATE FROM BAR WHERE ID=? AND XY=?";
		Date expectedResult = new Date();
		double arg1 = 24.7;
		String arg2 = "foo";
		Object arg3 = new Object();

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForObject(sql, new Object[]{arg1, arg2, arg3}, Date.class);
		mc.setDefaultMatcher(new ArrayMatcher());
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		Date result = jth.queryForObject(sql, Date.class, arg1, arg2, arg3);
		assertEquals(expectedResult, result);
		mc.verify();
	}

	public void testQueryForObjectWithoutArgs() throws Exception {
		String sql = "SELECT SYSDATE FROM DUAL";
		Date expectedResult = new Date();

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForObject(sql, Date.class);
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		Date result = jth.queryForObject(sql, Date.class);
		assertEquals(expectedResult, result);
		mc.verify();
	}

	public void testQueryForObjectWithRowMapperAndArgs() throws Exception {
		String sql = "SELECT SOMEDATE FROM BAR WHERE ID=? AND XY=?";
		Date expectedResult = new Date();
		double arg1 = 24.7;
		String arg2 = "foo";
		Object arg3 = new Object();

		ParameterizedRowMapper<Date> rm = new ParameterizedRowMapper<Date>() {
			public Date mapRow(ResultSet rs, int rowNum) {
				return new Date();
			}
		};

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForObject(sql, new Object[]{arg1, arg2, arg3}, rm);
		mc.setDefaultMatcher(new ArrayMatcher());
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		Date result = jth.queryForObject(sql, rm, arg1, arg2, arg3);
		assertEquals(expectedResult, result);
		mc.verify();
	}

	public void testQueryForObjectWithRowMapperAndWithoutArgs() throws Exception {
		String sql = "SELECT SYSDATE FROM DUAL";
		Date expectedResult = new Date();

		ParameterizedRowMapper<Date> rm = new ParameterizedRowMapper<Date>() {
			public Date mapRow(ResultSet rs, int rowNum) {
				return new Date();
			}
		};

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();
		jo.queryForObject(sql, rm);
		mc.setReturnValue(expectedResult);
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);
		Date result = jth.queryForObject(sql, rm);
		assertEquals(expectedResult, result);
		mc.verify();
	}

	public void testQueryForListWithArgs() throws Exception {
		testDelegation("queryForList", new Object[]{"sql"}, new Object[]{1, 2, 3}, new LinkedList());
	}

	public void testQueryForListWithoutArgs() throws Exception {
		testDelegation("queryForList", new Object[]{"sql"}, new Object[]{}, Collections.singletonList(new Object()));
	}

	public void testQueryForMapWithArgs() throws Exception {
		testDelegation("queryForMap", new Object[]{"sql"}, new Object[]{1, 2, 3}, new HashMap());
		// TODO test generic type
	}

	public void testQueryForMapWithoutArgs() throws Exception {
		testDelegation("queryForMap", new Object[]{"sql"}, new Object[]{}, new HashMap());
	}

	public void testUpdateWithArgs() throws Exception {
		testDelegation("update", new Object[]{"sql"}, new Object[]{1, 2, 3}, 666);
	}

	public void testUpdateWithoutArgs() throws Exception {
		testDelegation("update", new Object[]{"sql"}, new Object[]{}, 666);
	}

	private Object testDelegation(String methodName, Object[] typedArgs, Object[] varargs, Object expectedResult) throws Exception {
		Class[] unifiedTypes;
		Object[] unifiedArgs;
		Class[] unifiedTypes2;
		Object[] unifiedArgs2;

		if (varargs != null && varargs.length > 0) {
			// Allow for varargs.length
			unifiedTypes = new Class[typedArgs.length + 1];
			unifiedArgs = new Object[typedArgs.length + 1];
			for (int i = 0; i < unifiedTypes.length - 1; i++) {
				unifiedTypes[i] = typedArgs[i].getClass();
				unifiedArgs[i] = typedArgs[i];
			}
			unifiedTypes[unifiedTypes.length - 1] = Object[].class;
			unifiedArgs[unifiedTypes.length - 1] = varargs;

			unifiedTypes2 = unifiedTypes;
			unifiedArgs2 = unifiedArgs;
		}
		else {
			unifiedTypes = new Class[typedArgs.length];
			unifiedTypes2 = new Class[typedArgs.length + 1];
			unifiedArgs = new Object[typedArgs.length];
			unifiedArgs2 = new Object[typedArgs.length + 1];
			for (int i = 0; i < typedArgs.length; i++) {
				unifiedTypes[i] = unifiedTypes2[i] = typedArgs[i].getClass();
				unifiedArgs[i] = unifiedArgs2[i] = typedArgs[i];
			}
			unifiedTypes2[unifiedTypes2.length - 1] = Object[].class;
			unifiedArgs2[unifiedArgs2.length - 1] = new Object[]{};
		}

		MockControl mc = MockControl.createControl(JdbcOperations.class);
		JdbcOperations jo = (JdbcOperations) mc.getMock();

		Method joMethod = JdbcOperations.class.getMethod(methodName, unifiedTypes);
		joMethod.invoke(jo, unifiedArgs);

		mc.setDefaultMatcher(new ArrayMatcher());

		if (joMethod.getReturnType().isPrimitive()) {
			// TODO bit of a hack with autoboxing passing up Integer when the return
			// type is an int
			mc.setReturnValue(((Integer) expectedResult).intValue());
		}
		else {
			mc.setReturnValue(expectedResult);
		}
		mc.replay();

		JdbcTemplateHelper jth = new JdbcTemplateHelper(jo);

		Method jthMethod = JdbcTemplateHelper.class.getMethod(methodName, unifiedTypes2);
		Object result = jthMethod.invoke(jth, unifiedArgs2);

		assertEquals(expectedResult, result);

		mc.verify();

		return result;
	}

}
