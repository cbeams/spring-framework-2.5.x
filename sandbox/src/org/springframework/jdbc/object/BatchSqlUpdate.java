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
package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.util.Assert;

/**
 * SqlUpdate subclass that performs batch update operations.
 * 
 * @author Keith Donald
 */
public class BatchSqlUpdate extends SqlUpdate {
    private int batchSize = 100;
    private SimplePreparedStatementSetter setter;
    private LinkedList queue = new LinkedList();

    public BatchSqlUpdate() {
        super();
    }

    public BatchSqlUpdate(DataSource ds, String sql) {
        super(ds, sql);
    }

    public BatchSqlUpdate(DataSource ds, String sql, int[] types) {
        super(ds, sql, types);
    }

    public BatchSqlUpdate(DataSource ds, String sql, int[] types,
            int maxRowsAffected) {
        super(ds, sql, types, maxRowsAffected);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * @see org.springframework.jdbc.object.SqlUpdate#update(java.lang.Object[])
     */
    public int update(Object[] args)
            throws InvalidDataAccessApiUsageException {
        validateParameters(args);
        queue.addFirst(args);
        if (queue.size() == batchSize) {
            return doBatchUpdate().length;
        }
        return 0;
    }

    public int flush() {
        if (queue.size() > 0) {
            return doBatchUpdate().length;
        } else {
            return 0;
        }
    }

    protected int[] doBatchUpdate() {
        if (setter == null) {
            setter = new SimplePreparedStatementSetter(
                    getDeclaredParameters());
        }
        int rows[] = getJdbcTemplate().batchUpdate(getSql(),
                new BatchPreparedStatementSetter() {
                    public int getBatchSize() {
                        return queue.size();
                    }

                    public void setValues(PreparedStatement statement,
                            int index) throws SQLException {
                        setter.setPreparedStatementParameters(statement,
                                (Object[])queue.removeLast());
                    }
                });
        Assert.isTrue(queue.isEmpty());
        return rows;
    }

}