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

import java.util.Collections;

import simpl.db.api.Column;
import simpl.db.api.Table;
import simpl.db.db.TestTable;
import simpl.db.spec.TableSpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimplDbPreTest {

    @Test
    public void loadTableSpec() throws Exception {
        TableSpec spec = SimplDb.loadTableSpec(TestTable.class);
        assertEquals(TestTable.class, spec.simplDef);
        assertEquals(TestTable.NAME, spec.name);
        assertEquals(TestTable.class.getAnnotation(Table.class), spec.annotation);
        assertEquals(Collections.singleton(TestTable.TEST), spec.columnSpecs.keySet());
        assertTrue(spec.columnSpecs.get(TestTable.TEST).iterator().next() instanceof Column);
    }

    @Test
    public void getName() throws Exception {
        assertEquals(TestTable.NAME, SimplDb.getName(TestTable.class));
        assertEquals(TestTable.NAME, SimplDb.getName("testTable"));
        assertEquals(TestTable.NAME, SimplDb.getName("TestTable"));
        assertEquals(TestTable.NAME, SimplDb.getName("Test$Table"));
        assertEquals(TestTable.NAME, SimplDb.getName("test_Table"));
        assertEquals(TestTable.NAME + 1, SimplDb.getName("test_Table1"));
        assertEquals('_' + TestTable.NAME, SimplDb.getName("_test_table"));
        assertEquals("test__table", SimplDb.getName("test__table"));
    }

    @Test
    public void quoteKeyword() throws Exception {
        final String value = "some_value";
        final String expected = '"' + value + '"';
        assertEquals(expected, SimplDb.quote(value));
        assertEquals(expected, SimplDb.quote('"' + value));
        assertEquals(expected, SimplDb.quote(value + '"'));
        assertEquals(expected, SimplDb.quote(expected));
    }

    @Test
    public void validTables() throws Exception {
        SimplDb.loadTableSpec(TestTable.I.class);
    }

    @Test(expected = SimplError.class)
    public void invalidTable() throws Exception {
        SimplDb.loadTableSpec(TestTable.I1.class);
    }

    @Test(expected = SimplError.class)
    public void invalidColumn() throws Exception {
        SimplDb.loadTableSpec(TestTable.I2.class);
    }
}
