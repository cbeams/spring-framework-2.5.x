package org.springframework.jdbc.core;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Map;

/**
 * @author trisberg
 */
public class BatchSqlParameterSource {

	public static SqlParameterSource[] createBatch(Map... valueMaps) {
		MapSqlParameterSource[] batch = new MapSqlParameterSource[valueMaps.length];
		int i = 0;
		for (Map valueMap : valueMaps) {
			batch[i++] = new MapSqlParameterSource(valueMap);
		}
		return batch;
	}

	public static SqlParameterSource[] createBatch(Object... beans) {
		BeanPropertySqlParameterSource[] batch = new BeanPropertySqlParameterSource[beans.length];
		int i = 0;
		for (Object bean : beans) {
			batch[i++] = new BeanPropertySqlParameterSource(bean);
		}
		return batch;
	}

}
