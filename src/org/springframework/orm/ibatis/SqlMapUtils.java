package org.springframework.orm.ibatis;

import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ibatis.db.sqlmap.SqlMap;
import com.ibatis.db.sqlmap.XmlSqlMapBuilder;

import org.springframework.util.ClassLoaderUtils;

/**
 * @author Juergen Hoeller
 * @since 29.11.2003
 */
public abstract class SqlMapUtils {

	public static SqlMap buildSqlMap(String configLocation) throws IOException {
		InputStream is = ClassLoaderUtils.getResourceAsStream(configLocation);
		if (is == null) {
			throw new IOException("Class path resource [" + configLocation + "] not found");
		}
		return XmlSqlMapBuilder.buildSqlMap(new InputStreamReader(is));
	}

}
