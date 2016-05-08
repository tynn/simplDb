/*
 * Copyright 2016 Christian Schmitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simpl.db;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import simpl.db.db.TestTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableSqlTest {
    private static TableSql sTable;

    @BeforeClass
    public static void setupBuilder() {
        sTable = new TableSql();
    }

    @Test
    public void validTable() throws Exception {
        String sql = String.format(TestTable.SQL, TestTable.NAME);
        assertEquals(sql, sTable.build(TestTable.class, false));
        assertEquals(sql, sTable.toString());
        assertEquals(TestTable.NAME, sTable.getName());
        Set<String> columns = sTable.getColumns();
        assertTrue(columns.size() == 1 && columns.contains(TestTable.TEST));
    }

    @Test
    public void validTables() throws Exception {
        sTable.build(TestTable.I.class, false);
    }

    @Test
    public void tempTable() throws Exception {
        String sql = String.format(TestTable.SQL, '_' + TestTable.NAME);
        assertEquals(sql, sTable.build(TestTable.class, true));
        assertEquals(sql, sTable.toString());
    }

    @Test(expected = SimplError.class)
    public void invalidTable() throws Exception {
        sTable.build(TestTable.I1.class, false);
    }

    @Test(expected = SimplError.class)
    public void invalidColumn() throws Exception {
        sTable.build(TestTable.I2.class, false);
    }
}
