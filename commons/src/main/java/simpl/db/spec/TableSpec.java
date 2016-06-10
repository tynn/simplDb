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

package simpl.db.spec;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;

import simpl.db.api.Table;
import simpl.db.api.TableDef;

/**
 * An internal cache implementation for found {@link Table} annotations.
 */
public class TableSpec extends SimplSpec<Table, TableDef> {
    public final HashMap<String, HashSet<? extends Annotation>> columnSpecs = new HashMap<>();
    public final HashMap<String, Annotation> constraints = new HashMap<>();

    /**
     * @param name     as of {@link Class#getSimpleName()}
     * @param table    as present at class
     * @param tableDef as the class itself
     */
    public TableSpec(String name, Table table, Class<? extends TableDef> tableDef) {
        super(name, table, tableDef);
    }
}
