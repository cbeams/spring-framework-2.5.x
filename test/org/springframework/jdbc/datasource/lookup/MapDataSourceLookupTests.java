package org.springframework.jdbc.datasource.lookup;

import junit.framework.TestCase;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.AssertThrows;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for the {@link MapDataSourceLookup} class.
 *
 * @author Rick Evans
 */
public final class MapDataSourceLookupTests extends TestCase {

	private static final String DATA_SOURCE_NAME = "dataSource";


	public void testGetDataSourcesReturnsUnmodifiableMap() throws Exception {
		new AssertThrows(UnsupportedOperationException.class, "The Map returned from getDataSources() *must* be unmodifiable") {
			public void test() throws Exception {
				MapDataSourceLookup lookup = new MapDataSourceLookup(new HashMap());
				Map dataSources = lookup.getDataSources();
				dataSources.put("", "");
			}
		}.runTest();
	}

	public void testLookupSunnyDay() throws Exception {
		Map dataSources = new HashMap();
		StubDataSource expectedDataSource = new StubDataSource();
		dataSources.put(DATA_SOURCE_NAME, expectedDataSource);
		MapDataSourceLookup lookup = new MapDataSourceLookup();
		lookup.setDataSources(dataSources);
		DataSource dataSource = lookup.getDataSource(DATA_SOURCE_NAME);
		assertNotNull("A DataSourceLookup implementation must *never* return null from getDataSource(): this one obviously (and incorrectly) is", dataSource);
		assertSame(expectedDataSource, dataSource);
	}

	public void testSettingDataSourceMapToNullIsAnIdempotentOperation() throws Exception {
		Map dataSources = new HashMap();
		StubDataSource expectedDataSource = new StubDataSource();
		dataSources.put(DATA_SOURCE_NAME, expectedDataSource);
		MapDataSourceLookup lookup = new MapDataSourceLookup();
		lookup.setDataSources(dataSources);
		lookup.setDataSources(null); // must be idempotent (i.e. the following lookup must still work);
		DataSource dataSource = lookup.getDataSource(DATA_SOURCE_NAME);
		assertNotNull("A DataSourceLookup implementation must *never* return null from getDataSource(): this one obviously (and incorrectly) is", dataSource);
		assertSame(expectedDataSource, dataSource);
	}

	public void testAddingDataSourcePermitsOverride() throws Exception {
		Map dataSources = new HashMap();
		StubDataSource overridenDataSource = new StubDataSource();
		StubDataSource expectedDataSource = new StubDataSource();
		dataSources.put(DATA_SOURCE_NAME, overridenDataSource);
		MapDataSourceLookup lookup = new MapDataSourceLookup();
		lookup.setDataSources(dataSources);
		lookup.addDataSource(DATA_SOURCE_NAME, expectedDataSource); // must override existing entry
		DataSource dataSource = lookup.getDataSource(DATA_SOURCE_NAME);
		assertNotNull("A DataSourceLookup implementation must *never* return null from getDataSource(): this one obviously (and incorrectly) is", dataSource);
		assertSame(expectedDataSource, dataSource);
	}

	public void testGetDataSourceWhereSuppliedMapHasNonDataSourceTypeUnderSpecifiedKey() throws Exception {
		new AssertThrows(DataAccessResourceFailureException.class) {
			public void test() throws Exception {
				Map dataSources = new HashMap();
				dataSources.put(DATA_SOURCE_NAME, new Object());
				MapDataSourceLookup lookup = new MapDataSourceLookup();
				lookup.setDataSources(dataSources);
				lookup.getDataSource(DATA_SOURCE_NAME);
			}
		}.runTest();
	}

	public void testGetDataSourceWhereSuppliedMapHasNoEntryForSpecifiedKey() throws Exception {
		new AssertThrows(DataAccessResourceFailureException.class) {
			public void test() throws Exception {
				MapDataSourceLookup lookup = new MapDataSourceLookup();
				lookup.getDataSource(DATA_SOURCE_NAME);
			}
		}.runTest();
	}

}
