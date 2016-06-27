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
import java.util.Map;

import simpl.db.api.Column;
import simpl.db.api.Constraint;
import simpl.db.api.Database;
import simpl.db.api.Join;
import simpl.db.api.Query;
import simpl.db.api.Table;
import simpl.db.db.TestDatabase;
import simpl.db.db.TestQuery;
import simpl.db.db.TestTable;
import simpl.db.spec.DatabaseSpec;
import simpl.db.spec.QuerySpec;
import simpl.db.spec.TableSpec;

import static org.junit.Assert.assertEquals;
import static simpl.db.internal.SimplName.from;

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
    public void querySpec() throws Exception {
        Class<TestQuery> queryDef = TestQuery.class;
        Query queryAnn = queryDef.getAnnotation(Query.class);
        Join joinAnn = queryDef.getAnnotation(Join.class);
        Class<?> querySpec = Class.forName(queryDef.getName() + "$$" + QuerySpec.class.getSimpleName());
        QuerySpec spec = (QuerySpec) querySpec.newInstance();

        assertEquals(queryDef, spec.simplDef);
        assertEquals(queryAnn, spec.annotation);
        assertEquals(joinAnn, spec.join);
        assertEquals(queryDef.getSimpleName(), spec.name);
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

        HashMap<String, HashSet<? extends Annotation>> columnSpecs = spec.columnSpecs;
        assertEquals(3, columnSpecs.size());

        HashMap<String, Annotation> uncheckedConstraints = new HashMap<>();
        for (Map.Entry<String, Annotation> entry : spec.constraints.entrySet()) {
            String name = entry.getKey();
            Annotation constraint = entry.getValue();
            Class<?> ann = constraint.getClass().getInterfaces()[0];
            Annotation expected = tableDef.getAnnotation((Class<? extends Annotation>) ann);
            if (expected != null)
                assertEquals(expected, constraint);
            else
                uncheckedConstraints.put(name, constraint);
        }

        for (Field field : tableDef.getFields()) {
            String fieldValue = field.get(null).toString();

            if (field.getAnnotation(Constraint.class) != null)
                for (Annotation expected : field.getAnnotations()) {
                    String name = from(expected.annotationType().getSimpleName() + '_' + fieldValue);
                    Annotation constraint = uncheckedConstraints.remove(name);
                    if (constraint != null)
                        assertEquals(expected, constraint);
                }

            if (field.getAnnotation(Column.class) != null)
                for (Annotation columnSpec : columnSpecs.get(field.get(null).toString())) {
                    Class<?> ann = columnSpec.getClass().getInterfaces()[0];
                    assertEquals(field.getAnnotation((Class<? extends Annotation>) ann), columnSpec);
                }
        }

        assertEquals(0, uncheckedConstraints.size());
    }
}
