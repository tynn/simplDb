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

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;

import simpl.db.SimplQuery.Filter;
import simpl.db.db.TestJoin;
import simpl.db.db.TestQuery;
import simpl.db.db.TestSimplQuery;
import simpl.db.db.TestTable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SimplQueryPreTest {

    @Test
    public void query() throws Exception {
        SimplQuery query = SimplQuery.get(TestQuery.class);
        assertArrayEquals(new Object[]{TestTable.class}, query.getTables().toArray());

        assertEquals(TestTable.NAME, get(query, "mTable"));
        String[] columns = get(query, "mColumns");
        assertArrayEquals(new String[]{TestTable.TEST}, columns);

        Filter filter = get(query, "mFilter");
        assertEquals(TestQuery.SELECTION, filter.getSelection(null));
        assertArrayEquals(TestQuery.SELECTION_ARGS, filter.getSelectionArgs(null));
        assertEquals(TestTable.TEST, filter.getGroupBy(null));
        assertEquals(TestTable.TEST, filter.getHaving(null));
        assertEquals(TestTable.TEST, filter.getOrderBy(null));
        assertEquals("2", filter.getLimit(null));
    }

    @Test
    public void queryJoin() throws Exception {
        final Object[] tables = {TestTable.class, TestTable.I.class};
        SimplQuery query = SimplQuery.get(TestJoin.class);
        assertArrayEquals(tables, query.getTables().toArray());

        final HashSet<String> expectedColumns = new HashSet<>();
        expectedColumns.add(TestTable.TEST);
        expectedColumns.add(TestTable.I.TEST2);
        assertEquals(TestJoin.NAME, get(query, "mTable"));
        String[] columns = get(query, "mColumns");
        assertArrayEquals(expectedColumns.toArray(new String[2]), columns);
    }

    @Test
    public void queryTable() throws Exception {
        SimplQuery query = SimplQuery.get(TestTable.class);
        assertArrayEquals(new Object[]{TestTable.class}, query.getTables().toArray());

        assertEquals(TestTable.NAME, get(query, "mTable"));
        assertEquals(null, get(query, "mColumns"));
    }

    @Test
    public void querySimpl() throws Exception {
        SimplQuery query = SimplQuery.get(TestSimplQuery.class);
        assertArrayEquals(new Object[]{TestTable.class}, query.getTables().toArray());

        assertEquals(TestTable.NAME, get(query, "mTable"));
        assertEquals(null, get(query, "mColumns"));
    }

    @Test(expected = SimplError.class)
    public void invalidQuery() throws Exception {
        SimplQuery.get(TestQuery.Q1.class);
    }

    @Test
    public void invalidSimplQuery() throws Exception {
        try {
            SimplQuery.get(TestSimplQuery.NoName.class);
        } catch (SimplError e){
            if (!(e.getCause() instanceof NullPointerException))
                throw e;
        }
        try {
            SimplQuery.get(TestSimplQuery.NoTable.class);
        } catch (SimplError e){
            if (!(e.getCause() instanceof ArrayIndexOutOfBoundsException))
                throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(SimplQuery query, String name) throws Exception {
        Field field = SimplQuery.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(query);
    }
}
