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

import java.util.HashSet;

import simpl.db.api.Database;
import simpl.db.api.SimplDef;

/**
 * An internal cache implementation for found {@link Database} annotations.
 */
public class DatabaseSpec extends SimplSpec<Database, SimplDef> {
    public final HashSet<TableSpec> tableSpecs = new HashSet<>();

    /**
     * @param name     as of {@link Class#getSimpleName()}
     * @param database as present at class
     * @param simplDef as the class itself
     */
    public DatabaseSpec(String name, Database database, Class<? extends SimplDef> simplDef) {
        super(name, database, simplDef);
    }
}
