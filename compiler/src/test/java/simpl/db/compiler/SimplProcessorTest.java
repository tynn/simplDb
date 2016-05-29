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

package simpl.db.compiler;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

import simpl.db.api.Database;
import simpl.db.api.Table;
import simpl.db.db.TestDatabase;
import simpl.db.db.TestTable;
import simpl.db.spec.DatabaseSpec;
import simpl.db.spec.TableSpec;

import static org.junit.Assert.assertEquals;

public class SimplProcessorTest {

    @Test
    public void databaseSpec() throws Exception {
        Class<TestDatabase> dbDef = TestDatabase.class;
        Database dbAnn = dbDef.getAnnotation(Database.class);
        Class<?> dbSpec = Class.forName(dbDef.getName() + "$$" + DatabaseSpec.class.getSimpleName());
        DatabaseSpec spec = (DatabaseSpec) dbSpec.newInstance();

        assertEquals(dbDef, spec.simplDef);
        assertEquals(dbAnn, spec.annotation);
        assertEquals(dbDef.getSimpleName(), spec.name);

        HashSet<TableSpec> tableSpecs = spec.tableSpecs;
        assertEquals(1, tableSpecs.size());

        TableSpec tableSpec = tableSpecs.iterator().next();
        Class<TestTable> tableDef = TestTable.class;
        Table tableAnn = tableDef.getAnnotation(Table.class);

        assertEquals(tableDef, tableSpec.simplDef);
        assertEquals(tableAnn, tableSpec.annotation);
        assertEquals(tableDef.getSimpleName(), tableSpec.name);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void tableSpec() throws Exception {
        Class<TestTable> tableDef = TestTable.class;
        Table tableAnn = tableDef.getAnnotation(Table.class);
        Class<?> tableSpec = Class.forName(tableDef.getName() + "$$" + TableSpec.class.getSimpleName());
        TableSpec spec = (TableSpec) tableSpec.newInstance();

        assertEquals(tableDef, spec.simplDef);
        assertEquals(tableAnn, spec.annotation);
        assertEquals(tableDef.getSimpleName(), spec.name);

        for (Annotation constraint : spec.constraints) {
            Class<?> ann = constraint.getClass().getInterfaces()[0];
            assertEquals(tableDef.getAnnotation((Class<? extends Annotation>) ann), constraint);
        }

        HashMap<String, HashSet<? extends Annotation>> columnSpecs = spec.columnSpecs;
        assertEquals(3, columnSpecs.size());

        for (Field field : tableDef.getFields())
            for (Annotation columnSpec : columnSpecs.get(field.get(null).toString())) {
                Class<?> ann = columnSpec.getClass().getInterfaces()[0];
                assertEquals(field.getAnnotation((Class<? extends Annotation>) ann), columnSpec);
            }
    }
}